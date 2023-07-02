package com.stillcoolme.basic.utils;

import com.stillcoolme.basic.bean.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * @author: stillcoolme
 * @date: 2019/10/26 11:20
 * @description:
 */
public class List8Utils {

    /**
     * 筛选，排序
     *
     * @param personList
     * @return
     */
    private static List<String> sortList(List<Person> personList) {
        return personList.stream()
                .filter(person -> person.getAge() > 20)
                .sorted(comparing(Person::getAge))  //根据年龄进行排序
                .map(Person::getName)  //map流映射: 提取名称，将 Person 转换成 String
                .collect(Collectors.toList());  //转换为List !! 就不用另外加了
    }

    /**
     * flatMap流转换: 将一个流中的每个值都转换为另一个流
     */
    public void flatMapTest() {
        List<String> wordList = Arrays.asList("Hello", "World");
        List<String> strList = wordList.stream()
                // map(w -> w.split(" "))的返回值为Stream<String[]>
                .map(w -> w.split(" "))
                // 我们想获取Stream<String>，可以通过flatMap方法完成Stream ->Stream的转换
                .flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    // 元素匹配

    /**
     * 元素匹配 提供了三种匹配方式: allMatch匹配所有, anyMatch匹配其中一个(匹配到就break), noneMatch全部不匹配
     */
    public static void matchTest() {
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        if (integerList.stream().allMatch(i -> i > 3)) {
            // 所有都匹配才打印
            System.out.println("值都大于3");
        }

        List<Integer> integerList2 = Arrays.asList(1, 2);
        if (integerList2.stream().noneMatch(i -> i > 3)) {
            // 所有都不匹配才打印
            System.out.println("值都小于3");
        }

    }

    // 查找

    /**
     * findAny随机查找一个
     */
    public void findAnyTest() {
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        Optional<Integer> result = integerList
                .stream()
                .filter(i -> i > 3)
                .findAny();
        // 通过findAny方法查找到其中一个大于三的元素并打印;
        // 因为内部进行优化的原因，当找到第一个满足大于三的元素时就结束，该方法结果和findFirst方法结果一样;
        // 提供findAny方法是为了更好的利用并行流.
    }


    public static void main(String[] args) {
        matchTest();
        List<Person> list = new ArrayList();
        list.add(new Person("haha112", 112, 'x'));
        list.add(new Person("haha119", 119, 'x'));
        list.add(new Person("haha110", 110, 'x'));
        System.out.println(list.size());
        List<String> strings = sortList(list);
        System.out.println(strings.toString());
    }

}
