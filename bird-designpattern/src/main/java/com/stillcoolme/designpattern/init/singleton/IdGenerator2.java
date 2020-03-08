package com.stillcoolme.designpattern.init.singleton;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: stillcoolme
 * Date: 2020/3/8 18:41
 * Description:
 *  懒汉式
 */
public class IdGenerator2 {

    private AtomicInteger idAdder;

    private IdGenerator2() {
        this.idAdder = new AtomicInteger(0);
    }

    public static IdGenerator2 getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 静态内部类方式
    private static class SingletonHolder {
        private static final IdGenerator2 INSTANCE = new IdGenerator2();
    }

    public static void main(String[] args) {

    }
}
