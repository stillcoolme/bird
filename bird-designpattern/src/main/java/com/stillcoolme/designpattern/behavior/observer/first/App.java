package com.stillcoolme.designpattern.behavior.observer.first;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 16:19
 * Function:
 */
public class App {

    public static void main(String[] args) {
        // 生成一个主题角色
        Subject subject = new SubjectConcrete();
        subject.addObserver(new ObserverFirst());
        subject.addObserver(new ObserverSecond());

        // 做一些要通知观察者的业务逻辑...

        // 通知
        subject.notification(new Message());
    }

}
