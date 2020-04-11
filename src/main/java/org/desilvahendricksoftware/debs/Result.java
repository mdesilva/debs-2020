package org.desilvahendricksoftware.debs;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.HashMap;
import java.util.Map;

public class Result {
    long s;
    int d;
    long event_s;

    public Result(long s, boolean d, long event_s) {
        this.s = s;
        this.d = d == true ? 1 : 0 ;
        this.event_s = event_s;
    }
}
