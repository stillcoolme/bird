package com.stillcoolme.basic.reflect.spi;

/**
 * @author: stillcoolme
 * @date: 2020/3/25 15:29
 * Function:
 */
public class DoSomethingImpl implements IDoSomething {

    @Override
    public String shortName() {
        return "DoSomethingImpl";
    }

    @Override
    public void doSomething() {
        System.out.println("I am DoSomethingImpl");
    }
}
