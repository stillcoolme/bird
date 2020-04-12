package com.stillcoolme.basic;

import java.util.LinkedList;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
public class Test {

    public static int binarySearch(int[] array, int target) {
        if (array == null) {
            return -1;
        }
        int min = 0;
        int max = array.length;
        int firstMax = 0;
        while (min <= max) {
            int middle = (max + min) / 2;
            if (array[middle] > target) {
                firstMax = middle;
                max = middle - 1;
            } else if (array[middle] < target) {
                min = middle + 1;
            } else {
                return middle;
            }
        }
        return firstMax;
    }

    public static void main(String[] args) {
        String a = "123"; String b = "123";
        System.out.println(a == b);
    }
}
