package com.stillcoolme.basic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
@Slf4j
public class Test {

    public static void main(String[] args) {

        Integer test = null;
        if (test == null) {
            System.out.println("is null");
        } else {
            System.out.println("is not null");
        }

        String haha = "";
        if (haha == null) {
            System.out.println("null");
        }

        Long ss = Long.valueOf("1234564345632");
        System.out.println(ss);

        StringUtils.isNumeric("192");
        StringUtils.compare("aa", "23");

    }
}
