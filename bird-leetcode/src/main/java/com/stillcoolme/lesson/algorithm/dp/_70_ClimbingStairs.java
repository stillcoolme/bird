package com.stillcoolme.lesson.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/7/27 15:31
 * @description:
 *  爬楼梯，可以上一步或者两步
 **/
public class _70_ClimbingStairs {

    public int climbStairs(int n) {
        if(n == 0 || n == 1 || n == 2) return n;
        int[] mem = new int[n];
        mem[0] = 1;
        mem[1] = 2;
        for (int i = 2; i < n; i++) {
            mem[i] = mem[i - 1] + mem[i - 2];
        }
        return mem[n - 1];
    }

    public static void main(String[] args) {
        _70_ClimbingStairs climbingStairs = new _70_ClimbingStairs();
        System.out.println(climbingStairs.climbStairs(3));
    }

}
