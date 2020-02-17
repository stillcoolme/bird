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

    // compareList By float
    public static void ListCompare() {
        List<Result> list = new ArrayList<>();
        list.add(new Result(1, 23.1f));
        list.add(new Result(2, 23.12f));
        list.add(new Result(3, 24f));
        Collections.sort(list, new Comparator<Result>() {
            @Override
            public int compare(Result o1, Result o2) {
                float result = o1.getScore() - o2.getScore();
                // 倒序
                if(result < 0) {
                    return 1;
                } else {
                    return -1;
                }

                // 正序
                /*if(result < 0) {
                    return 1;
                } else {
                    return -1;
                }*/
            }
        });
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getId() + " " + list.get(i).getScore());
        }
    }

    public static void main(String[] args) {
        String[] str = new String[3];
        for (int i = 0; i < 3; i++) {
            str[i] = i + " ";
        }
        List list = arrayToList1(str);
        System.out.println(list);
    }
}
