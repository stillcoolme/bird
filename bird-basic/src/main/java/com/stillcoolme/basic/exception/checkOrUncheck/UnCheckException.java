package com.stillcoolme.basic.exception.checkOrUncheck;

import java.io.Serializable;

/**
 * @author: stillcoolme
 * @date: 2020/2/27 16:16
 *  继承 RuntimeException 的是受检异常， 不会飙红提示使用者处理
 */
public class UnCheckException extends RuntimeException implements Serializable {

    public UnCheckException(String messsage) {
        super(messsage);
    }

    public UnCheckException(String messsage, Throwable e) {
        super(messsage, e);
    }
}
