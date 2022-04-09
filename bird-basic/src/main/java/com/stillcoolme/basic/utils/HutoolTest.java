package com.stillcoolme.basic.utils;

import cn.hutool.core.convert.Convert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-12-25 23:05:00
 * @Description
 */
public class HutoolTest {

    public static Logger logger = LoggerFactory.getLogger(HutoolTest.class);

    public static void main(String[] args) {

        int number = 12;
        String numberStr = Convert.toStr(number);
        logger.info(numberStr);


    }
}
