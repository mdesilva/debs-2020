package org.desilvahendricksoftware.debs;

import java.util.ArrayList;

public class EventDetector {

    Cluster[] clusters;
    float temporalLocalityEpsilon;
    float lossThreshold;

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

        if (numTotalClustersWithoutNoiseCluster < 2) {
            return null;
        }

        ArrayList<Cluster> checkTwoClustersBuilder = new ArrayList<>();

        //Check 2: Verify that we have at least two clusters with locality > 1 - temporal locality epsilon
        for (Cluster cluster : clusters) {
            if (cluster.loc > 1 - this.temporalLocalityEpsilon) {
                checkTwoClustersBuilder.add(cluster);
            }
        }

        if (checkTwoClustersBuilder.size() < 2) {
            return null;
        }

        Cluster[] checkTwoClusters = checkTwoClustersBuilder.toArray(new Cluster[checkTwoClustersBuilder.size()]);

        /*
        Check 3:
        We must find all pairs of clusters that do not interleave in the time domain.
        A point s exists in C1 in which all points n > s do not belong to C1 anymore.
        A point i exists in C2 in which all points n < i do not belong to C2 anymore.
        The maximum index s of C1 has to < the minimum index i of C2
         */

        ArrayList<NonInterleavingClusterPair> non_interleaving_cluster_pairs = new ArrayList<>();

        for (int i=0; i<checkTwoClusters.length; i++) {
            for (int j=1; j<checkTwoClusters.length; j++) {
                Cluster cluster_i = checkTwoClusters[i];
                Cluster cluster_j = checkTwoClusters[j];
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
                if (c1.v < c2.u) {
                    if (noiseCluster == null) {
                        return null;
                    }
                    else {
                        /*
                        Find the events in the noise cluster that are in between the last event of c1 and the first event of c2
                         */
                        int[] c0_indices = noiseCluster.memberIndices;
                        boolean[] condition = new boolean[noiseCluster.memberIndices.length];
                        int numValidEvents = 0;
                        for (int k=0; i<noiseCluster.memberIndices.length; k++) {
                            if (c0_indices[k] > c1.v && c0_indices[k] < c2.u) {
                                condition[k] = true;
                                numValidEvents++;
                            } else {
                                condition[k] = false;
                            }
                        }
                        int[] event_interval_t = new int[numValidEvents];
                        int event_interval_t_index = 0;
                        for (int l=0; l<condition.length; l++) {
                            if (condition[l]) {
                                event_interval_t[event_interval_t_index] = c0_indices[l];
                                event_interval_t_index++;
                            }
                        }

                        if (event_interval_t.length != 0) {
                            non_interleaving_cluster_pairs.add(new NonInterleavingClusterPair(c1 ,c2, event_interval_t));
                        }
                    }
                }
            }
        }
        if (non_interleaving_cluster_pairs.size() < 1) {
            return null;
        } else {
            return non_interleaving_cluster_pairs.toArray(new NonInterleavingClusterPair[non_interleaving_cluster_pairs.size()]);
        }

    }
    /*
    Compute the loss values of different non-interleaving cluster pairs.
    Return the cluster pair with the minimum loss value
     */
    public NonInterleavingClusterPair compute_and_evaluate_loss(NonInterleavingClusterPair[] checked_clusters) {
        for (int i=0;i<checked_clusters.length;i++) {
            int lower_event_bound_u = checked_clusters[i].event_interval_t[0] - 1;
            int upper_event_bound_v = checked_clusters[i].event_interval_t[-1] + 1;
            int[] c1_indices = checked_clusters[i].c1.memberIndices;
            int[] c2_indices = checked_clusters[i].c2.memberIndices;
            int[] c1_and_c2_indices = MiscUtils.concat(c1_indices, c2_indices);
            int numC2SamplesLessThanLowerBound = 0;
            int numC1SamplesGreaterThanUpperBound = 0;
            int numSamplesInBetweenBounds = 0;

            //get all c2 samples less than or equal to the lower bound u
            for (int j=0; j<c2_indices.length; j++) {
                if (c2_indices[i] <= lower_event_bound_u) {
                    numC2SamplesLessThanLowerBound++;
                }
            }

            //get all c1 samples greater than or equal to upper bound v
            for (int k=0; k<c1_indices.length; k++) {
                if (c1_indices[k] >= upper_event_bound_v) {
                    numC1SamplesGreaterThanUpperBound++;
                }
            }

            for (int l=0; l<c1_and_c2_indices.length;l++) {
                if (c1_and_c2_indices[l] > lower_event_bound_u && c1_and_c2_indices[l] < upper_event_bound_v) {
                    numSamplesInBetweenBounds++;
                }
            }
            checked_clusters[i].setModelLoss(numC2SamplesLessThanLowerBound + numC1SamplesGreaterThanUpperBound + numSamplesInBetweenBounds);
        }

        //get the cluster pair with the smallest model loss
        int leastModelLoss = checked_clusters[0].eventModelLoss;
        NonInterleavingClusterPair clusterWithLeastModelLoss = checked_clusters[0];

        for (NonInterleavingClusterPair clusterPair: checked_clusters) {
            if (clusterPair.eventModelLoss < leastModelLoss) {
             clusterWithLeastModelLoss = clusterPair;
            }
        }

        if (clusterWithLeastModelLoss.eventModelLoss <= this.lossThreshold) {
            return clusterWithLeastModelLoss;
        } else {
            return null;
        }
    }
}
