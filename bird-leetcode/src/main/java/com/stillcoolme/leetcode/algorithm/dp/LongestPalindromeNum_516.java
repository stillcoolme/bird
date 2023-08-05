package com.stillcoolme.leetcode.algorithm.dp;

/**
 * @author: stillcoolme
 * @date: 2019/8/18 9:21
 * @description:
 *  求 最长回文子串长度
 **/
class LongestPalindromeNum_516 {

    /**
     * dp[i][j] 记录 i 到 j 这段子字符串之间的最长回文子串长度
     *
     * 解决这类问题的核心思想就是两个字“延伸”，具体来说
     * 1）如果一个字符串是回文串，那么在它左右分别加上一个相同的字符，那么它一定还是一个回文串，因此回文长度增加2
     * 2）如果一个字符串不是回文串，或者在回文串左右分别加不同的字符，得到的一定不是回文串,因此回文长度不变，我们取[i][j-1]和[i+1][j]的较大值
     *
     * 与第5题不同的在于，这种是 dp[i][j] 记录的是由底层传上来的数，所以如果不符合数就不会变的，
     * 就不用像第5题那样在第2个if中：```else if(j - i == 1 && chars[i] == chars[j]) {``` 加上对 ```j - i == 1```的判断
     * 判断说这是那种中间一个数的情况。
     *
     * @param s
     * @return
     */
    public int longestPalindromeSubseq(String s) {
        if(s.length() < 1) return 0;
        char[] chars = s.toCharArray();
        int length = chars.length;
        int[][] dp = new int[length][length];
        for (int i = length - 1; i >= 0; i --) {
            for (int j = i; j < length; j ++) {
                if(i == j) {
                    dp[i][j] = 1;
                } else if(chars[i] == chars[j]) {
                    dp[i][j] = dp[i + 1][j - 1] + 2;
                } else {
                    dp[i][j] = Math.max(dp[i][j - 1], dp[i + 1][j]);
                }
            }
        }
        return dp[0][length - 1];
    }

    public static void main(String[] args) {
        LongestPalindromeNum_516 longestPalindromeNum_516 = new LongestPalindromeNum_516();
        System.out.println(longestPalindromeNum_516.longestPalindromeSubseq("abba"));
    }

}
