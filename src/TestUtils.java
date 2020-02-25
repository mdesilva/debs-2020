import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestUtils {

    @Test
    public void BasicUtils() {
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