package com.stillcoolme.designpattern.behavior.observer.jdk;

import java.util.Observable;
import java.util.Observer;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 10:46
 * Function:
 * People观察股票的价格变化
 */
public class People implements Observer {

    private String name;

    public People(String name) {
        this.name = name;
    }

    @Override
    public void update(Observable observable, Object data) {
        System.out.println("People update() -> update name:" + this.name + ",price:" + ((Float) data).floatValue());
    }

}