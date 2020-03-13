package com.stillcoolme.designpattern.construct.adapter.third;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 21:28
 * Description:
 *  对 SystemC 进行适配，使用构造函数注入进来
 */
public class SystemCAdapter implements ISystemA {
    private SystemC systemC;

    public SystemCAdapter(SystemC c) {
        systemC = c;
    }

    @Override
    public void call() {
        systemC.mycall();
    }
}
