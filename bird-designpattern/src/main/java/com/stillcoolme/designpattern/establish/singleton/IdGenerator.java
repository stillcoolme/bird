package com.stillcoolme.designpattern.establish.singleton;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: stillcoolme
 * Date: 2020/3/8 16:00
 * Description:
 *  单例模式饿汉式: 在类初始化时，已经自行实例化。
 *
 */
public class IdGenerator {

    private static final IdGenerator instance = new IdGenerator();
    private AtomicInteger idAdder;

    private IdGenerator() {
        idAdder = new AtomicInteger(0);
    }

    public static IdGenerator getInstance() {
        return instance;
    }

    public Integer getId() {
        return idAdder.addAndGet(1);
    }

}
