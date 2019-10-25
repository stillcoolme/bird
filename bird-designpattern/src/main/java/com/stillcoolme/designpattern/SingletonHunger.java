package com.stillcoolme.designpattern;

/**
 * @author: stillcoolme
 * @date: 2019/7/20 10:05
 * @description:
 * 单例模式饿汉式: 在类初始化时，已经自行实例化。
 **/
public class SingletonHunger {

    private final static SingletonHunger INSTANCE = new SingletonHunger();

    private SingletonHunger() {
    }

    /**
     * 饿汉式: 在类初始化时，已经自行实例化。
     * 问题就是：
     *  在类的构造函数中，可能需要依赖于别的类先干的一些事（比如某个配置文件，或是某个被其它类创建的资源）；
     *  希望它能在我第一次getInstance()时才被真正的创建。这样，我们可以控制真正的类创建的时刻，而不是把类的创建委托给了类装载器。
     * @return
     */
    private static SingletonHunger getInstance() {
        return INSTANCE;
    }


}
