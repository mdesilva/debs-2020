package org.desilvahendricksoftware.debs;

import java.util.ArrayList;

public class EventDetector {

    Cluster[] clusters;
    float temporalLocalityEpsilon;

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

}
