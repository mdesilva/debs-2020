package main;// Inspired by: https://github.com/dmpalyvos/debs-2020-challenge-local/blob/master/solution_app/Test_Utility.py


public class Utils {

    /***
     * Active Power P = \sum (v \times c) / 1000.
     ***/

    public static Double calculateActivePower(Long[] voltage, Long[] current) throws Exception {
        Long ret = new Long(0);
        if(voltage.length != current.length){
            throw new Exception("Signals need the same length");
        }
        int period = voltage.length; // should be a 1000
        for(int i = 0; i < period; i++){
            Long temp = Math.multiplyExact(voltage[i], current[i]);
            ret = Math.addExact(temp, ret);
        }
        return (double) ret / period;
    }

    /**
     * With n values {x1,x2,...,xn}
     * Room Mean Square = sqrt(1/n(x1^2 + x2^2 + .... + xn^2))
     */
    public static Double rootMeanSquare(Long[] vals) {
        long squaresSum = 0;
        for(long i:vals) {
            squaresSum += Math.pow(i,2);
        }
        long meanSquaresSum = squaresSum / vals.length;
        return Math.sqrt(meanSquaresSum);
    }

    /***
     * Apparent Power S = voltage_RMS \times current_RMS
     ***/

    public static Double calculateApparentPower(Long[] voltage, Long[] current) throws Exception{
        if(voltage.length != current.length){
            throw new Exception("Signals need the same length");
        }
        return rootMeanSquare(voltage) * rootMeanSquare(current);
    }

    /**
     *
     * Reactive Power Q = sqrt(apparent power^2 - active power^2)
     */
    public static Double calculateReactivePower(Long[] voltage, Long[] current) throws Exception {
        return Math.sqrt((Math.pow(calculateApparentPower(voltage, current),2) - Math.pow(calculateActivePower(voltage, current), 2)));
    }



    public static void main(String args[]){
        Long[] test1 = {15l,15l,10l};
        Long[] test2 = {10l,10l,10l};
        try{
            System.out.println(calculateReactivePower(test1, test2));
        } catch (Exception e){
            System.out.println(e);
        }
    }

}
