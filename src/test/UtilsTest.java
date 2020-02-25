package test;

import main.Utils;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

public class UtilsTest {

    Long[] voltageVals = new Long[]{5L,5L,5L,5L};
    Long[] currentVals = new Long[]{10L, 10L, 10L, 10L};

   @Test
    public void RootMeanSquareOfSetOf4ofXShouldReturnX() {
       assertEquals(5, Utils.rootMeanSquare(voltageVals), "The root mean square of the set of 4 5's is 5");
       assertEquals(10, Utils.rootMeanSquare(currentVals), "The root mean square of the set of 4 10's is 10");
    }

    @Test
    public void testApparentPower() throws Exception {
       assertEquals(50, Utils.calculateApparentPower(voltageVals, currentVals), "The apparent power of voltages " + Arrays.toString(voltageVals) + " and currents " + Arrays.toString(currentVals) + " should be 50");
    }

    @Test
    public void testReactivePower() throws Exception {
       assertEquals(0, Utils.calculateReactivePower(voltageVals, currentVals), "The reactive power of voltages " + Arrays.toString(voltageVals) + " and currents " + Arrays.toString(currentVals) + "  should be 0");
    }

    @Test
    public void testAllBasicUtils() {
        Long[] test1 = {1l,2l,3l};
        Long[] test2 = {5l,5l,6l};
        try {
            assertEquals( 2.0, (double) Utils.rootMeanSquare(test1), "The Root Means Square of " + Arrays.toString(test1) + " should be 2.0");
            assertEquals( 11.0, (double) Utils.calculateActivePower(test1, test2), "The Active Power of " + Arrays.toString(test1) + " and " + Arrays.toString(test2) + " should be 11.0");
            assertEquals(4.0, (double) Utils.calculateApparentPower(test1,test1), "The Apparent Power of " + Arrays.toString(test1) + " and " + Arrays.toString(test1) + " should be 4.0");
            Long[] test3 = {15l,15l,10l};
            Long[] test4 = {10l,10l,10l};
            assertEquals(22.852182001336743, (double) Utils.calculateReactivePower(test3, test4), "The Reactive Power of " + Arrays.toString(test3) + " and " + Arrays.toString(test4) + " should be 22.852182001336743");
        } catch (Exception e){
            System.out.println("TEST FAILED: " + e);
        }
    }
}