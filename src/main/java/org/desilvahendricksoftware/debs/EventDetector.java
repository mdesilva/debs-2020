package org.desilvahendricksoftware.debs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class EventDetector {

    Cluster[] clusters;
    float temporalLocalityEpsilon;

    public int[] check_event_model_constraints() {

        //Check 1: Verify that we have at least two clusters other than the outlier cluster
        Cluster noiseCluster = Cluster.getNoiseCluster(clusters);
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

        ArrayList<Cluster> checkTwoClusters = new ArrayList<>();

        //Check 2: Verify that we have at least two clusters with locality > 1 - temporal locality epsilon
        for (Cluster cluster : clusters) {
            if (cluster.loc > 1 - this.temporalLocalityEpsilon) {
                checkTwoClusters.add(cluster);
            }
        }

        if (checkTwoClusters.size() < 2) {
            return null;
        }

        Cluster[] checkTwoClustersArray = new Cluster[checkTwoClusters.size()];

        int index = 0;
        for (Cluster cluster : checkTwoClusters) {
            checkTwoClustersArray[index] = cluster;
            index++;
        }


        /*
        Check 3:
        We must find two clusters that do not interleave in the time domain.
        A point s exists in C1 in which all points n > s do not belong to C1 anymore.
        A point i exists in C2 in which all points n < i do not belong to C2 anymore.
        The maximum index s of C1 has to < the minimum index i of C2
         */

        for (int i=0; i<checkTwoClustersArray.length; i++) {
            for (int j=1; j<checkTwoClustersArray.length; j++) {
                Cluster cluster_i = checkTwoClustersArray[i];
                Cluster cluster_j = checkTwoClustersArray[j];
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

                //The last event of c1 is less than the first event of c2
                if (c1.v < c2.u) {
                    if (noiseCluster == null) {
                        return null;
                    }
                    else {
                        int[] c0_indices = noiseCluster.memberIndices;
                        for (int k=0; i<c0_indices.length; i++) {

                        }
                    }
                }
            }
        }

    }

    public void compute_and_evaluate_loss(int[] checked_clusters) {
    }
}
