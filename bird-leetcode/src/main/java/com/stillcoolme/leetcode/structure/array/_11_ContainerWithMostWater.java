package com.stillcoolme.leetcode.structure.array;

/**
 * @author: stillcoolme
 * @date: 2019/9/4 20:36
 * @description:
 * 给定 n 个非负整数 a1，a2，...，an，每个数代表坐标中的一个点 (i, ai) 。
 * 在坐标内画 n 条垂直线，垂直线 i 的两个端点分别为 (i, ai) 和 (i, 0)。
 * 找出其中的两条线，使得它们与 x 轴共同构成的容器可以容纳最多的水。
 * 而且 n > 2
 *
 */
public class _11_ContainerWithMostWater {

    /**
     * 我的暴力法，时间复杂度 O(n*n)，耗时 490 ms
     * @param height
     * @return
     */
    public int maxArea(int[] height) {
        int maxarea = 0;
        int left = 0;
        int right = 0;
        for (int i = 0; i < height.length - 1; i++) {
            for (int j = i + 1; j < height.length; j++) {
                int min = Math.min(height[i], height[j]);
                left = i;
                right = j;
                int capacity = min * (right - left);
                maxarea = capacity > maxarea ? capacity : maxarea;
            }
        }
        return maxarea;
    }

    /**
     * 别人的双指针法，时间复杂度 O(n)，耗时 5ms
     * 我这样写也用了太多变量了吧！！！
     *
     * 该方法思路在于：两线段之间形成的区域总是会受到其中较短那条长度的限制。此外，两线段距离越远，得到的面积就越大。
     * 在由线段长度构成的数组中使用两个指针，一个放在开始，一个置于末尾。
     * 并将指向较短线段的指针向较长线段那端移动一步。
     * 面积减少1，但是高度能增加。
     * @param height
     * @return
     */
    public int maxArea2(int[] height) {
        int maxarea = 0;
        int left = 0;
        int right = height.length - 1;
        int min = 0;
        int tempLeft = 0;
        int tempRight = right;
        while(left < right) {
            if(height[left] > height[right]) {
                min = height[right];
                tempRight = right - 1;
            } else {
                min = height[left];
                tempLeft = left + 1;
            }
            int capacity = min * (right - left);
            maxarea = capacity > maxarea ? capacity : maxarea;

            right = tempRight;
            left = tempLeft;
        }
        return maxarea;
    }

    /**
     * 精简变量
     * @param height
     * @return
     */
    public int maxArea3(int[] height) {
        int maxarea = 0, left = 0, right = height.length - 1;
        while(left < right) {
            maxarea = Math.max(maxarea, Math.min(height[left], height[right]) * (right - left));
            if(height[left] > height[right]) {
                right -= 1;
            } else {
                left += 1;
            }
        }
        return maxarea;
    }

    public static void main(String[] args) {
        int[] pool = new int[]{1,8,6,2,5,4,8,3,7};
        _11_ContainerWithMostWater containerWithMostWater = new _11_ContainerWithMostWater();
        System.out.println(containerWithMostWater.maxArea3(pool));
    }
}
