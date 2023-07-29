package com.stillcoolme.leetcode.all;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/5/20 15:20
 */
public class lc2327fixTime {
    public int countTime(String time) {
        char queestionMark = '?';

        int count = 1;
        // 判断前两位
        if (time.charAt(0) == queestionMark && time.charAt(1) == queestionMark) {
            count = 2 * 10 + 4;
        } else if (time.charAt(0) == queestionMark) {
            Integer integer = Integer.valueOf(String.valueOf(time.charAt(1)));
            if (integer > 3) {
                count = 2;
            } else {
                count = 3;
            }
        } else if (time.charAt(1) == queestionMark) {
            Integer integer = Integer.valueOf(String.valueOf(time.charAt(0)));
            if (integer > 1) {
                count = 4;
            } else {
                count = 10;
            }
        }

        // 判断后两位
        if (time.charAt(3) == queestionMark && time.charAt(4) == queestionMark) {
            count *=  60;
        } else if (time.charAt(3) == queestionMark) {
            count *= 6;
        } else if (time.charAt(4) == queestionMark) {
            count *= 10;
        }
        return count;
    }

    public static void main(String[] args) {
        lc2327fixTime l2327 = new lc2327fixTime();
        int i = l2327.countTime("?4:22");
        System.out.println(i);
    }
}
