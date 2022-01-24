package com.learning.framework.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-18 09:15:00
 * @Description
 *   切面类，实现被前后增强的逻辑。
 *   这个只是切面模板抽象类，提供模板抽象方法（钩子方法），具体由子类 选择 扩展相应方法
 */
@Slf4j
public abstract class AspectProxy implements Proxy{

    public Object doProxy(ProxyChain proxyChain) throws Throwable {
        Object result = null;

        Class<?> cls = proxyChain.getTargetClass();
        Method method = proxyChain.getTargetMethod();
        Object[] params = proxyChain.getMethodParams();

        begin();
        try {
            if (intercept(cls, method, params)) {
                before(cls,method,params);
                result = proxyChain.doProxyChain();
                after(cls,method,params,result);
            } else {
                result = proxyChain.doProxyChain();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("proxy failure");
            error(cls, method, params, result, e);
        } finally {
            end();
        }

        return result;
    }
    public void begin() {

    }

    public boolean intercept(Class<?> cls,Method method,Object[] params) {
        return true;
    }

    public abstract void before(Class<?> cls,Method method,Object[] params) throws Throwable;

    public abstract void after(Class<?> cls,Method method,Object[] params,Object result) throws Throwable;

    public  void error(Class<?> cls, Method method, Object[] params, Object result, Throwable throwable) {

    }

    public void end() {

    }
}
