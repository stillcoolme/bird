package com.stillcoolme.designpattern.behavior.observer.first;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 16:15
 * Function: 具体主题，观察者们注册到这里，来观察 具体主题 做了什么 然后通知他们。
 */
public class SubjectConcrete implements Subject {
    List<Observer> observerList = new ArrayList();

    @Override
    public void addObserver(Observer observer) {
        observerList.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observerList.remove(observer);
    }

    @Override
    public void notification(Message message) {
        observerList.forEach(x -> {
            x.updateMessage(message);
        });
    }

    public void subjectDoSomething() {
        // doSomething

    }
}
