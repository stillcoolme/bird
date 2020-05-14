package com.stillcoolme.designpattern.behavior.respchain.servlet;

/**
 * @author: stillcoolme
 * @date: 2020/5/14 9:50
 * Function:
 */
public class ApplicationFilterChain implements FilterChain {
    private int pos = 0; //当前执行到了哪个filter
    private int n; //filter的个数
    private Filter[] filters;


    @Override
    public void doFilter(String requestParam) {
        if (pos < n) {
            Filter filter = filters[pos ++];
            // 递归调用
            filter.doFilter(requestParam, this);
        } else {
            // filter 都执行完了，执行 servlet
            // servlet.service(requestParam);
        }


    }

    @Override
    public void addFilter(Filter filter) {
        if (n == filters.length) {//扩容
            Filter[] newFilters = new Filter[n + n];
            System.arraycopy(filters, 0, newFilters, 0, n);
            filters = newFilters;
        }
        filters[n++] = filter;
    }
}
