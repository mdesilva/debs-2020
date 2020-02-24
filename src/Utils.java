// Inspired by: https://github.com/dmpalyvos/debs-2020-challenge-local/blob/master/solution_app/Test_Utility.py


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

    /***
     * Apparent Power S = voltage_RMS \times current_RMS
     ***/

    public static Long calculateApparentPower(Long voltage, Long current){
        return new Long(1);
    }

    public static Long reactivePower(Long apparentPower, Long activePower){
        return new Long(1);
    }



    public static void main(String args[]){
        Long[] test1 = {1l,2l,3l};
        Long[] test2 = {4l,5l,6l};
        try{
            System.out.println(calculateActivePower(test1, test2));
        } catch (Exception e){
            System.out.println(e);
        }
    }

}
