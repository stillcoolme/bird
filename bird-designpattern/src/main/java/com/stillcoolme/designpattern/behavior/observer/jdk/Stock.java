package com.stillcoolme.designpattern.behavior.observer.jdk;

import java.util.Observable;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 10:45
 * Function:
 *  Stock 继承 Observable 可被观察
 */
public class Stock extends Observable {
    private float mPrice;// 价钱

    public Stock(float price) {
        this.mPrice = price;
    }

    public float getPrice() {
        return this.mPrice;
    }

    public void setPrice(float price) {
        super.setChanged();
        super.notifyObservers(price);// 价格被改变
        this.mPrice = price;
    }

    public String toString() {
        return "股票价格为：" + this.mPrice;
    }
}
