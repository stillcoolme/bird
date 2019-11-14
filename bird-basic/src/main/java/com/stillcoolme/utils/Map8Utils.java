package com.stillcoolme.utils;

import com.stillcoolme.bean.Person;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author: stillcoolme
 * @date: 2019/10/26 11:18
 * @description:
 */
public class Map8Utils {

    /**
     * 据性别种类进行分类，返回一个Map<Type, List<Person>>的结果
     * @return
     */
    public static Map<Character, List<Person>> so(List<Person> list) {
        return list.stream().collect(groupingBy(Person::getSex));

        // jdk8以前写法。。。
        /*
        Map<Character, List<Person>> result = new HashMap<>();
        for (Person person : list) {
            //不存在则初始化
            if (result.get(person.getSex())==null) {
                List<Person> persons = new ArrayList<>();
                persons.add(person);
                result.put(person.getSex(), persons);
            } else {
                //存在则追加
                result.get(person.getSex()).add(person);
            }
        }
        return result;
        */
    }

    public static void sort() {
        Map<String, Integer> map = new HashMap();
        map.put("derek",24 );
        map.put("dad", 51);
        map.put("mom", 46);
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        list.addAll(map.entrySet());
        Collections.sort(list, (m1, m2) ->
            m1.getValue() - m2.getValue());
        list.forEach(x -> System.out.println(x.getKey()));
    }


    public static void removeByIterator() {
        Map<String, String> _revertMap = new HashMap();
        _revertMap.put("aa_1", "aa");
        _revertMap.put("aa_2", "aa");
        _revertMap.put("bb_2", "bb");

        _revertMap.entrySet().stream().forEach(x -> {
            System.out.println("Key = " + x.getKey() + ", Value = " + x.getValue());
        });
        Iterator<Map.Entry<String, String>> iterator = _revertMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
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

}
