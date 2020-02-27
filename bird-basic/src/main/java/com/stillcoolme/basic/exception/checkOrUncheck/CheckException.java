package com.stillcoolme.basic.exception.checkOrUncheck;

import java.io.Serializable;

/**
 * @author: stillcoolme
 * @date: 2020/2/27 15:39
 *
 * 继承 Exception 的是受检异常，会飙红提示使用者处理
 */
public class CheckException extends Exception implements Serializable {

    public CheckException(String message) {
        super(message);
    }

    public CheckException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
