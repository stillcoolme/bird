package com.stillcoolme.designpattern.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-17 17:37:00
 * @Description
 *   newProxyInstance方法参数太麻烦，对其进行封装
 */
public class HelloDynamicProxy2 implements InvocationHandler {

    private Object target;

    public HelloDynamicProxy2(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("before");
        Object result = method.invoke(target, args);
        System.out.println("after");
        return result;
    }

    // 封装了获取代理对象在内部！！！！SuppressWarnings("unchecked")注解表示忽略编译时告警
    // 因为Proxy.newProxyInstance返回Object，转为T是向下转型，会告警。
    @SuppressWarnings("unchecked")
    public <T> T getProxy() {
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                this
        );
    }


    public static void main(String[] args) {
        IHello hello = new HelloImpl();
        IHello helloProxy = new HelloDynamicProxy2(hello).getProxy();
        helloProxy.say("babe2");

    }
}
