package com.stillcoolme.basic.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 通过反射实现的动态代理；动态代理还可通过ASM那些来实现，则是通过生成业务类的子类作为代理类（cglib)
 */
public class DynamicProxy {

    public static void main(String[] args) {
        SayHelloImpl sayHelloImpl = new SayHelloImpl();
        MyInvocationHandler handler = new MyInvocationHandler(sayHelloImpl);
        // 调用者得到的还是原来的接口，却不知道具体实现已经被修改了。
        SayHello sayHello = (SayHello) Proxy.newProxyInstance(sayHelloImpl.getClass().getClassLoader(), SayHelloImpl.class.getInterfaces(), handler);
        sayHello.sayHello();
    }

}

interface SayHello{
    public void sayHello();
}

/**
 * 被代理的对象一定要实现一个接口
 *
 * 要动态地生成代理类，要有接口才行。
 * 代理对象也实现被代理的对象一样的接口，在调用方看来没变化，但是里面却已沧海桑田。
 */
class SayHelloImpl implements SayHello{
    @Override
    public void sayHello() {
        System.out.println("SayHello: hello!!!");
    }
}

/**
 * 每一个动态代理类都必须要实现InvocationHandler这个接口，并且每个代理类的实例都关联到了一个handler，
 * 当我们通过代理对象调用一个方法的时候，这个方法的调用就会被转发为由InvocationHandler这个接口的 invoke 方法来进行调用。
 */
class MyInvocationHandler implements InvocationHandler{
    // 要被代理的目标对象
    private Object target;
    public MyInvocationHandler(Object target){
        this.target = target;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 新的执行逻辑
        System.out.println("Proxy: hello!!!");
        // 调用被代理对象的执行逻辑
        Object result = method.invoke(target, args);
        return result;
    }
}
