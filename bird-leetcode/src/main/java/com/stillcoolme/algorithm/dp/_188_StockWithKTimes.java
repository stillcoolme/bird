package com.stillcoolme.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/7/28 15:05
 * @description:
 * 设计一个算法来计算股票交易中你所能获取的最大利润。
 * 你最多可以完成 k 笔交易。
 * 注意: 你必须在再次购买前出售掉之前的股票。
 * 示例 1:
 * 输入: [2,4,1], k = 2
 * 输出: 2
 * 解释: 在第 1 天 (股票价格 = 2) 的时候买入，在第 2 天 (股票价格 = 4) 的时候卖出，这笔交易所能获得利润 = 4-2 = 2 。
 **/
public class _188_StockWithKTimes {

    /**
     * 思路：
     * 1）定义 mp[i] 表示 在 i 天的最大利润(max profile)
     *  i天可能买卖股票，于是得到状态转移方程:
     *      mp[i] = mp[i - 1] - a[i]   // 在 i 天买股票a[i]， 就 - a[i]
     *      mp[i] = mp[i - 1] + a[i]   // 在 i 天卖股票a[i]， 就加上a[i]
     * 2）要进行买卖，有个条件：买股票前手里得没股票，卖股票前手里得有股票
     *  这样 mp[i] 一维数组没法记录这额外的是否有股票的信息，丢失了这天有没股票的信息。
     *  所以增加一个维度，定义 mp[i][j],
     *  i 的范围是0 到 n-1 天， j 的范围是 0 或 1 表示 没有股票 或 有1份股票，
     *  于是得到状态转移方程:
     *      mp[i, 0] 就是 i 天 没有股票，就是 没有操作 或者 卖出了：
     *      mp[i, 0] =  Max - | mp[i - 1, 0]          // i 天没有操作
     *                        | mp[i - 1, 1] + a[i]   // i 天卖出了股票
     *      mp[i, 1] 就是 i 天 持有了股票，就是 没有操作 或者 卖出了：
     *      mp[i, 1] =  Max - | mp[i - 1, 1]          // i 天没有操作
     *                        | mp[i - 1, 0] - a[i]   // i 天买入了股票
     * 3）还有个条件：交易股票的次数最多为k次（买一次股票记为交易一次），需要再多一维来记录已经交易了多少次
     *  定义 mp[i][k][j] k的范围是 0 - k 次
     *  于是得到状态转移方程：
     *      mp[i, k, 0] 就是 i 天 没有股票，就是 没有操作 或者 卖出了：
     *      mp[i, k, 0] =  Max - | mp[i - 1, k, 0]          // i 天没有操作
     *                           | mp[i - 1, k - 1, 1] + a[i]   // i 天卖出了股票
     *      mp[i, k, 1] 就是 i 天 持有了股票，就是 没有操作 或者 卖出了：
     *      mp[i, k, 1] =  Max - | mp[i - 1, k, 1]          // i 天没有操作
     *                        | mp[i - 1, k - 1, 0] - a[i]   // i 天买入了股票
     *
     * @param k
     * @param prices
     * @return
     */
    public int maxProfit(int k, int[] prices) {
        if(k < 1) {
            return 0;
        }
        // 一次交易由买入和卖出构成，至少需要两天。
        // 所以说有效的限制 k 应该不超过 n/2，如果超过，就没有约束作用了，相当于 k = +infinity。
        if(k > prices.length / 2) {
            int[][] mp = new int[prices.length + 1][2];
            mp[0][0] = 0;
            mp[0][1] = Integer.MIN_VALUE;
            for (int i = 1; i < prices.length + 1; i++) {
                mp[i][0] = Math.max(mp[i - 1][0], mp[i - 1][1] + prices[i - 1]);
                mp[i][1] = Math.max(mp[i - 1][1], mp[i - 1][0] - prices[i - 1]);
            }
            return mp[prices.length][0];
        }
        // 从1 开始计算天数，这样方便定义base，对应的价格为prices[i - 1]
        // k也是从下标 1 开始算比较方便
        int[][][] mp = new int[prices.length + 1][k + 1][2];
        // 定义base
        for (int kk = 1; kk <= k; kk++) {
            // 因为 i 是从 1 开始的，所以 i = 0 意味着还没有开始，这时候的利润当然是 0 。
            mp[0][kk][0] = 0;
            // 还没开始的时候，是不可能持有股票的，用负无穷表示这种不可能。
            mp[0][kk][1] = Integer.MIN_VALUE;
        }
        for (int i = 0; i < prices.length + 1; i++) {
            // 因为 k 是从 1 开始的，所以 k = 0 意味着根本不允许交易，这时候利润当然是 0 。
            mp[i][0][0] = 0;
            // 不允许交易的情况下，是不可能持有股票的，用负无穷表示这种不可能。
            mp[i][0][1] = Integer.MIN_VALUE;
        }
        for (int i = 1; i < prices.length + 1; i++) {
            for (int kk = k; kk >= 1; kk--) {
                // 第i天没有股票
                mp[i][kk][0] = Math.max(mp[i - 1][kk][0], mp[i - 1][kk][1] + prices[i - 1]);
                // 第i天有股票
                mp[i][kk][1] = Math.max(mp[i - 1][kk][1], mp[i - 1][kk - 1][0] - prices[i - 1]);
                System.out.println(i + " " + kk + " " + mp[i][kk][0]);
            }
        }
        return mp[prices.length][k][0];
    }

    public static void main(String[] args) {
        // 测试用例1： int[] prices = {2, 4, 1};  k = 2
        // 测试用例2:  int[] prices = {1,2,4,2,5,7,2,4,9,0};  k = 4
        int[] prices = {1,2,4,2,5,7,2,4,9,0};
        _188_StockWithKTimes stockWithKTimes = new _188_StockWithKTimes();
        System.out.println(stockWithKTimes.maxProfit(4, prices));
    }
}
