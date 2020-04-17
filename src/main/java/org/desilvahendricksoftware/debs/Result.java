package org.desilvahendricksoftware.debs;

public class Result {
    long s;
    int d;
    double event_s;

    public Result(long s, boolean d, double event_s) {
        this.s = s;
        this.d = d == true ? 1 : 0 ;
        this.event_s = event_s;
    }
}
