package org.desilvahendricksoftware.debs;

public class Result {
    long s;
    int d;
    int event_s;

    public Result(long s, boolean d, int event_s) {
        this.s = s;
        this.d = d == true ? 1 : 0 ;
        this.event_s = event_s;
    }

    @Override
    public String toString() {
        return "(" + this.s + "," + this.d + "," + this.event_s + ")";
    }
}
