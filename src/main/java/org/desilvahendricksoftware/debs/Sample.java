package org.desilvahendricksoftware.debs;

public class Sample {

    long i;
    double voltage;
    double current;

    public Sample(long i, double voltage, double current) {
        this.i = i;
        this.voltage = voltage;
        this.current = current;
    }

    public String toString() {
        return "Index: " + this.i + " Voltage: " + this.voltage + " Current: " + this.current;
    }
}
