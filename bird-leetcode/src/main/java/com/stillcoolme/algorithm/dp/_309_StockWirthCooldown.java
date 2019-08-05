package com.stillcoolme.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/8/5 8:40
 * @description:
 *  在满足以下约束条件下，你可以尽可能地完成更多的交易（多次买卖一支股票）:
 *  1. 你不能同时参与多笔交易（你必须在再次购买前出售掉之前的股票）。
 *  2. 卖出股票后，你无法在第二天买入股票 (即冷冻期为 1 天)。
 *  即 k = +infinity 并且有冷冻期
 **/
public class _309_StockWirthCooldown {

    /**
     * DP解法：
     * 每次 sell 之后要等一天才能继续交易。
     * 只要把这个特点融入到 122题的 k = +infinity 的状态转移方程即可
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
            if(i == 1) {
                mp[i][1] = Math.max(mp[i - 1][1], - prices[i - 1]);
            } else {
                mp[i][1] = Math.max(mp[i - 1][1], mp[i - 2][0] - prices[i - 1]);
            }
        }
        return mp[prices.length][0];
    }


    public static void main(String[] args) {
        int[] prices = {1,2,3,0,2};
        _309_StockWirthCooldown stockWirthCooldown = new _309_StockWirthCooldown();
        System.out.println(stockWirthCooldown.maxProfit(prices));
    }

}
