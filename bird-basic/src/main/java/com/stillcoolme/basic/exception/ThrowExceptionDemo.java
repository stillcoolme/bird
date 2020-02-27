package com.stillcoolme.basic.exception;

import com.stillcoolme.basic.exception.checkOrUncheck.CheckException;
import com.stillcoolme.basic.exception.checkOrUncheck.UnCheckException;

/**
 * @author: stillcoolme
 * @date: 2020/2/27 14:53
 *
 *   继承结构：
 *   Throable
 *      | - Exccption
 *              |  -  RuntimeException
 *                          | - 各种不可查异常
 *              |  -  各种可查异常
 *
 *   1. RuntimeException 及其子类：
 *      不可查异常，下层抛出了不会提示用户需要捕获；
 *      抛出这种异常没有捕获的话程序就终止；
 *
 *   2. 继承 Exception 的：
 *      可查异常，会飙红，提示调用者需要处理
 */
public class ThrowExceptionDemo {

    public static void main(String[] args) {
        // 没有飙红来提示使用者要捕获下层抛出了 RuntimeException 或 RuntimeException的子类！！
        throwRuntimeExceptionTest();
        throwRuntimeExceptionTest2();

        // 继承Exception的为 可查异常，就会飙红，需要处理
        //  throwExceptionTest();
        //  throwExceptionTest2();

        System.out.println("程序继续执行");
    }

    public static void throwRuntimeExceptionTest() {
        int i = 10;
        if(i == 10) {
            throw new RuntimeException("i == 10 exception");
        }
    }

    public static void throwRuntimeExceptionTest2() throws UnCheckException {
        int i = 10;
        if(i == 10) {

        }
    }

    public static void throwExceptionTest() throws CheckException {
        try {
            int i = 10;
        } catch (Exception e) {
            // wrap成新的Exception2然后re-throw
            throw new CheckException("i == 10 exception", e);
        }
    }

    public static void throwExceptionTest2() throws Exception {
        try {
            int i = 10;
        } catch (Exception e) {
            throw new Exception("i == 10 exception");
        }
    }

}
