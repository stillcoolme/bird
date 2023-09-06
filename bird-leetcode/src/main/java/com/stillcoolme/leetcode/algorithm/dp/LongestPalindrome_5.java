package com.stillcoolme.leetcode.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/8/6 11:44
 * @description:
 *  求 最长回文子串
 *  给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为 1000。
 *  示例 1：
 *  输入: "babad"
 *  输出: "bab"
 *  注意: "aba" 也是一个有效答案。
 **/
public class LongestPalindrome_5 {

    /**
     * 解决这类问题的核心思想就是两个字“延伸”，具体来说
     * 1）如果一个字符串是回文串，那么在它左右分别加上一个相同的字符，那么它一定还是一个回文串
     * 2）如果一个字符串不是回文串，或者在回文串左右分别加不同的字符，得到的一定不是回文串
     * 事实上，上面的分析已经建立了大问题和小问题之间的关联， 基于此，我们可以建立动态规划模型。
     * 1）状态定义：dp[i][j] 表示 字符串中下标 i 到 j 的子字符串是否是回文子串
     * 2）状态选择方程：
     * if (s[i] == s[j] && dp[i + 1][j - 1]) {
     *   dp[i][j] = true;
     * }
     * 3）base case就是：
     * 一个字符（轴对称点是本身），或者两个字符（轴对称点是介于两者之间的虚拟点）。
     *
     * @param s
     * @return
     */
    public String longestPalindrome(String s) {
        if(s.length() < 1) return "";

        char[] chars = s.toCharArray();
        int length = chars.length;

        boolean[][] dp = new boolean[length][length];

        String result = "";
        for (int i = length - 1; i >= 0; i --) {
            for (int j = i; j < length; j ++) {
                if(j == i) {
                    dp[i][j] = true;
                } else if(j - i == 1 && chars[i] == chars[j]) {
                    dp[i][j] = true;
                } else if(dp[i + 1][j - 1] && chars[i] == chars[j]) {
                    dp[i][j] = true;
                }
                // 取符合的子字符串
                if (dp[i][j] && j - i + 1 > result.length()) {
                    result = s.substring(i, j + 1);
                }
            }
        }
        return result;
    }


    public static void main(String[] args) {
        LongestPalindrome_5 longestPalindrome = new LongestPalindrome_5();
        System.out.println(longestPalindrome.longestPalindrome("abcda"));
    }
}
