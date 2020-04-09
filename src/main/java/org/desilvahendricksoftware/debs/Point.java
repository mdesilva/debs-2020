package org.desilvahendricksoftware.debs;

import org.apache.flink.api.java.tuple.Tuple2;

public final class Point extends Tuple2<Double, Double> {

    public Point(double x, double y) {
        this.f0 = x;
        this.f1 = y;
    }

    @Override
    public String toString() {
        return "(" + this.f0 + ", " + this.f1 + ")";
    }
}