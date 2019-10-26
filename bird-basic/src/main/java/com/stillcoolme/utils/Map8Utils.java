package com.stillcoolme.utils;

import com.stillcoolme.bean.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


}
