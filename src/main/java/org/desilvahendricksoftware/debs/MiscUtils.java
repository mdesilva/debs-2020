package org.desilvahendricksoftware.debs;

import java.lang.reflect.Array;

public class MiscUtils {

    /*
    public static <E extends Array> E concat(E[] arrayA, E[] arrayB) {
        E[] concatenatedArray = new E[arrayA.length + arrayB.length]();
    }
     */

    public static int[] concat(int[] arrayA, int[] arrayB) {
        int[] concatenatedArray = new int[arrayA.length + arrayB.length];
        int index = 0;
        for (int i=0; i<arrayA.length;i++) {
            concatenatedArray[index++] = arrayA[i];
        }

        for (int i: arrayB) {
            concatenatedArray[index++] = i;
        }
        return concatenatedArray;
    }
}
