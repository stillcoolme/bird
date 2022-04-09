package com.stillcoolme.basic;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        String realName = "刘第三";
        realName = realName.substring(0, 1) + "*" + realName.substring(realName.length() - 1, realName.length());
        System.out.println(realName);


    }
}
