package com.stillcoolme.designpattern.behavior.observer.first;


/**
 * 主题接口
 */
public interface Subject {

    public void addObserver(Observer observer);
    public void removeObserver(Observer observer);
    public void notification(Message message);

}
