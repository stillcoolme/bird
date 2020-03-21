package com.stillcoolme.leetcode.lesson.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/8/6 20:01
 * @description:
 * 给定两个单词 word1 和 word2，计算出将 word1 转换成 word2 所使用的最少操作数 。
 * 你可以对一个单词进行如下三种操作：
 * 插入一个字符
 * 删除一个字符
 * 替换一个字符
 * 示例 1:
 * 输入: word1 = "horse", word2 = "ros"
 * 输出: 3
 * 解释:
 * horse -> rorse (将 'h' 替换为 'r')
 * rorse -> rose (删除 'r')
 * rose -> ros (删除 'e')
 *
 **/
public class _72_EditDistance {

    /**
     * DP:
     * 状态定义：又是一维不够的情况；
     * dp[i][j] 表示 word1 的前i个字符，替换 word2 的前j个字符需要变动的次数；
     * 状态选择方程：
     * 1) 如果 word1 的第i个字符 等于 word2 的第j个字符，那么 dp[i][j] = dp[i - 1][j - 1]
     * 2）如果 word1 的第i个字符 不等于 word2 的第j个字符，那么可以有3种操作(删除word1的，删除word2，替换的)
     *  这三种操作分别表现为
     *  dp[i - 1][j] + 1,
     *  dp[i][j - 1] + 1,
     *  dp[i - 1][j - 1] + 1，
     *  选最小的：
     *  dp[i][j] = min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]) + 1
     *
     * @param word1
     * @param word2
     * @return
     */
    public int minDistance(String word1, String word2) {
        char[] char1 = word1.toCharArray();
        int n = char1.length;
        char[] char2 = word2.toCharArray();
        int m = char2.length;
        int[][] dp = new int[n + 1][m + 1];
        // base定义
        for (int i = 0; i < n + 1; i++) {
            // word2不动，一直对word1进行操作
            dp[i][0] = i;
        }
        for (int j = 0; j < m + 1; j++) {
            // word1不动，一直对word2进行操作
            dp[0][j] = j;
        }
        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                if(char1[i - 1] == char2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1])) + 1;
                }
            }
        }
        return dp[n][m];
    }

    public static void main(String[] args) {
        _72_EditDistance editDistance = new _72_EditDistance();
        System.out.println(editDistance.minDistance("intention", "execution"));
    }
}
