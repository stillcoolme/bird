package com.stillcoolme.leetcode.all._145_20190714;

import java.util.*;

/**
 * Author: stillcoolme
 * Date: 2019/7/14 10:43
 * Description:
 * 给你两个数组，arr1 和 arr2，
 *  arr2 中的元素各不相同
 *  arr2 中的每个元素都出现在 arr1 中
 * 对 arr1 中的元素进行排序，使 arr1 中项的相对顺序和 arr2 中的相对顺序相同。
 * 未在 arr2 中出现过的元素需要按照升序放在 arr1 的末尾。
 *
 * 输入：arr1 = [2,3,1,3,2,4,6,7,9,2,19], arr2 = [2,1,4,3,9,6]
 * 输出：[2,2,2,1,4,3,3,9,6,7,19]
 */
public class _5127_RelativeSortArray {

    /**
     * 暴力法：使用map来记录，但是用太多for循环了吧
     * @param arr1
     * @param arr2
     * @return
     */
    public int[] relativeSortArray(int[] arr1, int[] arr2) {
        if(arr2.length < 1){
            return arr1;
        }
        List<Integer> arr2List = new ArrayList<>();
        for (int i = 0; i < arr2.length; i++) {
            arr2List.add(arr2[i]);
        }

        Map<Integer, Integer> map = new HashMap();
        List notAppear = new ArrayList();
        for (int i = 0; i < arr1.length; i++) {
            if(arr2List.contains(arr1[i])) {
                Integer count = map.getOrDefault(arr1[i], 0);
                map.put(arr1[i], count + 1);
            } else {
                notAppear.add(arr1[i]);
            }
        }
        arr2List.clear();
        for (int i = 0; i < arr2.length; i++) {
            int count = map.get(arr2[i]);
            for (int j = 0; j < count; j++) {
                arr2List.add(arr2[i]);
            }
        }
        Collections.sort(notAppear);
        for (int i = 0; i < notAppear.size(); i++) {
            arr2List.add((Integer) notAppear.get(i));
        }
        int[] result = new int[arr2List.size()];
        for (int i = 0; i < arr2List.size(); i++) {
            result[i] = (int) arr2List.get(i);
        }
        return result;
    }

    public static void main(String[] args) {
        _5127_RelativeSortArray relativeSortArray = new _5127_RelativeSortArray();
        relativeSortArray.relativeSortArray(new int[]{2,3,1,3,2,4,6,7,9,2,19}, new int[]{2,1,4,3,9,6});

    }
}
