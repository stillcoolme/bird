package com.stillcoolme.structure.map;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: create by stillcoolme
 * @date: 2019/6/30 9:28
 * @description:
 **/
public class _1_TwoSum {

    /**
     * 解法1：两遍哈希表。
     * 为了对运行时间复杂度进行优化，我们需要一种更有效的方法来检查数组中是否存在目标元素。
     * 如果存在，我们需要找出它的索引。
     * 保持数组中的每个元素与其索引相互对应的最好方法是什么？哈希表。
     *
     * @param nums
     * @param target
     * @return
     */
    public int[] twoSum(int[] nums, int target) {
        if(nums == null){
            return null;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            // 这里这样put，如果有两个数组元素相同会覆盖，但是下面的for循环用数组来循环就没事
            map.put(nums[i], i);
        }
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if(map.containsKey(complement) && map.get(complement) != i){
                return new int[]{i, map.get(complement)};
            }
        }
        return null;
    }

    /**
     * 解法1：一遍哈希表。
     * 1. 在进行迭代并将元素插入到表前，先检查表中是否已经存在当前元素所对应的目标元素；
     * 2. 如果它存在，那我们已经找到了对应解，并立即将其返回。
     * 时间复杂度：O(n)， 把包含有 n 个元素的列表遍历两次。由于哈希表将查找时间缩短到 O(1) ，所以时间复杂度为 O(1) * O(n)。
     * 空间复杂度：O(n)， 所需的额外空间取决于哈希表中存储的元素数量，该表中存储了 n 个元素。
     * @param nums
     * @param target
     * @return
     */
    public int[] twoSum2(int[] nums, int target) {
        if(nums == null){
            return null;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement) && map.get(complement) != i) {
                return new int[]{i, map.get(complement)};
            }
            map.put(nums[i], i);
            // 要是加了else，用的时间比解法二时间还长。。。。
            /*else {
                map.put(nums[i], i);
            }*/
        }
        return null;
    }

    public static void main(String[] args) {
        _1_TwoSum twoSum = new _1_TwoSum();
        twoSum.twoSum(new int[]{3, 3}, 6);
    }
}
