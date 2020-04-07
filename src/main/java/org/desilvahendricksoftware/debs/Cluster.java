package org.desilvahendricksoftware.debs;

import java.util.Arrays;

public class Cluster {

    public int label;
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
            if (i.label == -1) return i;
        }
        return null;
    }

    public float compute_temporal_locality() {
        return (float) this.memberIndices.length / (float) (this.v - this.u + 1);
    }

    public String toString(){
        return ("Cluster " + this.label + " :\n" + "Member indices: " + Arrays.toString(this.memberIndices) + "\nu: " + this.u + "\nv: " + this.v + "\nloc: " + this.loc + "\n");
    }
}
