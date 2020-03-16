package org.desilvahendricksoftware.debs;// Inspired by: https://github.com/dmpalyvos/debs-2020-challenge-local/blob/master/solution_app/Test_Utility.py


import java.util.Arrays;

public class Utils {

    /***
     * Active Power P = \sum (v \times c) / 1000.
     ***/

    public static double[] calculateActivePower(Long[] voltage, Long[] current) throws Exception {
        Long ret = new Long(0);
        if(voltage.length != current.length){
            throw new Exception("Signals need the same length");
        }
        int period = voltage.length; // should be a 1000
        for(int i = 0; i < period; i++){
            Long temp = Math.multiplyExact(voltage[i], current[i]);
            ret = Math.addExact(temp, ret);
        }
        return new double[] {(double) ret / period};
    }

    /**
     * With n values {x1,x2,...,xn}
     * Room Mean Square = sqrt(1/n(x1^2 + x2^2 + .... + xn^2))
     */
    public static double[] rootMeanSquare(Long[] vals) {
        long squaresSum = 0;
        for(long i:vals) {
            squaresSum += Math.pow(i,2);
        }
        long meanSquaresSum = squaresSum / vals.length;
        return new double[] {Math.sqrt(meanSquaresSum)};
    }

    /***
     * Apparent Power S = voltage_RMS \times current_RMS
     ***/

    public static double[] calculateApparentPower(Long[] voltage, Long[] current) throws Exception{
        if(voltage.length != current.length){
            throw new Exception("Signals need the same length");
        }
        double[] rmsV = rootMeanSquare(voltage);
        double[] rmsC = rootMeanSquare(current);
        double[] ret = new double[rmsC.length];
        for(int i = 0; i < rmsC.length; i++){
            ret[i] = rmsC[i] * rmsV[i];
        }
        return ret;
    }

    /**
     *
     * Reactive Power Q = sqrt(apparent power^2 - active power^2)
     */
    public static double[] calculateReactivePower(Long[] voltage, Long[] current) throws Exception {
        if(voltage.length != current.length){
            throw new Exception("Signals need the same length");
        }
        double[] apparentPower = calculateApparentPower(voltage, current);
        double[] activePower = calculateActivePower(voltage, current);
        double[] ret = new double[activePower.length];
        for(int i = 0; i < current.length; i++){
            ret[i] = Math.sqrt((Math.pow(apparentPower[i], 2)) - Math.pow(activePower[i], 2));
        }
        return ret;
       // return Math.sqrt((Math.pow(calculateApparentPower(voltage, current),2) - Math.pow(calculateActivePower(voltage, current), 2)));
    }



    public static void main(String args[]){
        Long[] test1 = {6l,5l};
        Long[] test2 = {7l,8l};
        try{
            System.out.println(Arrays.toString(calculateActivePower(test1, test2)));
        } catch (Exception e){
            System.out.println(e);
        }
    }

}
