package org.desilvahendricksoftware.debs;

import java.util.ArrayList;

public class ArrayUtils {

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

    public static int[] unique(int[] array) {
        ArrayList<Integer> uniqueValuesBuilder = new ArrayList<>();
        for (int i: array) {
            if (!uniqueValuesBuilder.contains(i)) {
                uniqueValuesBuilder.add(i);
            }
        }
        int[] uniqueValues = new int[uniqueValuesBuilder.size()];
        for (int i=0; i<uniqueValues.length; i++) {
            uniqueValues[i] = uniqueValuesBuilder.get(i);
        }
        return uniqueValues;
    }

    public static int[] toArray(ArrayList<Integer> arrayList) {
        int[] ret = new int[arrayList.size()];
        for (int i=0; i<arrayList.size(); i++) {
            ret[i] = arrayList.get(i);
        }
        return ret;
    }

    public static int min(int[] array) {
        int min = array[0];
        for (int i=0; i<array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }
    public static int max(int[] array) {
        int max = array[0];
        for (int i=0; i<array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }
}
