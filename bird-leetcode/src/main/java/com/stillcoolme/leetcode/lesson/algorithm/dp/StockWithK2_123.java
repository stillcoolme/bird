package com.stillcoolme.leetcode.lesson.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/8/5 8:16
 * @description:
 *  设计一个算法来计算你所能获取的最大利润。你最多可以完成 两笔 交易。
 *  即 k = 2
 **/
public class StockWithK2_123 {

    /**
     * DP
     * @param prices
     * @return
     */
    public int maxProfit(int[] prices) {
        if(prices.length < 1) {
            return 0;
        }

        int[][][] mp = new int[prices.length + 1][3][2];

        for (int k = 1; k <= 2; k++) {
            // 因为 i 是从 1 开始的，所以 i = 0 意味着还没有开始，这时候的利润当然是 0 。
            mp[0][k][0] = 0;
            // 还没开始的时候，是不可能持有股票的，用负无穷表示这种不可能。
            mp[0][k][1] = Integer.MIN_VALUE;
        }
        for (int i = 0; i < prices.length + 1; i++) {
            // 因为 k 是从 1 开始的，所以 k = 0 意味着根本不允许交易，这时候利润当然是 0 。
            mp[i][0][0] = 0;
            // 不允许交易的情况下，是不可能持有股票的，用负无穷表示这种不可能。
            mp[i][0][1] = Integer.MIN_VALUE;
        }

        for (int i = 1; i < prices.length + 1; i++) {
            for (int k = 1; k <= 2; k++) {
                mp[i][k][0] = Math.max(mp[i - 1][k][0], mp[i - 1][k][1] + prices[i - 1]);
                mp[i][k][1] = Math.max(mp[i - 1][k][1], mp[i - 1][k - 1][0] - prices[i - 1]);
            }
        }
        return mp[prices.length][2][0];

    }

    public static void main(String[] args) {
        int[] prices = {7,6,4,3,1};
        StockWithK2_123 stock3 = new StockWithK2_123();
        System.out.println(stock3.maxProfit(prices));

    }
}
