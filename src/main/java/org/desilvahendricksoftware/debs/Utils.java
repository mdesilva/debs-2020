package org.desilvahendricksoftware.debs;// Inspired by: https://github.com/dmpalyvos/debs-2020-challenge-local/blob/master/solution_app/Test_Utility.py


import java.lang.reflect.Array;
import java.util.Arrays;

public class Utils {

    /***
     * Active Power P = \sum (v \times c) / 1000.
     ***/

    public static Double calculateActivePower(Double[] voltage, Double[] current) throws Exception {
        Double ret = new Double(0);
        if(voltage.length != current.length){
            throw new Exception("Signals need the same length");
        }
        int period = voltage.length; // should be 1000
        for(int i = 0; i < period; i++){
            Double temp = voltage[i] * current[i];
            ret += temp;
        }
        return ret / period;
    }

    /**
     * With n values {x1,x2,...,xn}
     * Room Mean Square = sqrt(1/n(x1^2 + x2^2 + .... + xn^2))
     */
    public static Double rootMeanSquare(Double[] vals) {
        double square = 0.0;
        double mean;
        double root;
        // Calculate square.
        for(int i = 0; i < 1000; i++)
        {
            square += Math.pow(vals[i], 2);
        }

        // Calculate Mean.
        mean = square / 1000.0;

        // Calculate Root.
        root = Math.sqrt(mean);
        return root;
    }

    /***
     * Apparent Power S = voltage_RMS \times current_RMS
     ***/

    public static Double calculateApparentPower(Double[] voltage, Double[] current) throws Exception{
        if(voltage.length != current.length){
            throw new Exception("Signals need the same length");
        }
        return rootMeanSquare(voltage) * rootMeanSquare(current);
    }

    /**
     *
     * Reactive Power Q = sqrt(apparent power^2 - active power^2)
     */
    public static Double calculateReactivePower(Double[] voltage, Double[] current) throws Exception {
        return Math.sqrt((Math.pow(calculateApparentPower(voltage, current),2) - Math.pow(calculateActivePower(voltage, current), 2)));
    }



    public static void main(String args[]){
    }

}