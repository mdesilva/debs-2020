package org.desilvahendricksoftware.debs;

import org.apache.flink.api.java.tuple.Tuple3;

import java.io.Serializable;

public final class Point extends Tuple3<Double, Double, Long> implements Serializable{

    public Point(double x, double y, Long id) {
        this.f0 = x;
        this.f1 = y;
        this.f2 = id;
    }

    @Override
    public String toString() {
        return "(" + this.f0 + ", " + this.f1 + "," + this.f2 + ")";
    }
}