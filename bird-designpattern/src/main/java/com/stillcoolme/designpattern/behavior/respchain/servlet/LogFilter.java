package com.stillcoolme.designpattern.behavior.respchain.servlet;

/**
 * @author: stillcoolme
 * @date: 2020/5/14 9:25
 * Function:
 */
public class LogFilter implements Filter {

    @Override
    public void initFilter(String filterConfig) {
        // 在创建Filter时自动调用，
        // 其中filterConfig包含这个Filter的配置参数，比如name之类的（从配置文件中读取的）

    }

    @Override
    public void doFilter(String requestParam, FilterChain filterChain) {
        System.out.println("拦截客户端发送来的请求, 执行 LogFilter 逻辑.");

        // 递归调用 filterChain，filterChain 会调用下一个 Filter
        filterChain.doFilter(requestParam);

        System.out.println("拦截发送给客户端的响应, 执行 LogFilter 逻辑.");
    }

    @Override
    public void destoryFilter() {

    }

    /**
     * // 在web.xml配置文件中如下配置：
     * <filter>
     *  <filter-name>logFilter</filter-name>
     *  <filter-class>com.xzg.cd.LogFilter</filter-class>
     * </filter>
     * <filter-mapping>
     *  <filter-name>logFilter</filter-name>
     *  <url-pattern>/*</url-pattern>
     * </filter-mapping>
     */
}
