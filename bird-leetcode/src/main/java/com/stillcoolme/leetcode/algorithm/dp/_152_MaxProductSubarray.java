package com.stillcoolme.leetcode.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/7/28 9:23
 * @description:
 *  求子数组能得到的最大乘积，子数组需要连续
 **/
public class _152_MaxProductSubarray {

    /**
     * DP   难点在于有正数有负数
     * @param nums
     * @return 负数
     */
    public int maxProduct2(int[] nums) {
        if(nums == null || nums.length < 1) {
            return 0;
        }
        // 在i的位置可能是正，可能是负；就可能得到 正最大，负最大。所以用两个数组来做状态的定义
        int[] positiveMax = new int[nums.length];
        int[] negativeMax = new int[nums.length];
        positiveMax[0] = nums[0];
        negativeMax[0] = nums[0];
        int result = nums[0];
        for (int i = 1; i < nums.length; i++) {
            positiveMax[i] = Math.max(Math.max(positiveMax[i - 1] * nums[i], negativeMax[i - 1] * nums[i]), nums[i]);
            negativeMax[i] = Math.min(Math.min(positiveMax[i - 1] * nums[i], negativeMax[i - 1] * nums[i]), nums[i]);
            if(positiveMax[i] > result) {
                result = positiveMax[i];
            }
        }
        return result;
    }

    /**
     * 执行用时 :
     * 4 ms, 在所有 Java 提交中击败了72.66%的用户
     * 内存消耗 :
     * 36.7 MB, 在所有 Java 提交中击败了43.74%的用户
     * @param nums
     * @return
     */
    public int maxProduct3(int[] nums) {
        if(nums == null || nums.length < 1) {
            return 0;
        }
        int max = nums[0];
        int min = nums[0];
        int result = nums[0];
        for (int i = 1; i < nums.length; i++) {
            int temp = max;
            max = Math.max(Math.max(max * nums[i], min * nums[i]), nums[i]);
            min = Math.min(Math.min(temp * nums[i], min * nums[i]), nums[i]);
            if(max > result) {
                result = max;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        _152_MaxProductSubarray maxProductSubarray = new _152_MaxProductSubarray();
        int[] array = {2, 3, -2, 4};
        System.out.println(maxProductSubarray.maxProduct2(array));
    }
}
