//package org.desilvahendricksoftware.debs.test;

import org.desilvahendricksoftware.debs.Utils;
import org.junit.Test;

import java.util.Arrays;

import static org.desilvahendricksoftware.debs.Utils.calculateApparentPower;
import static org.junit.Assert.*;

public class UtilsTest {

    Double[] voltageVals = new Double[]{5.0,5.0,5.0,5.0};
    Double[] currentVals = new Double[]{10.0, 10.0, 10.0, 10.0};

    @Test
    public void RootMeanSquareOfSetOf4ofXShouldReturnX() {
        assertEquals(5.0,(double) Utils.rootMeanSquare(voltageVals), 0);
        assertEquals(10.0, (double)Utils.rootMeanSquare(currentVals), 0);
    }

    @Test
    public void testApparentPower() throws Exception {
        assertEquals(50.0, (double) calculateApparentPower(voltageVals, currentVals),0);
    }

    @Test
    public void testReactivePower() throws Exception {
        assertEquals( 0.0, (double) Utils.calculateReactivePower(voltageVals, currentVals),0);
    }

    @Test
    public void testAllBasicUtils() {
        Double[] test1 = {1.0,2.0,3.0};
        Double[] test2 = {5.0,5.0,6.0};
        try {
            assertEquals(2.0, (double) Utils.rootMeanSquare(test1),0);
            assertEquals(11.0, (double) Utils.calculateActivePower(test1, test2),0);
            assertEquals(4.0, (double) calculateApparentPower(test1,test1),0);
            Double[] test3 = {15.0,15.0,10.0};
            Double[] test4 = {10.0,10.0,10.0};
            assertEquals(22.852182001336743, (double) Utils.calculateReactivePower(test3, test4),0);
        } catch (Exception e){
            System.out.println("TEST FAILED: " + e);
        }
    }
}