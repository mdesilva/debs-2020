package org.desilvahendricksoftware.debs;

public class Cluster {

    public int clusterNum;
    public int[] memberIndices;
    public int u;
    public int v;
    public float loc;

    public Cluster(int[] memberIndices, int u, int v, float loc) {
        this.memberIndices = memberIndices;
        this.u = u;
        this.v = v;
        this.loc = loc;
    }
    public Cluster(){}

    public static Cluster getNoiseCluster(Cluster[] clusters) {
        for (Cluster i: clusters) {
            if (i.clusterNum == -1) return i;
        }
        return null;
    }

    public float compute_temporal_locality() {
        return this.memberIndices.length / (this.v - this.u + 1);
    }
}
