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
}