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

    public static void main(String[] args) throws InterruptedException {

        Logger logger = LoggerFactory.getLogger(Test.class);
        logger.info("----------------- start --------------");
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            logger.info("hello world {}", i);
        }

        logger.info("----------------- end --------------\n");

    }
}
