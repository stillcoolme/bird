package com.stillcoolme.basic.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/7/6 9:22
 */
public class LoggerUtils {

    // slf4j门面就都是 LoggerFactory.getLogger 来调用
    public static Logger logger = LoggerFactory.getLogger(LoggerUtils.class);

    public static void main(String[] args) {
        logger.info("this is a log");
    }
}
