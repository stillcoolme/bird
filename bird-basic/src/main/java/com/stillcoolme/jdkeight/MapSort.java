package com.stillcoolme.jdkeight;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: stillcoolme
 * Date: 2019/6/28 10:33
 * Description:
 */
public class MapSort {

    public static void main(String[] args) {
        Map ss = new HashMap<String, Float>();
        Map ss2 = new HashMap<String, Float>();
        ss2.put("ss", 12F);
        ss2.put("aa", 13F);
        ss2.put("aad", 14F);
        ss2.put("aa", 14F);

        //  https://blog.csdn.net/ontheroad1875/article/details/86747672
        ss2.forEach((key, value) -> ss.merge(key, value, (v1, v2) -> v2));
        for (Object key : ss.keySet()) {
            System.out.println(key + " " + ss.get(key));
        }
//        Map<String, Float> result2 = new LinkedHashMap<>();
//        ss2.entrySet().stream()
//                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
//                .forEachOrdered(x -> result2.put());

        List<Map.Entry<String, Float>> list1 = new ArrayList<>();
        list1.addAll(ss2.entrySet());
        Collections.sort(list1, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> map1, Map.Entry<String, Float> map2) {
                return ((map2.getValue() - map1.getValue() == 0) ? 0
                        : (map2.getValue() - map1.getValue() > 0) ? 1
                        : -1);
            }
        });
        for (Map.Entry<String, Float> entry : list1) {
            System.out.println("key:" + entry.getKey() + ",value:" + entry.getValue());
        }

    }
}
