package com.stillcoolme.designpattern.behavior.respchain.servlet;

/**
 * @author: stillcoolme
 * @date: 2020/5/14 8:34
 * Function: FilterChain 接口, 具体的 web容器 tomcat, jetty 来做具体实现，
 */
public interface FilterChain {

    public void doFilter(String requestParam);

    public void addFilter(Filter filter);

}
