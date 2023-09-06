package com.stillcoolme.leetcode.algorithm.sort;

/**
 * @author: stillcoolme
 * @date: 2020/3/25 22:23
 * Function:
 */
public class QuickSort {

    public static void main(String[] args) {
        int[] array = {12, 21, 22, 22, 34, 44};
        quickSort(array);
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
    }

    public static void quickSort(int[] array) {
        sort(array, 0, array.length - 1);
    }

    public static void sort(int[] array, int low, int high) {
        int i, j, index;
        if (low > high) {
            return;
        }
        i = low;
        j = high;
        // 用子表的第一个记录做基准
        index = array[i];
        // 从表的两端交替向中间扫描
        while (i < j) {
            while (i < j && array[j] >= index) {
                j--;
            }
            if (i < j) {
                // 用比基准小的记录替换低位记录
                array[i++] = array[j];
            }
            while (i < j && array[i] < index) {
                i++;
            }
            if (i < j) {
                // 用比基准大的记录替换高位记录
                array[j--] = array[i];
            }
        }
        // 将基准数值替换回 a[i]
        array[i] = index;
        // 对低子表进行递归排序
        sort(array, low, i - 1);
        // 对高子表进行递归排序
        sort(array, i + 1, high);
    }

}
