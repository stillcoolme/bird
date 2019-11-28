package com.stillcoolme.framework.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: stillcoolme
 * @date: 2019/11/27 16:07
 * @description:
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        String world = "world";
        logger.info("hellp world:{}",world);
        logger.error("exception e");
    }
}