package com.stillcoolme.designpattern.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-17 17:23:00
 * @Description
 */
public class HelloDynamicProxy implements InvocationHandler {

    // 被代理的类
    private Object target;

    public HelloDynamicProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("before");
        Object result = method.invoke(target, args);
        System.out.println("after");
        return result;
    }

    public static void main(String[] args) {

        IHello hello = new HelloImpl();
        // 代理类通过构造方法传入 被代理对象
        HelloDynamicProxy helloDynamicProxy = new HelloDynamicProxy(hello);
        // 动态生成代理对象
        IHello helloProxy = (IHello) Proxy.newProxyInstance(
                hello.getClass().getClassLoader(),
                hello.getClass().getInterfaces(),
                helloDynamicProxy
        );
        helloProxy.say("baby");
    }
}
