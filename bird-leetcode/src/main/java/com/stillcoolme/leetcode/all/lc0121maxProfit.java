package com.stillcoolme.leetcode.all;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/5/17 19:33
 */
public class lc0121maxProfit {

    /**
     * 我是想遍历两次来弄的，但是看历史提交，遍历一次就好了
     * @param prices
     * @return
     */
    public int maxProfit(int[] prices) {
        int min = Integer.MAX_VALUE;
        int profile = 0;
        for (int i = 0; i < prices.length; i++) {
            if (prices[i] < min) {
                min = prices[i];
            }
            if ((prices[i] - min) > profile) {
                profile = prices[i] - min;
            }
        }
        return profile;
    }

    public static void main(String[] args) {
        lc0121maxProfit l121 = new lc0121maxProfit();
        int[] prices = new int[]{7,1,5,3,6,4};
        int xx = l121.maxProfit(prices);
        System.out.println(xx);
    }
}
