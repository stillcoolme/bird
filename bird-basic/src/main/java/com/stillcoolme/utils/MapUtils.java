package com.stillcoolme.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: stillcoolme
 * @date: 2019/8/11 19:14
 * @description:
 **/
public class MapUtils {
    public static void mergeMap(Map map) {
        Map ss = new HashMap<String, Float>();
        Map ss2 = new HashMap<String, Float>();
        ss.put("ss", 12F);
        ss2.put("aa", 13F);
        ss2.put("aad", 14F);
        ss2.put("aa", 14F);
        //  https://blog.csdn.net/ontheroad1875/article/details/86747672
        ss2.forEach((key, value) -> ss.merge(key, value, (v1, v2) -> v2));
        for (Object key : ss.keySet()) {
            System.out.println(key + " " + ss.get(key));
        }
    }




    public static void sortMap(Map allGeodeKey2ScoreMap) {
        List<Map.Entry<String, Float>> list1 = new ArrayList<>();
        list1.addAll(allGeodeKey2ScoreMap.entrySet());
        Collections.sort(list1, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> map1, Map.Entry<String, Float> map2) {
                return ((map2.getValue() - map1.getValue() == 0) ? 0
                        : (map2.getValue() - map1.getValue() > 0) ? 1
                        : -1);
            }
        });
        for (int i = 0; i < list1.size(); i++) {
            System.out.println("key: " + list1.get(i).getKey() + ", value: " + list1.get(i).getValue());
        }
    }

    /**
     * 遍历map的几种方式
     * @param map
     */
    public static void loopMap(Map<String, Object> map) {

        System.out.println("遍历方式一 使用entries来遍历：");
        for(Map.Entry<String, Object> entry: map.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }

        System.out.println("遍历方式二 使用Iterator遍历：");
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }

        System.out.println("遍历方式三 在for-each循环中遍历keys或values，效率低：");
        //遍历map中的键
        for (String key : map.keySet()) {
            System.out.println("Key = " + key + ", Value = " + map.get(key));
        }
        //遍历map中的值
        for (Object value : map.values()) {
            System.out.println("Value = " + value);
        }

        // java8
        map.forEach((key, value) -> {
            System.out.println("Key = " + key + "  " + " Value = " + value);
        });

        map.entrySet().stream()
                // ...
                .forEach(e -> System.out.println(e.getKey() + ":" + e.getValue()));

    }


    /**
     * 测试 map中的list是否需要重新 put回去
     * getOrDefault得到的不行
     * get出来的才能不put回去
     */
    public static void testGetOrCreate() {
        Map<String, List<Integer>> map = new HashMap<>();
        List list = map.getOrDefault("haha", new ArrayList<>());
        list.add(1);
        list.add(2);
        //map.put("haha", list);
        for (Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }

        map.put("yoyo", new ArrayList<>());
        List list3 = map.get("yoyo");
        list3.add(3);
        list3.add(4);
        for (Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }

        Iterator<Map.Entry<String, List<Integer>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Integer>> entry = iterator.next();
            entry.getValue().clear();
        }
        System.out.println("=======");
        for (Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        System.out.println("+++++");
        List list4 = map.get("yoyo");
        list4.add(3);
        list4.add(4);
        for (Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    public static void removeByIterator() {
        Map _revertMap = new HashMap();
        _revertMap.put("aa_1", "aa");
        _revertMap.put("aa_2", "aa");
        _revertMap.put("bb_2", "bb");
        Iterator<Map.Entry<String, String>> iterator = _revertMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            if(entry.getValue().equals("aa")){
                iterator.remove();
            }
        }

        Iterator<Map.Entry<String, String>> iterator2 = _revertMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<String, String> entry = iterator2.next();
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
    }

    public static void test() {
        Map<String, Set<Integer>> map = new HashMap<String, Set<Integer>>();
        Set<Integer> set = new HashSet<Integer>();
        set.add(2);
        set.add(3);
        map.put("haha", set);
        Iterator<Map.Entry<String, Set<Integer>>> iterator = map.entrySet().iterator();
        Map.Entry<String, Set<Integer>> entry = iterator.next();
        if(! entry.getValue().isEmpty()) {
            Integer[] keyArray = entry.getValue().stream().toArray(Integer[]::new);
            for (int i = 0; i < keyArray.length; i++) {
                System.out.println(keyArray[i]);
            }
        }
    }


    /**
     * 没有map就new Map的正常写法
     */
    public static void noThenCreate() {
        Map<String, Map> localState = new HashMap<>();
        Map<String, String> local = (Map<String, String>) localState.get("map");
        if (local == null) {
            local = new HashMap<String, String>();
        }

    }


    public static void main(String[] args) {
/*        Map<String, Float> map = new HashMap();
        map.put("2dea5b3439b94d248de0e5e0fbc3fa0d", 0.7857033f);
        map.put("123", 0.98581654f);
        map.put("3d9c457767ce41c7a275115455e4437a", 0.7002197f);
        map.put("e0b87ae0662f498fa5e4c38827735a84", 0.69558275f);
        map.put("zjh", 0.98581654f);
        map.put("7b3363a3b2cf4401832bc75c91fc004a", 0.7142418f);
        map.put("37de8f4d69fa4b708d6840b107f42931", 0.7225254f);
        map.put("b3a67aaea74e47d18dc8978a6ee572ed", 0.8395152f);
        map.put("0ec32d7f3e2b407db74c5d26bcfa592d", 0.71480906f);
        map.put("7d5adf6dd410426fa2f785c2f574d23d", 0.7084687f);
        sortMap(map);

        testGetOrCreate();*/

        //test();
        removeByIterator();
    }
}
