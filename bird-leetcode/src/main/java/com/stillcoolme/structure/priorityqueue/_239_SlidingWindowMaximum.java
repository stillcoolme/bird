package com.stillcoolme.structure.priorityqueue;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Author: stillcoolme
 * Date: 2019/6/26 13:50
 * Description:
 *  there is a sliding window of size k which is moving from the very left of the array to the very right.
 *  You can only see the k numbers in the window.
 *  Each time the sliding window moves right by one position. Return the max sliding window.
 *  求该滑动窗口内最大的数
 *
 *  You may assume k is always valid, 1 ≤ k ≤ input array's size for non-empty array.
 *  Could you solve it in linear time?
 */
public class _239_SlidingWindowMaximum {

    /**
     * 用一个大顶堆来实现
     * 时间复杂度：N * logK
     * @param nums
     * @param k
     * @return
     */
    public int[] maxSlidingWindow(int[] nums, int k) {
        if(nums == null || nums.length < 2) return nums;
        int[] result = new int[nums.length - k + 1];
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(k, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            maxHeap.offer(nums[i]);
            if(i >= k - 1 || i == nums.length - 1){
                result[count ++] = maxHeap.peek();
                // 将窗口最后的数据出堆
                maxHeap.remove(nums[i - (k - 1)]);
            }

        }
        return result;
    }

    /**
     * 利用双端队列deque实现
     * 因为窗口大小恒定，且最大的只有一个，每次可以将窗口队列中其他小的都去掉，只留下一个最大的。
     * 用双向队列保存数字的下标，遍历整个数组，如果此时队列的首元素是 i-k 的话，表示此时窗口向右移了一步，则移除队首元素。
     * 一直比较队尾元素和将要进来的值，如果队列元素小的话就都移除，然后此时我们把队首元素加入结果中即可
     * 时间复杂度：N
     * @param nums
     * @param k
     * @return
     */
    // TODO
    public int[] maxSlidingWindow2(int[] nums, int k) {
        if(nums == null || nums.length < 2) return nums;
        int[] result = new int[nums.length - k + 1];
        // 双向队列 保存当前窗口最大值的数组下标，队列头的是最大值
        Deque<Integer> deque = new LinkedList();
        for (int i = 0; i < nums.length; i++) {
            // deque.getFirst() == i - k 说明窗口移动到了有效的位置！！！
            if(!deque.isEmpty() &&  deque.getFirst() == i - k){
                deque.pollFirst();
            }
            while(!deque.isEmpty() && nums[deque.getLast()] < nums[i]){
                    deque.pollLast();
            }
            // 在这里添加数组下标！！！
            deque.offerLast(i);
            if(i >= k - 1){
                result[i - k + 1] = nums[deque.peekFirst()];
            }
        }
        return result;

    }


    public static void main(String[] args) {
        int[] nums = new int[]{1, -1};
        _239_SlidingWindowMaximum slidingWindowMaximum = new _239_SlidingWindowMaximum();
        int[] result = slidingWindowMaximum.maxSlidingWindow2(nums, 1);
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i] + " ");
        }
    }
}
