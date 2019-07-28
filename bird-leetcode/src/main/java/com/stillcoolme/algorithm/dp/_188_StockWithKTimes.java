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
     * 3）还有个条件：买卖股票的次数最多为k次，需要再多一维来记录已经交易了多少次
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
        return 0;
    }
}
