package com.stillcoolme.basic;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        String str = "2022-12-05";
        int i = "2022-12-06".compareTo(str);
        System.out.println(i);

        ArrayList arrayList = new ArrayList();
        arrayList.add("BOB");

        Map<String, List<String>> thisMap= new HashMap<>();
        thisMap.put("name", Arrays.asList("HAHA", "BOBO", "LUCY"));
        arrayList.addAll(thisMap.get("name"));

        System.out.println(arrayList);

    }
}
