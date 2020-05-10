package com.stillcoolme.designpattern.behavior.observer.eventbus;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 17:09
 * Function: EventBus 实现的是阻塞同步的观察者模式
 */
public class EventBus {

    // Observer注册表： 消息类型 -> 观察者类 和 接收该类型消息的方法
    // 我以为是： 观察者 -> 观察者要执行的方法
    // 用对象封装 map
    private ObserverRegistry observerRegistry;
    private Executor executor;

    public EventBus() {
        this(Executors.newSingleThreadExecutor());
    }

    public EventBus(Executor executor) {
        this.executor = executor;
        this.observerRegistry = new ObserverRegistry();
    }

    public void register(Object object) {
        observerRegistry.register(object);
    }

    /**
     * 通知监听者进行执行
     *
     * @param event
     */
    public void post(Object event) {
        List<ObserverAction> matchedObserverActions =
                observerRegistry.getMatchedObserverActions(event);
        matchedObserverActions.forEach(x -> {
            executor.execute(() -> {
                x.execute(event);
            });
        });
    }

}
