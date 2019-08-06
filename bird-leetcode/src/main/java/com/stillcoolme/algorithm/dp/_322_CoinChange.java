package com.stillcoolme.algorithm.dp;

import java.util.Arrays;

/**
 * @author: stillcoolme
 * @date: 2019/8/6 8:33
 * @description:
 *  给定不同面额的硬币 coins 和一个总金额 amount。
 *  编写一个函数来计算可以凑成总金额所需的最少的硬币个数。
 *  如果没有任何一种硬币组合能组成总金额，返回 -1。
 *  每个硬币都可以选多个。
 *  示例 1:
 *  输入: coins = [1, 2, 5], amount = 11
 *  输出: 3
 *  解释: 11 = 5 + 5 + 1
 **/
public class _322_CoinChange {

    /**
     * DP
     * 思路：转换成上楼梯问题：dp[i] 表示上到 i 阶楼梯，最少的步数
     * 那么，这题的 dp[i] 表示组成到 i 元面值，组成的最少硬币数
     * @param coins
     * @param amount
     * @return
     */
    public int coinChange(int[] coins, int amount) {
        if(amount < 1) {
            return 0;
        }
        int[] dp = new int[amount + 1];
        // 填充为超过 amount + 1 个硬币数
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int i = 1; i <= amount; i++) {
            for (int j = 0; j < coins.length; j++) {
                // i表示要拼凑的总金额，coins[j] <= i 当前使用的coin 小于 要拼凑的总金额
                if(coins[j] <= i) {
                    dp[i] = Math.min(dp[i], dp[i - coins[j]] + 1);
                }
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    public static void main(String[] args) {
        int[] coins = new int[]{1, 2, 5};
        int amount = 11;
        _322_CoinChange coinChange = new _322_CoinChange();
        System.out.println(coinChange.coinChange(coins, amount));
    }
}
