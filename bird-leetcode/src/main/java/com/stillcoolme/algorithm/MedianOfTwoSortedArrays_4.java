package com.stillcoolme.algorithm;

/**
 * @author: stillcoolme
 * @date: 2019/8/19 18:38
 * @description:
 * 给定两个大小为 m 和 n 的有序数组 nums1 和 nums2。
 * 请你找出这两个有序数组的中位数，并且要求算法的时间复杂度为 O(log(m + n))。
 * 你可以假设 nums1 和 nums2 不会同时为空。
 **/
public class MedianOfTwoSortedArrays_4 {

    /**
     * 二分查找，思路：https://mp.weixin.qq.com/s/FBlH7o-ssj_iMEPLcvsY2w
     * @param nums1
     * @param nums2
     * @return
     */
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int n = nums1.length;
        int m = nums2.length;
        int left = (n + m + 1) / 2;
        int right = (n + m + 2) / 2;
        return 0.5 * (getKth(nums1, 0, n - 1, nums2, 0, m - 1, left) + getKth(nums1, 0, n - 1, nums2, 0, m - 1, right));
    }

    private int getKth(int[] nums1, int start1, int end1, int[] nums2, int start2, int end2, int k) {
        int len1 = end1 - start1 + 1;
        int len2 = end2 - start2 + 1;
        if(len1 > len2) {
            getKth(nums2, start2, end2, nums1, start1, end1, k);
        }
        if(len1 == 0) {
            return nums2[start2 + k - 1];
        }
        if(k == 1) {
            return Math.min(nums1[start1], nums2[start2]);
        }
        int i = start1 + Math.min(len1, k / 2) - 1;
        int j = start2 + Math.min(len2, k / 2) - 1;

        if(nums1[i] > nums2[j]) {
            return getKth(nums1, start1, end1, nums2, j + 1, end2, k - (j - start2 + 1));
        } else {
            return getKth(nums1, i + 1, end1, nums2, start2, end2, k - (i - start1 + 1));
        }
    }

    public static void main(String[] args) {
        MedianOfTwoSortedArrays_4 findMedianSortedArrays = new MedianOfTwoSortedArrays_4();
        int[] num1 = {1};
        int[] num2 = {};
        double middle = findMedianSortedArrays.findMedianSortedArrays(num1, num2);
        System.out.println(middle);
    }
}
