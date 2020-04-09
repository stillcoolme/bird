package com.stillcoolme.leetcode.lesson.algorithm.sort;

/**
 * @author: stillcoolme
 * @date: 2020/3/25 21:35
 * Function:
 */
public class BubbleSort {

    public static int[] bubbleSort(int[] array) {
        if (array == null) {
            return array;
        }
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length - i; j++) {
                if (array[j - 1] > array[j]) {
                    int temp = array[j - 1];
                    array[j - 1] = array[j];
                    array[j] = temp;
                }
            }
        }
        return array;
    }

    public static void main(String[] args) {
        int[] array  = {12, 21, 22, 22, 34, 44};
        int[] sorted = bubbleSort(array);
        for (int i = 0; i < sorted.length; i++) {
            System.out.print(sorted[i] + " ");
        }
    }
}
