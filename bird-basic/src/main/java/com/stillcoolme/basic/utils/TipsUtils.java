package com.stillcoolme.basic.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author: stillcoolme
 * @date: 2019/11/12 9:29
 * @description:
 */
public class TipsUtils {

    public static void main(String[] args) {
        // 获得程序的调用者
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        String stackTraceStr = Arrays.stream(stackTraceElements).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator()));
        System.out.println(stackTraceStr);
    }
}
