package com.learning.framework.proxy;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-17 18:50:00
 * @Description
 */
public interface Proxy {

    Object doProxy(ProxyChain proxyChain) throws Throwable;

}
