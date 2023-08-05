package com.stillcoolme.leetcode.structure.priorityqueue;

import java.util.PriorityQueue;

/**
 * Author: stillcoolme
 * Date: 2019/7/5 18:32
 * Description:
 * 得到不断流入的数据中前第K大的数据
 */
class _703_KthLargest {
    int k;
    // 一个基于优先级堆的无界优先级队列，不指定Comparator时默认为最小堆，大小为11
    PriorityQueue<Integer> minHeap;

    public _703_KthLargest(int k, int[] nums) {
        this.k = k;
        this.minHeap = new PriorityQueue(k);
        for (int i = 0; i < nums.length; i++) {
            add(nums[i]);
        }
    }

    /**
     * 通过对java自带的优先队列进行操作，
     * 队列小于k时不断插入，
     * 大于k时，判断是否大于最小的，大于则入队，java实现了调整算法
     * 然后返回队列头就是第k大的数
     * 时间复杂度：n * log n 。  n是n个元素进来，log n 则是调整元素的时间
     * @param val
     * @return
     */
    // TODO 自己实现优先队列
    public int add(int val) {
        if(minHeap.size() < k){
            minHeap.offer(val);
        } else if(minHeap.peek() < val){
            // 将第一个元素取出
            minHeap.poll();
            // 添加元素
            minHeap.offer(val);
        }
        return minHeap.peek();
    }

    public static void main(String[] args) {
        int[] nums = new int[]{2, 4, 5, 1};
        _703_KthLargest obj = new _703_KthLargest(3, nums);
        int result = obj.add(6);
        System.out.println(result);
    }
}

