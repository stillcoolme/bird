package com.stillcoolme.designpattern.construct.adapter.third;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 21:24
 * Description:
 *  对适配器模式的调用3： 替换依赖的外部系统
 *
 *  现在App里面使用的是 SystemA 的服务，后面要换成调用 SystemC的服务，
 *  那么就构造一个 SystemCAdapter implements ISystemA
 *
 */
public class App {

    private ISystemA a;
    public App(ISystemA a) {
        this.a = a;
    }

    public static void main(String[] args) {

        // 原来在我们的项目中，这样调用 外部系统A
        // ISystemA system = new SystemA();

        // 改成这样调 外部系统C，用构造函数注入 SystemC，有点装饰器的味道啊
        ISystemA system = new SystemCAdapter(new SystemC());

        // 调用ISystem接口的这地方都无需改动 !!!!!!!!!!!!
        App app = new App(system);

    }
}
