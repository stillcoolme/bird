package com.stillcoolme.designpattern.proxy;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-17 17:20:00
 * @Description
 */
public class HelloImpl implements IHello{
    @Override
    public void say(String name) {
        System.out.println("hello by HelloImpl, hi " + name);
    }
}
