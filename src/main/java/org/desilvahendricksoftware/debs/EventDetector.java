package org.desilvahendricksoftware.debs;

import org.apache.flink.api.java.tuple.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class EventDetector implements Serializable {

    Cluster[] clusters;
    Cluster[] forward_pass_clusters;
    Cluster[] backward_pass_clusters;
    DBSCAN dbscan;
    double temporalLocalityEpsilon;
    double lossThresholdLambda;
    boolean eventDetected = false;
    int numWindowsProcessedSinceLastEventDetectedCheck = 0; //if we hit 100 windows and we still haven't detected an event, remove the earliest pair of features from X

    public EventDetector(double dbscan_epsilon, int dbscan_minPoints, double temporalLocalityEpsilon, double lossThresholdLambda) {
        this.temporalLocalityEpsilon = temporalLocalityEpsilon;
        this.lossThresholdLambda = lossThresholdLambda;
        this.dbscan = new DBSCAN(dbscan_epsilon, dbscan_minPoints);
    }

    /*
    Take the clusters outputted by the DBSCAN algorithm, and perform the following checks:
    verify that we have at least two clusters other than the outlier cluster
    verify that we have at least two clusters with locality > 1 - temp locality epsilon
    RETURN all cluster pairs that do not interleave in the time domain
     */

    public NonInterleavingClusterPair[] check_event_model_constraints() {

        //Check 1: Verify that we have at least two clusters other than the outlier cluster
        Cluster noiseCluster = Cluster.getNoiseCluster(this.clusters);
        int numTotalClusters = this.clusters.length;
        int numTotalClustersWithoutNoiseCluster;
        if (noiseCluster != null) {
            numTotalClustersWithoutNoiseCluster = numTotalClusters - 1;
        } else {
            numTotalClustersWithoutNoiseCluster = numTotalClusters;
        }

        System.out.println("Total number of clusters without noise: " + numTotalClustersWithoutNoiseCluster);

        if (numTotalClustersWithoutNoiseCluster < 2) {
            return null;
        }
        ArrayList<Cluster> checkTwoClustersBuilder = new ArrayList<>();
        //Check 2: Verify that we have at least two clusters with locality > 1 - temporal locality epsilon, not including the noise cluster
        for (Cluster cluster : clusters) {
            if (cluster.label != -1) {
                System.out.println(cluster.toString());
                if (cluster.loc >= 1 - this.temporalLocalityEpsilon) {
                    System.out.println("add");
                    checkTwoClustersBuilder.add(cluster);
                }
            } else {
                System.out.println("Noise cluster: " + cluster.toString());
            }

        }
        if (checkTwoClustersBuilder.size() < 2) {
            return null;
        }


        Cluster[] checkTwoClusters = checkTwoClustersBuilder.toArray(new Cluster[checkTwoClustersBuilder.size()]);
        System.out.println("Number of clusters that pass Check 2: " + checkTwoClusters.length);
        /*
        Check 3:
        We must find all pairs of clusters that do not interleave in the time domain.
        A point s exists in C1 in which all points n > s do not belong to C1 anymore.
        A point i exists in C2 in which all points n < i do not belong to C2 anymore.
        The maximum index s of C1 has to < the minimum index i of C2
         */

        ArrayList<NonInterleavingClusterPair> non_interleaving_cluster_pairs = new ArrayList<>();
        for (int i = 0; i < checkTwoClusters.length; i++) {
            for (int j = 1; j < checkTwoClusters.length; j++) {
                Cluster cluster_i = checkTwoClusters[i];
                Cluster cluster_j = checkTwoClusters[j];
                System.out.println(cluster_i.toString());
                System.out.println(cluster_j.toString());

                Cluster c1;
                Cluster c2;
                // The cluster with the smaller u, occurs first in time. That cluster will be c1.
                if (cluster_i.u < cluster_j.u) {
                    c1 = cluster_i;
                    c2 = cluster_j;
                } else {
                    c1 = cluster_j;
                    c2 = cluster_i;
                }
                //The last event of c1 must be less than the first event of c2
                //System.out.println("c1.v: " + c1.v + " c2.u: " + c2.u);
                if (c1.v < c2.u) {
                    System.out.println("hit");
                    if (noiseCluster == null) {
                        return null;
                    } else {
                        /*
                        Find the events in the noise cluster that are in between the last event of c1 and the first event of c2
                         */
                        int[] c0_indices = noiseCluster.memberIndices;
                        System.out.println("c0_indices: " + Arrays.toString(c0_indices));
                        boolean[] condition = new boolean[noiseCluster.memberIndices.length];
                        int numValidEvents = 0;
                        for (int k = 0; k < noiseCluster.memberIndices.length; k++) {
                            if (c0_indices[k] > c1.v && c0_indices[k] < c2.u) {
                                condition[k] = true;
                                numValidEvents++;
                            } else {
                                condition[k] = false;
                            }
                        }
                        System.out.println("Condition: " + Arrays.toString(condition));
                        int[] event_interval_t = new int[numValidEvents];
                        int event_interval_t_index = 0;
                        for (int l = 0; l < condition.length; l++) {
                            if (condition[l]) {
                                event_interval_t[event_interval_t_index] = c0_indices[l];
                                event_interval_t_index++;
                            }
                        }

                        if (event_interval_t.length != 0) {
                            non_interleaving_cluster_pairs.add(new NonInterleavingClusterPair(c1, c2, event_interval_t));
                        }
                    }
                }
            }
        }
        if (non_interleaving_cluster_pairs.size() < 1) {
            return null;
        } else {
            System.out.println("Passed check 3");
            return non_interleaving_cluster_pairs.toArray(new NonInterleavingClusterPair[non_interleaving_cluster_pairs.size()]);
        }

    }

    /*
    Compute the loss values of different non-interleaving cluster pairs.
    Return the cluster pair with the minimum loss value
     */
    public NonInterleavingClusterPair compute_and_evaluate_loss(NonInterleavingClusterPair[] checked_clusters) {
        for (int i = 0; i < checked_clusters.length; i++) {
            System.out.println(Arrays.toString(checked_clusters[i].event_interval_t));
            int lower_event_bound_u = checked_clusters[i].event_interval_t[0] - 1;
            int upper_event_bound_v = checked_clusters[i].event_interval_t[checked_clusters[i].event_interval_t.length - 1] + 1;
            int[] c1_indices = checked_clusters[i].c1.memberIndices;
            int[] c2_indices = checked_clusters[i].c2.memberIndices;
            int[] c1_and_c2_indices = ArrayUtils.concat(c1_indices, c2_indices);
            int numC2SamplesLessThanLowerBound = 0;
            int numC1SamplesGreaterThanUpperBound = 0;
            int numSamplesInBetweenBounds = 0;

            //get all c2 samples less than or equal to the lower bound u
            for (int j = 0; j < c2_indices.length; j++) {
                if (c2_indices[i] <= lower_event_bound_u) {
                    numC2SamplesLessThanLowerBound++;
                }
            }

            //get all c1 samples greater than or equal to upper bound v
            for (int k = 0; k < c1_indices.length; k++) {
                if (c1_indices[k] >= upper_event_bound_v) {
                    numC1SamplesGreaterThanUpperBound++;
                }
            }

            for (int l = 0; l < c1_and_c2_indices.length; l++) {
                if (c1_and_c2_indices[l] > lower_event_bound_u && c1_and_c2_indices[l] < upper_event_bound_v) {
                    numSamplesInBetweenBounds++;
                }
            }
            checked_clusters[i].setModelLoss(numC2SamplesLessThanLowerBound + numC1SamplesGreaterThanUpperBound + numSamplesInBetweenBounds);
        }

        //get the cluster pair with the smallest model loss
        int leastModelLoss = checked_clusters[0].eventModelLoss;
        NonInterleavingClusterPair clusterWithLeastModelLoss = checked_clusters[0];

        for (NonInterleavingClusterPair clusterPair : checked_clusters) {
            if (clusterPair.eventModelLoss < leastModelLoss) {
                clusterWithLeastModelLoss = clusterPair;
            }
        }

        if (clusterWithLeastModelLoss.eventModelLoss <= this.lossThresholdLambda) {
            return clusterWithLeastModelLoss;
        } else {
            return null;
        }
    }

    public void update_clustering_structure(Point[] X) {
        this.dbscan.fit(X);
        int[] clusters_X = this.dbscan.clusters;
        int[] cluster_labels = this.dbscan.labels;
        Cluster[] clusters = new Cluster[cluster_labels.length];
        int index = 0;
        for (int cluster_label : cluster_labels) {
            Cluster cluster = new Cluster();
            cluster.label = cluster_label;
            ArrayList<Integer> member_indices = new ArrayList<Integer>();
            for (int i = 0; i < clusters_X.length; i++) {
                if (cluster_label == clusters_X[i]) {
                    member_indices.add(i);
                }
            }
            cluster.memberIndices = ArrayUtils.toArray(member_indices);
            cluster.u = ArrayUtils.min(cluster.memberIndices);
            cluster.v = ArrayUtils.max(cluster.memberIndices);
            cluster.loc = cluster.compute_temporal_locality();
            clusters[index] = cluster;
            index++;
        }
        this.clusters = clusters;
    }

    public Tuple2<Long, Integer> predict(long windowId, Point[] X) {
        //Forward pass

        this.update_clustering_structure(X); //step 2
        for (int i = 0; i < this.clusters.length; i++) {
            this.clusters[i].toString();
        }

        NonInterleavingClusterPair[] valid_cluster_pairs = this.check_event_model_constraints(); //step 2a
        if (valid_cluster_pairs == null || valid_cluster_pairs.length == 0) {
            //System.out.println("No valid clusters found");
            return new Tuple2(windowId, 0); //We need at least one cluster pair to continue (e.g no event was detected). Return and take the next sample
        }
        //Event detected, so now return the cluster pair with the least model loss
        NonInterleavingClusterPair forward_pass_event_cluster_pair_with_least_loss = this.compute_and_evaluate_loss(valid_cluster_pairs); //step 3
        if (forward_pass_event_cluster_pair_with_least_loss == null) {
            return new Tuple2(windowId, 0); //No cluster pair was found with model loss < loss threshold. Return and take the next sample
        } else {
            //Begin backwards pass if we found a non interleaving cluster pair with loss < loss threshold
            this.forward_pass_clusters = this.clusters;
            this.backward_pass_clusters = this.forward_pass_clusters;
            NonInterleavingClusterPair last_known_valid_event_cluster_pair_with_least_loss = forward_pass_event_cluster_pair_with_least_loss;
            for (int i = 0; i < X.length; i++) {
                Point sample_to_be_cut = X[0];
                Point[] X_cut = Arrays.copyOfRange(X, i + 1, X.length); //step 5
                this.update_clustering_structure(X_cut); //step 6

                //Find all non-interleaving cluster pairs in this cluster structure (same as cluster structure in forward pass except 1st cluster is removed with each iteration)
                NonInterleavingClusterPair[] backward_pass_checked_clusters = this.check_event_model_constraints();
                if (backward_pass_checked_clusters == null) {
                    break;
                } else {
                    NonInterleavingClusterPair current_backward_pass_event_cluster_pair_with_least_loss = this.compute_and_evaluate_loss(backward_pass_checked_clusters);
                    if (current_backward_pass_event_cluster_pair_with_least_loss == null) {
                        // Without the last sample, no event is detected; so reinsert the last sample back into the window
                        Point[] XwithLastSample = new Point[X_cut.length + 1];
                        XwithLastSample[0] = sample_to_be_cut;
                        for (int j=1; j < XwithLastSample.length; j++) {
                            XwithLastSample[j] = X_cut[j-1];
                        }
                        this.update_clustering_structure(XwithLastSample);
                        break;
                    } else {
                        last_known_valid_event_cluster_pair_with_least_loss = current_backward_pass_event_cluster_pair_with_least_loss;
                        continue;
                    }
                }
            }
            System.out.println("Event detected at " + Arrays.toString(last_known_valid_event_cluster_pair_with_least_loss.event_interval_t));
            return new Tuple2(windowId, 1);
        }
    }
}
