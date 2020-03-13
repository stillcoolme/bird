package com.stillcoolme.designpattern.construct.adapter.first;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 20:44
 * Description:
 *  对适配器模式的调用1： 封装有缺陷的接口设计 ThridPartSDK
 */
public class App {

    public static void main(String[] args) {
        ITarget target = new ThirdPartSDKAdapter1();
        // 外部调用就只看到适配器的方法了
        target.function1();
        target.function2();
        target.fucntion3(new ParamsWrapperDefinition(1, 2));
        target.lowPerformanceFunction4();
    }
}
