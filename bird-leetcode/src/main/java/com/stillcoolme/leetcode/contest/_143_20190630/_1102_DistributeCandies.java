package com.stillcoolme.leetcode.contest._143_20190630;

/**
 * @author: create by stillcoolme
 * @date: 2019/6/30 10:45
 * @description: 给小朋友发糖果 1，2，3....
 **/
public class _1102_DistributeCandies {

    public int[] distributeCandies(int candies, int num_people) {
        int[] result = new int[num_people];
        int distributeNum = 1;
        while(candies > 0){
            for (int i = 0; i < num_people; i++) {
                if(candies - distributeNum < 0) {
                    distributeNum = candies;
                }
                candies -= distributeNum;
                result[i] += distributeNum;
                distributeNum += 1;
                if(candies <= 0){
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 不要while了，直接for candies
     * @param candies
     * @param num_people
     * @return
     */
    public int[] distributeCandies2(int candies, int num_people) {
        int[] result = new int[num_people];
        for (int i = 1; i < candies; i++) {
            result[(i - 1) % num_people] = Math.min(i, candies);
            candies = Math.max(i, candies);
        }
        return result;
    }

    public static void main(String[] args) {
        _1102_DistributeCandies test = new _1102_DistributeCandies();
        int[] a = test.distributeCandies(10, 3);
    }
}
