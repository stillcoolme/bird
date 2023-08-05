package com.stillcoolme.leetcode.algorithm.sort;

/**
 * @author: stillcoolme
 * @date: 2020/4/12 21:17
 * Function:
 *  归并排序
 */
public class MergeSort {

    public static void main(String[] args) {
        int[] array = {12, 21, 22, 22, 34, 44};
        mergeSort(array);
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
    }

    public static void mergeSort(int[] array) {
        sort(array, 0, array.length - 1);
    }

    public static void sort(int[] array, int left, int right) {
        if (left >= right) {
            return;
        }
        int center = (left + right) / 2;
        sort(array, left, center);
        sort(array, center + 1, right);
        merge(array, left, center, right);
    }

    /**
     * 将两个数组进行归并，归并前面2个数组已有序，归并后依然有序
     * @param array
     *            数组对象
     * @param left
     *            左数组的第一个元素的索引
     * @param center
     *            左数组的最后一个元素的索引，center+1是右数组第一个元素的索引
     * @param right
     *            右数组最后一个元素的索引
     */
    private static void merge(int[] array, int left, int center, int right) {
        // 临时数组
        int[] tmpArr = new int[array.length];
        // 临时数组的索引
        int tmpIndex = left;
        // 右数组的第一个元素的索引
        int mid = center + 1;
        // 缓存 左数组第一个元素的索引
        int tmpFlag = left;

        while (left <= center && mid <= right) {
            // 从两个数组中取出最小的放入临时数组
            if (array[left] <= array[mid]) {
                tmpArr[tmpIndex++] = array[left++];
            } else {
                tmpArr[tmpIndex++] = array[mid++];
            }
        }
        // 剩余部分依次放入临时数组（实际上两个while只会执行其中一个）
        while (left <= center) {
            tmpArr[tmpIndex++] = array[left++];
        }
        while (mid <= right) {
            tmpArr[tmpIndex++] = array[mid++];
        }
        // 将临时数组中的内容拷贝回原数组中
        // （原left-right范围的内容被复制回原数组）
        while (tmpFlag <= right) {
            array[tmpFlag] = tmpArr[tmpFlag++];
        }
    }


}
