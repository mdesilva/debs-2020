package org.desilvahendricksoftware.debs;

public class NonInterleavingClusterPair {

    Cluster c1;
    Cluster c2;
    int[] event_interval_t;
    public int eventModelLoss;

    public NonInterleavingClusterPair(Cluster c1, Cluster c2, int[] event_interval_t) {
        this.c1 = c1;
        this.c2 = c2;
        this.event_interval_t = event_interval_t;
    }

    public void setModelLoss(int modelLoss) {
        this.eventModelLoss = modelLoss;
    }
}
