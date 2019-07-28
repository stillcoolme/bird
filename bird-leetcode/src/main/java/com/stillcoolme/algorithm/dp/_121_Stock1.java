package com.stillcoolme.algorithm.dp;

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
 * 最大的利润为：6-1 = 5（不是7-1 = 6，因为销售价格需要比购买价格大）
 **/
public class _121_Stock1 {

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

    public int maxProfit2(int[] prices) {
        int[][][] mp = new int[prices.length][3][2];
        mp[0][0][0] = 0;
        mp[0][0][1] = prices[0];
        for (int i = 1; i < prices.length; i++) {
            for (int k = 0; k < 2; k++) {
                // 第i天没有股票
                mp[i][k][0] = Math.max(mp[i][k][0], mp[i][k + 1][1] + prices[i]);
                // 第i天有股票
                mp[i][k][1] = Math.max(mp[i][k][1], mp[i][k + 1][0] - prices[i]);
            }
        }
        return mp[prices.length - 1][1][0];
    }

    public static void main(String[] args) {
        int[] prices = {7,1,5,3,6,4};
        _121_Stock1 stock = new _121_Stock1();
        System.out.println(stock.maxProfit2(prices));
    }
}
