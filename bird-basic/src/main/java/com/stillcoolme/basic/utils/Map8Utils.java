package com.stillcoolme.basic.utils;

import com.stillcoolme.basic.bean.Person;
import com.stillcoolme.basic.bean.User;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author: stillcoolme
 * @date: 2019/10/26 11:18
 * @description:
 */
public class Map8Utils {

    /**
     * 利用 flatMap，将用户的权限列表里面相同的权限去重合并
     * @return
     */
    public static void flatMapTest() {
        List<String> list = Arrays.asList(new String[]{"first page", "manager page", "manager page"});
        ArrayList<String> perList = new ArrayList<>(list);
        User user1 = new User("bobo", perList);
        User user2 = new User("jack", perList);
        ArrayList<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        List<String> userList = users.stream().flatMap(user -> user.getPermi().stream())
                .distinct()
                .collect(Collectors.toList());
        System.out.println(userList.toString());
    }


    /**
     * 以 user name 为 key， User 为value的 map
     * @return
     */
    public static void toPersonNameMap() {
        List<String> list = Arrays.asList(new String[]{"first page", "manager page", "manager page"});
        ArrayList<String> perList = new ArrayList<>(list);
        User user1 = new User("bobo", perList);
        User user2 = new User("jack", perList);
        ArrayList<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Map<String, User> userNameMap = users.stream().collect(Collectors.toMap(User::getName, user -> user));
        System.out.println(userNameMap.toString());


        Map<String, List<String>> userNametoPermMap = userNameMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue().getPermi() // 假设Person类有一个getAge()方法来获取年龄
                ));
        System.out.println(userNametoPermMap.toString());
    }


    /**
     * 据性别种类进行分类 groupingBy，返回一个Map<Type, List<Person>>的结果
     * @param list
     * @return
     */
    public static Map<Character, List<Person>> groupingByTest(List<Person> list) {
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

    /**
     * 合并map
     */
    public static void mergeMap() {
        Map<String, Person> map1 = new HashMap<>();
        Map<String, Person> map2 = new HashMap<>();
        Person person1 = new Person("Henry");
        map1.put(person1.getName(), person1);
        Person person2 = new Person("Annie");
        map1.put(person2.getName(), person2);
        Person person3 = new Person("John");
        map1.put(person3.getName(), person3);

        Person person4 = new Person("George");
        map2.put(person4.getName(), person4);
        Person person5 = new Person("Henry");
        map2.put(person5.getName(), person5);

        Map<String, Person> map3 = new HashMap<>(map1);
        map2.forEach((key, value) ->
                map3.merge(key, value, (v1, v2) -> new Person(v1.getName())));
        map3.forEach((k, v) -> System.out.println(k + ": " + v.getName()));
        int s = 2;
        s = s >> 1;
        System.out.println("==========");
        // 方法二
        Map<String, Person> map4 = Stream.of(map1, map2)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> new Person(v2.getName())
                        )
                );
        map4.forEach((k, v) -> System.out.println(k + ": " + v.getName()));
    }

    public static void main(String[] args) {
        mergeMap();

        flatMapTest();

        toPersonNameMap();
    }
}
