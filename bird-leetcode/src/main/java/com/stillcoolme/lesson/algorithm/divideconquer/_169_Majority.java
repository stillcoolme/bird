package com.stillcoolme.lesson.algorithm.divideconquer;

import java.util.Arrays;

/**
 * Authir: stillcoolme
 * Date: 2019/7/1 19:47
 * Description:
 *  求数组中出现次数 超过 n/2 次的数，且一定会出现。
 */
public class _169_Majority {

    /**
     * 解法1：看到计数的很容易我们就能想到用Map来
     * 1. 先将元素按<元素，出现次数>放进Map中
     * 2. 再直接find 出现次数 超过 n/2 次的元素
     * @param nums
     * @return
     */

    /**
     * 解法2：先排序
     * 从头到尾看那个数是重复超过 n/2 次
     * @param nums
     * @return
     */
    public int majorityElement(int[] nums) {
        Arrays.sort(nums);
        int count = 1;
        for (int i = 1; i < nums.length; i++) {
            if(nums[i - 1] == nums[i]){
                count ++;
                if(count > nums.length / 2){
                    return nums[i];
                }
            } else {
                count = 1;
            }
        }
        return nums[0];
    }

    /**
     * 解法3：先排序
     * 无论众数是多少，返回 2/n 下标对应的值都是正确的。
     * 时间复杂度：O(nlgn)。就是排序的时间复杂度
     * @param nums
     * @return
     */
    // TODO
    public int majorityElement2(int[] nums) {
        Arrays.sort(nums);
        return nums[nums.length / 2];
    }

    /**
     * 解法4：分治
     *
     * 时间复杂度：O(nlgn)
     * @param nums
     * @return
     */
    public int majorityElement3(int[] nums) {
        return majorityElement3Impl(nums, 0, nums.length - 1);
    }

    private int majorityElement3Impl(int[] nums, int lo, int hi) {
        if(lo == hi){
            return nums[lo];
        }
        int mid = (hi - lo) / 2 + lo;
        // 分治得到左边出现次数最多的数
        int left = majorityElement3Impl(nums, lo, mid);
        // 分治得到右边出现次数最多的数
        int right = majorityElement3Impl(nums, mid + 1, hi);
        if(left == right){
            return left;
        }
        // 如果 左右两边出现的不是同一个数，再去全部计算一遍
        int leftCount = countThisNumber(nums, left, lo, hi);
        System.out.println(left + " count：" + leftCount);
        int rightCount = countThisNumber(nums, right, lo, hi);
        System.out.println(right + " count：" + rightCount);
        return leftCount > rightCount ? left : right;
    }

    private int countThisNumber(int[] nums, int target, int lo, int hi) {
        int count = 0;
        //  j <= hi
        for (int j = lo; j <= hi; j++) {
            if(nums[j] == target){
                count ++;
            }
        }
        return count;
    }


    public static void main(String[] args) {
        int[] nums = new int[]{6, 5, 5};
        _169_Majority majority = new _169_Majority();
        int result = majority.majorityElement3(nums);
        System.out.println(result);
    }
}
