package com.stillcoolme.designpattern.behavior.observer.jdk;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 10:49
 * Function: jdk自带的观察者模式框架： 1. 被观察者继承Observable; 2. 观察者实现Observer接口。
 */
public class App {

    public static void main(String[] args) {

        Stock house = new Stock(1222f);
        People p1 = new People("p1");
        People p2 = new People("p2");
        People p3 = new People("p3");
        house.addObserver(p1);
        house.addObserver(p2);
        house.addObserver(p3);

        System.out.println(house + ""); // 输出价格

        house.setPrice(111f);

        System.out.println(house + ""); // 输出价格
    }

}
