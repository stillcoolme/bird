package com.stillcoolme.designpattern.behavior.observer.eventbus;

import java.util.concurrent.Executors;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 23:23
 * Function: 异步非阻塞的EventBus
 */
public class EventBusAsync extends EventBus {

    public EventBusAsync() {
        super(Executors.newCachedThreadPool());
    }

}
