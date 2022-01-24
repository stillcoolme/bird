package com.learning.framework.proxy;

import com.learning.framework.annotation.Aspect;
import com.learning.framework.annotation.Controller;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-18 09:26:00
 * @Description
 *  拦截 Controller 所有方法，在目标方法执行前后执行 before、after 方法逻辑
 */
@Slf4j
@Aspect(Controller.class)
public class ControllerAspect extends AspectProxy{

    private long begin;

    public void before(Class<?> cls, Method method, Object[] params) throws Throwable {
        log.debug("----begin----");
        log.debug(cls.getName());
        log.debug(method.getName());
        begin = System.currentTimeMillis();
    }

    public void after(Class<?> cls, Method method, Object[] params, Object result) throws Throwable {
        log.debug("用时:"+(System.currentTimeMillis()-begin));
        log.debug("---end---");
    }
}
