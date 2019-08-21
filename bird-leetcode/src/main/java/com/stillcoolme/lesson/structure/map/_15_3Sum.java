package com.stillcoolme.lesson.structure.map;

import java.util.*;

/**
 * @author: create by stillcoolme
 * @date: 2019/6/30 10:22
 * @description:
 * 高频题，看是否有3个数符合 x+y+z=0
 **/
public class _15_3Sum {

    /**
     * 解法：转换成 x + y = -z ，然后用两个循环去找
     * 未解决
     * @param nums
     * @return
     */
    public List<List<Integer>> threeSum(int[] nums) {
        if(nums.length < 3) return null;
        List<List<Integer>> returnList = new ArrayList();
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            set.add(nums[i]);
        }
        // 这两重循环会导致结果重复啊。。
        for (int i = 0; i < nums.length - 1; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                int complement = nums[i] + nums[j];
                if(set.contains(-complement)){
                    List list = Arrays.asList(new int[]{nums[i], nums[j], -complement});
                    set.remove(-complement);
                    returnList.add(list);
                }
            }
        }
        return returnList;
    }

    /**
     * 解法二：先排序，再两边往中间夹查找。用这办法要对数组排序，就改变了给的数据好像不太好。
     * 使用了两个循环，时间复杂度O(N^2)
     * @param nums
     * @return
     */
    public List<List<Integer>> threeSum2(int[] nums) {
        List<List<Integer>> returnList = new ArrayList();
        if(nums == null || nums.length < 3) return returnList;
        Arrays.sort(nums);
        for (int i = 0; i < nums.length - 2; i++) {
            if (i == 0 || i > 0 && nums[i] != nums[i-1]) {//nums[i] != nums[i-1] 去重
                int left = i + 1;
                int right = nums.length - 1;
                while(left < right){
                    int sum = nums[i] + nums[left] + nums[right];
                    if(sum == 0){
                        List list = Arrays.asList(nums[i], nums[left], nums[right]);
                        returnList.add(list);
                        // 去重
                        while(left < right && nums[left] == nums[left + 1]) left ++;
                        while(left < right && nums[right] == nums[right - 1]) right --;
                        left ++;
                        right --;
                    } else if(sum > 0) {   // sum太大了，右边向左边移
                        // 去重
                        while(left < right && nums[right] == nums[right - 1]) right --;
                        right --;
                    } else {     // sum太小了，左边往右边移
                        // 去重
                        while(left < right && nums[left] == nums[left + 1]) left ++;
                        left ++;
                    }
                }
            }
        }
        return returnList;
    }


    public static void main(String[] args) {
        _15_3Sum sum = new _15_3Sum();
        List<List<Integer>> list = sum.threeSum2(new int[]{-1, 0, 1, 2, -1, -4});
    }
}
