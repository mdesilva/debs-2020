package org.desilvahendricksoftware.debs;

import java.util.Arrays;

public class NonInterleavingClusterPair {

    Cluster c1;
    Cluster c2;
    int[] event_interval_t; //samples in noise cluster between c1.u and c2.v
    public int eventModelLoss;

    public NonInterleavingClusterPair(Cluster c1, Cluster c2, int[] event_interval_t) {
        this.c1 = c1;
        this.c2 = c2;
        this.event_interval_t = event_interval_t;
    }

    @Override
    public String toString() {
        return "NonInterleavingClusterPair{" +
                "c1=" + c1.toString() +
                ", c2=" + c2.toString() +
                ", event_interval_t=" + Arrays.toString(event_interval_t) +
                ", eventModelLoss=" + eventModelLoss +
                '}';
    }

    public void setModelLoss(int modelLoss) {
        this.eventModelLoss = modelLoss;
    }
}
