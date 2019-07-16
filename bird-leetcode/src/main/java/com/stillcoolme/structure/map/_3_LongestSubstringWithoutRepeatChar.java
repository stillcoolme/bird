package com.stillcoolme.structure.map;

import java.util.*;

/**
 * Author: stillcoolme
 * Date: 2019/7/12 13:27
 * Description:
 * 返回最长不重复的子串
 * 哎，暴力法都做不出来
 */
public class _3_LongestSubstringWithoutRepeatChar {

    /**
     * 牛逼滑动窗口？？
     * 滑动窗口是数组/字符串问题中常用的抽象概念。
     * 窗口通常是在数组/字符串中由开始和结束索引定义的一系列元素的集合，即 [i,j)（左闭，右开）。
     * @param s
     * @return
     */
    //TODO
    public int lengthOfLongestSubstring(String s) {
        char[] chars = s.toCharArray();
        Set slideSet = new HashSet();
        int result = 0;
        int start = 0;
        int end = 0;
        while(start < chars.length && end < chars.length) {
            if(slideSet.contains(chars[end])){
                // 只是删除最开始的？？
                slideSet.remove(chars[start ++]);
            } else {
                slideSet.add(chars[end ++]);
                result = Math.max(result, end - start);
            }
        }
        return result;
    }

    /**
     * 思路：
     * 我们可以定义字符到索引的映射,
     * 当我们找到重复的字符时，我们可以立即跳过该窗口到。
     * 如果遍历到 s[j]，前面已遍历的 [i, j) 范围内有a[ j'] 和 a[j] 重复，我们不需要逐渐增加 i 。
     * 我们可以直接跳过 [i，j']范围内的所有元素，并将 i 变为 j' + 1
     * 用map的value来间接记录和现在char相同的之前字符的位置，学到了 ！！！
     * @param s
     * @return
     */
    //TODO
    public int lengthOfLongestSubstring2(String s) {
        int n = s.length(), ans = 0;
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0, j = 0; j < n; j++) {
            if(map.containsKey(s.charAt(j))){
                i = Math.max(map.get(s.charAt(j)), i);
            }
            ans = Math.max(ans, j - i + 1);
            map.put(s.charAt(j), j + 1);
        }
        return ans;
    }

    public static void main(String[] args) {
        _3_LongestSubstringWithoutRepeatChar longestSubstringWithoutRepeatChar = new _3_LongestSubstringWithoutRepeatChar();
        int length = longestSubstringWithoutRepeatChar.lengthOfLongestSubstring2("aubucd");
        System.out.println(length);

    }
}
