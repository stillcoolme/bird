package com.stillcoolme.basic.utils;

import com.stillcoolme.basic.bean.Result;

import java.util.*;

/**
 * Author: stillcoolme
 * Date: 2019/7/25 17:51
 * Description:
 *  List相关工具类
 */
public class ListUtils {

    // array 转 List
    public static List arrayToList1(String[] array) {
        List list = new ArrayList();
        Collections.addAll(list, array);
        return list;
    }

    // array 转 List
    public static List arrayToList2(String[] array) {
        return new ArrayList<>(Arrays.asList(array));
    }

    // list 转 array
    public static String[] arrayToList2(List<String> list) {
        String[] array = list.toArray(new String[list.size()]);
        return array;
    }


    // compareList By float
    public static void listCompare() {
        List<Result> list = new ArrayList<>();
        list.add(new Result(1, 23.1f));
        list.add(new Result(2, 23.12f));
        list.add(new Result(3, 24f));
        Collections.sort(list, new Comparator<Result>() {
            @Override
            public int compare(Result o1, Result o2) {
                // o1 - o2 正序 （返回负数说明比较小排前面，返回正数说明比较大排后面）， o2 - o1 倒序
                // 正序
                float result = o1.getScore() - o2.getScore();
                // 倒序
//                float result = o2.getScore() - o1.getScore();

                if(result > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getId() + " " + list.get(i).getScore());
        }
    }

    public static void main(String[] args) {
        listCompare();
        String[] str = new String[3];
        for (int i = 0; i < 3; i++) {
            str[i] = i + " ";
        }
        List list = arrayToList1(str);
        System.out.println(list);
    }
}
