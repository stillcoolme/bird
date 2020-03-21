package com.stillcoolme.leetcode.lesson.algorithm;

/**
 * Author: stillcoolme
 * Date: 2020/2/4 16:11
 * Description:
 */
public class BinarySearch {

    public static void main(String[] args) {
        int[] array  = {12, 21, 22, 34, 44};
        int target = binarySearch(array, 23);
        System.out.println(target);
    }

    public static int binarySearch(int[] array, int target) {
        if(array == null) {
            return 0;
        }
        int start = 0;
        int end = array.length - 1;
        int firstMax = 0;
        while (start <= end) {
            int middle = (start + end) / 2;
            if(array[middle] > target) {
                firstMax = middle;
                end = middle - 1;
            } else if(array[middle] < target){
                start = middle + 1;
            } else {
                return middle;
            }
        }
        return firstMax;
    }
}
