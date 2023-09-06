package com.stillcoolme.leetcode.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/8/5 8:11
 * @description:
 *  设计一个算法来计算你所能获取的最大利润。你可以尽可能地完成更多的交易（多次买卖一支股票）。
 *  即 k = +infinity
 **/
public class StockWithKInfinity_122 {

    /**
     * DP 解法：
     * 如果 k 为正无穷，那么就可以认为 k 和 k - 1 是一样的。可以这样改写状态选择方程：
     * mp[i][k][0] = max(mp[i-1][k][0], mp[i-1][k][1] + prices[i-1])
     * mp[i][k][1] = max(mp[i-1][k][1], mp[i-1][k-1][0] - prices[i-1])
     *             = max(mp[i-1][k][1], mp[i-1][k][0] - prices[i-1])
     * 我们发现数组中的 k 已经不会改变了，也就是说不需要记录 k 这个状态了！！
     *
     * @param prices
     * @return
     */
    public int maxProfit(int[] prices) {
        if(prices.length < 1) {
            return 0;
        }
        int[][] mp = new int[prices.length + 1][2];
        mp[0][0] = 0;
        mp[0][1] = Integer.MIN_VALUE;
        for (int i = 1; i < prices.length + 1; i++) {
            mp[i][0] = Math.max(mp[i - 1][0], mp[i - 1][1] + prices[i - 1]);
            mp[i][1] = Math.max(mp[i - 1][1], mp[i - 1][0] - prices[i - 1]);
        }
        return mp[prices.length][0];
    }

    public static void main(String[] args) {
        int[] prices = {7,1,5,3,6,4};
        StockWithKInfinity_122 stock2 = new StockWithKInfinity_122();
        System.out.println(stock2.maxProfit(prices));
    }
}
