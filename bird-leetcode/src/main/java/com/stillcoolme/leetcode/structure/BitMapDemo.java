package com.stillcoolme.leetcode.structure;

import java.util.BitSet;

/**
 * Author: stillcoolme
 * Date: 2020/3/21 23:09
 * Description:
 *  spark里的bloomfilter https://mp.weixin.qq.com/s/80DdrpmGIpYUThxWqf6kew
 */
public class BitMapDemo {

    static class BitMap {
        private long length;
        private int[] bitsMap;

        public BitMap(long length) {
            this.length = length;
            // 根据 length 算出需要的数据大小， 右移 5 位就是除以 2的5次方，也就是 32
            bitsMap = new int[(int) (length >> 5) + ((length & 31) > 0 ? 1 : 0)];
        }

        public int getBit(long index) {
            int intData = bitsMap[(int) ((index - 1) >> 5)];
            int offset = (int) ((index - 1) & 31);
            return intData >> offset & 0x01;
        }

        public void setBit(long index) {
            // 求出该值的在数组中的那个元素上
            int arrayIndex =  (int) ((index - 1) >> 5);
            int inData = bitsMap[arrayIndex];
            // 求出该值的在元素的偏移量(求余)
            int offset = (int) ((index - 1) & 31);
            bitsMap[arrayIndex] = inData | (0x01 << offset);
        }

    }

    public static void main(String[] args) {
        int[] array = new int[]{3, 22, 65};

        // jdk自带的bitmap实现

        BitSet bitMap = new BitSet();
        // 默认大小是64位
        System.out.println(bitMap.size());
        //将数组内容放到 bitmap 对应位上，置为 true，后面就可以判断这个 true 为有该元素了
        for (int i = 0; i < array.length; i++) {
            bitMap.set(array[i], true);
        }
        System.out.println("下面开始遍历BitSet：");
        for ( int i = 0; i < bitMap.size(); i++ ){
            System.out.println(bitMap.get(i));
        }

        // 自定义 bitMap
        BitMap bitMap1 = new BitMap(32L);
        bitMap1.setBit(32);
        System.out.println(bitMap1.getBit(1));
        System.out.println(bitMap1.getBit(32));

    }

}
