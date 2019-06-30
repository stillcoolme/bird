package com.stillcoolme.structure.map;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: stillcoolme
 * Date: 2019/6/27 9:43
 * Description:
 * Given two strings s and t , write a function to determine if t is an anagram of s.
 * Example 1:
 *  Input: s = "anagram", t = "nagaram"
 *  Output: true
 */
public class _242_ValidAnagram {

    /**
     * 解法1：
     * 暴力：将两个字符串中的字符排序，然后比较每个是否相同。
     * 时间复杂度 O(N * logN)。 假设 N 是 s 的长度，排序成本 O(N * logN) 和比较两个字符串的成本O(N)。排序时间占主导地位，总体时间复杂度为O(N * logN)。
     *
     */
    public boolean isAnagram1(String s, String t) {
        if(s == null || t == null || s.length() != t.length()){
            return false;
        }
        char[] chars1 = s.toCharArray();
        char[] chars2 = t.toCharArray();
        Arrays.sort(chars1);
        Arrays.sort(chars2);
        return chars1.equals(chars2);
    }

    /**
     * 解法2: 使用map来记数。
     * 1. 将s作为char数组，每个char放到map中作为key，然后出现次数作为value；
     * 2. t也弄成char数组，每个char放到map中value为减1；
     * 3. 最后map中那个value不为0，说明 s和t不一致。
     *
     * 时间复杂度：O(n)。因为访问计数器表是一个固定的时间操作。
     * 空间复杂度：O(1)。尽管我们使用了额外的空间，但是空间的复杂性是 O(1)，因为无论 N 有多大，表的大小都保持不变。
     *
     * @param s
     * @param t
     * @return
     */
    public boolean isAnagram2(String s, String t) {
        if(s == null || t == null || s.length() != t.length()){
            return false;
        }
        char[] chars1 = s.toCharArray();
        char[] chars2 = t.toCharArray();
        Map map1 = new HashMap<Character, Integer>();
        for (int i = 0; i < chars1.length; i++) {
            if(map1.containsKey(chars1[i])){
                map1.put(chars1[i], (Integer)map1.get(chars1[i]) + 1);
            } else {
                map1.put(chars1[i], 1);
            }
        }
        for (int i = 0; i < chars2.length; i++) {
            if(map1.containsKey(chars2[i])){
                map1.put(chars2[i], (Integer)map1.get(chars2[i]) - 1);
            } else {
                return false;
            }
        }
        for(Object value: map1.values()){
            if(! value.equals(0)){
                return false;
            }
        }
        return true;
    }

    /**
     * 解法3: 使用数组来计数，对解法2的改进。
     * 1. 因为只有26个字母，每个字母占用一个数组位置，数组值用来计数。
     * @param s
     * @param t
     * @return
     */
    public boolean isAnagram3(String s, String t) {
        if(s == null || t == null || s.length() != t.length()){
            return false;
        }
        int[] table = new int[26];
        for (int i = 0; i < s.length(); i++) {
            table[s.charAt(i) - 'a'] ++;
        }
        for (int i = 0; i < t.length(); i++) {
            table[t.charAt(i) - 'a'] --;
            if(table[t.charAt(i) - 'a'] < 0){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        _242_ValidAnagram validAnagram = new _242_ValidAnagram();
        validAnagram.isAnagram3("hahah", "hahah");
    }
}
