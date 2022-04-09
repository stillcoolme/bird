package com.stillcoolme.designpattern.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-17 17:21:00
 * @Description
 */
public class HelloProxy implements IHello{

    HelloImpl helloImpl;

    public HelloProxy() {
        helloImpl = new HelloImpl();
    }

    @Override
    public void say(String name) {
        beforeSay();
        helloImpl.say(name);
        afterSay();
    }

    private void afterSay() {
        System.out.println("after say");
    }

    private void beforeSay() {
        System.out.println("before say");
    }


    public static void main(String[] args) {
        HelloProxy helloProxy = new HelloProxy();
        helloProxy.say("baby");
    }

}
