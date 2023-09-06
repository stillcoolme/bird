package com.stillcoolme.leetcode.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/7/28 11:50
 * @description:
 * 你有一个由每天的股票价格组成的数组。
 * 如果你只能进行一次交易（比如购买或者销售一个股票），设计一个算法来获取最大利润。
 * 注意你不能在买入股票前卖出股票。
 * 例子1：
 * Input: [7, 1, 5, 3, 6, 4]
 * Output: 5
 * 最大的利润为：6 - 1 = 5（不是 7 - 1 = 6，因为销售价格需要比购买价格大）
 **/
public class Stock1_121 {

    /**
     * 暴力单循环即可
     * @param prices
     * @return
     */
    public int maxProfit(int[] prices) {
        int min = Integer.MAX_VALUE;
        int profile = 0;
        for (int i = 0; i < prices.length; i++) {
            if(prices[i] < min) min = prices[i];
            if(prices[i] - min > profile) profile = prices[i] - min;
        }
        return profile;
    }

    /**
     * DP
     * @param prices
     * @return
     */
    public int maxProfit2(int[] prices) {
        // 从1 开始计算天数，这样方便定义base，对应的价格为prices[i - 1]
        // k也是从下标 1 开始算比较方便
        int[][][] mp = new int[prices.length + 1][2][2];

        // 定义base
        // i = 0 ，即第 0 天还没开始，这时不持有股票利润当然就是 0
        mp[0][0][0] = 0;
        mp[0][1][0] = 0;
        // 还没开始的时候，是不可能持有股票的，用负无穷表示这种不可能。
        mp[0][0][1] = Integer.MIN_VALUE;
        mp[0][1][1] = Integer.MIN_VALUE;

        for (int i = 1; i < prices.length + 1; i++) {
            for (int k = 1; k <= 1; k++) {
                // 第i天没有股票
                mp[i][k][0] = Math.max(mp[i - 1][k][0], mp[i - 1][k][1] + prices[i - 1]);
                // 第i天有股票
                mp[i][k][1] = Math.max(mp[i - 1][k][1], mp[i - 1][k - 1][0] - prices[i - 1]);
            }
        }
        return mp[prices.length][1][0];
    }

    /**
     * 简单优化一下，上面的for循环里面，k都是等于1的
     *  // 第i天没有股票
     *  mp[i][1][0] = Math.max(mp[i - 1][1][0], mp[i - 1][1][1] + prices[i - 1]);
     *  // 第i天有股票
     *  mp[i][1][1] = Math.max(mp[i - 1][1][1], mp[i - 1][0][0] - prices[i - 1]);
     *  而且可知道 mp[i - 1][0][0] 等于 0
     *  所以状态选择方程和 k 的值无关了
     *
     * @param prices
     * @return
     */
    public int maxProfit3(int[] prices) {
        int[][] mp = new int[prices.length + 1][2];
        // 定义base
        // i = 0 ，即第 0 天还没开始，这时不持有股票利润当然就是 0
        mp[0][0] = 0;
        // 还没开始的时候，是不可能持有股票的，用负无穷表示这种不可能。
        mp[0][1] = Integer.MIN_VALUE;
        for (int i = 1; i < prices.length + 1; i++) {
            for (int k = 1; k <= 1; k++) {
                // 第i天没有股票
                mp[i][0] = Math.max(mp[i - 1][0], mp[i - 1][1] + prices[i - 1]);
                // 第i天有股票
                mp[i][1] = Math.max(mp[i - 1][1],  - prices[i - 1]);
            }
        }
        return mp[prices.length][0];
    }

    public static void main(String[] args) {
        int[] prices = {7,1,5,3,6,4,90};
        Stock1_121 stock = new Stock1_121();
        System.out.println(stock.maxProfit3(prices));
    }
}
