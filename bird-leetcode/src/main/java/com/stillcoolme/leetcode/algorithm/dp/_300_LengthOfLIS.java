package com.stillcoolme.leetcode.algorithm.dp;

import java.util.Arrays;

/**
 * @author: stillcoolme
 * @date: 2019/8/5 18:58
 * @description:
 * 求 最长的上升子序列的长度
 * 输入: [10,9,2,5,3,7,101,18]
 * 输出: 4
 * 解释: 最长的上升子序列是 [2,3,7,101]，它的长度是 4。
 **/
public class _300_LengthOfLIS {

    /**
     * DP解法：44 ms
     * 选到第 n 个数的时候，前 n - 1 个数里面的 最长的上升子序列的长度 就已经是确定的了；
     * 所以它是有最优子结构的！！
     *
     * // 状态选择方程我写成这样真是傻B了，未知数 dp[i] 居然放到方程里
     * dp[i] = Math.max(dp[i], dp[j] + 1);
     *
     * @param nums
     * @return
     */
    public int lengthOfLIS(int[] nums) {
        if(nums == null || nums.length == 0){
            return 0;
        }
        // 状态的定义：dp[i] 是到第 i 个数与前面的数组成的最长上升子序列的**长度**
        int[] dp = new int[nums.length];
        dp[0] = 1;
        int maxValue = dp[0];
        for (int i = 1; i < nums.length; i++) {
            // 注意要赋值，不然就变成未知数了！！
            dp[i] = 0;
            // 找 dp[0] 到 dp[i - 1] 里面的最大值
            for (int j = 0; j < i; j++) {
                if(nums[i] > nums[j]) {
                    dp[i] = Math.max(dp[i], dp[j]);
                }
            }
            dp[i] = dp[i] + 1;
            maxValue = Math.max(maxValue, dp[i]);
        }
        return maxValue;
    }

    /**
     * 二分搜索进行优化，能想到二分搜索真是反人类的想法: 时间复杂度O(n log n)  耗时 3ms
     * 用 LIS数组 来存放 最长的上升子序列
     * 然后优化第二个for循环
     * 对于nums[j]
     * 如果它比 LIS数组中的数都大，那么直接添加到 LIS数组
     * 否则，通过二分搜索找到比它大的数的位置，然后替换掉那个数
     * @param nums
     * @return
     */
    public int lengthOfLIS2(int[] nums) {
        if(nums == null || nums.length == 0){
            return 0;
        }
        int[] LIS = new int[nums.length];
        LIS[0] = nums[0];
        int len = 1;
        for (int i = 1; i < nums.length; i++) {
            int index = Arrays.binarySearch(LIS, 0, len, nums[i]);
            if(index < 0) {
                index = - (index + 1);
            }
            LIS[index] = nums[i];
            if(index == len) {
                len ++;
            }
        }
        return len;
    }


    public static void main(String[] args) {
        //int[] array = new int[]{0};
        int[] array = new int[]{10,9,2,5,3,4};
        _300_LengthOfLIS lengthOfLIS = new _300_LengthOfLIS();
        System.out.println(lengthOfLIS.lengthOfLIS2(array));
    }

}
