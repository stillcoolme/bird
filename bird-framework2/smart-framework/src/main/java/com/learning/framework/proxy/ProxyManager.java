package com.learning.framework.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-17 19:17:00
 * @Description
 *  用来创建所有代理对象。
 *  输入一个目标类和一组Proxy接口实现，输出一个代理对象
 */
public class ProxyManager {

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final Class<?> targetClass, final List<Proxy> proxyList) {

        return (T) Enhancer.create(targetClass, new MethodInterceptor() {

            public Object intercept(Object targetObject, Method targetMethod,
                                     Object[] methodParams , MethodProxy methodProxy) throws Throwable {
                return new ProxyChain(targetClass, targetObject, targetMethod,
                               methodProxy, methodParams, proxyList).doProxyChain();
            }
        });

    }
}