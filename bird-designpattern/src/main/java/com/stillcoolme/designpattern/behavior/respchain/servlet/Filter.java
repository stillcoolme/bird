package com.stillcoolme.designpattern.behavior.respchain.servlet;

/**
 * @author: stillcoolme
 * @date: 2020/5/14 8:34
 * Function: Servlet 的 Filter 接口
 */
public interface Filter {

    /**
     * filter init method
     * @param filterConfig 读取 web.xml 中对该 filter 的配置：filter名，过滤的url 等等
     * @return
     */
    public void initFilter(String filterConfig) throws Exception;

    /**
     *
     * @param requestParam
     * @return
     */
    public void doFilter(String requestParam, FilterChain filterChain);

    public void destoryFilter();


}
