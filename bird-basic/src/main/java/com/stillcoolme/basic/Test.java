package com.stillcoolme.basic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
@Slf4j
public class Test {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Test.class);
        logger.info("test slf4j to log4j2");

        Long nano = System.nanoTime();
        AtomicLong nanoAtomic = new AtomicLong(System.nanoTime());
        System.out.println(nano);
        System.out.println(nanoAtomic.get());

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

        HashMap integerHashMap = new HashMap<String, String>();
        integerHashMap.put("1", "hh");
        System.out.println(integerHashMap.keySet().toString());


    }
}
