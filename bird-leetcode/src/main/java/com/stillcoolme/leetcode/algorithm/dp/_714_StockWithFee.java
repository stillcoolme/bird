package com.stillcoolme.leetcode.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/8/5 13:31
 * @description:
 *  你可以无限次地完成交易，但是你每次交易都需要付手续费。
 *  如果你已经购买了一个股票，在卖出它之前你就不能再继续购买股票了。
 **/
public class _714_StockWithFee {

    public int maxProfit(int[] prices, int fee) {
        if(prices.length < 1) {
            return 0;
        }
        int[][] mp = new int[prices.length + 1][2];
        mp[0][0] = 0;
        mp[0][1] = Integer.MIN_VALUE;
        for (int i = 1; i < prices.length + 1; i++) {
            mp[i][0] = Math.max(mp[i - 1][0], mp[i - 1][1] + prices[i - 1]);
            mp[i][1] = Math.max(mp[i - 1][1], mp[i - 1][0] - prices[i - 1] - fee);
        }
        return mp[prices.length][0];
    }

    public static void main(String[] args) {
        int[] prices = {1, 3, 2, 8, 4, 9};
        _714_StockWithFee stockWithFee = new _714_StockWithFee();
        System.out.println(stockWithFee.maxProfit(prices, 2));
    }
}
