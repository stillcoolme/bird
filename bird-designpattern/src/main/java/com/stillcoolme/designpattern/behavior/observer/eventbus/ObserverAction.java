package com.stillcoolme.designpattern.behavior.observer.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 20:53
 * Function:  表示 @Subscribe 注解的方法，
 * target 表示观察者类，
 * method 表示被注解的方法
 */
public class ObserverAction {
    private Object target;
    private Method method;

    public ObserverAction(Object target, Method method) {
        this.target = target;
        this.method = method;
        method.setAccessible(true);
    }

    /**
     * event 就是传递给观察者的参数
     *
     * @param event
     */
    public void execute(Object event) {
        try {
            method.invoke(target, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


}
