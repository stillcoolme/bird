package com.stillcoolme.designpattern.construct.adapter.first;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 20:32
 * Description:
 *  适配器模式接口
 */
public interface ITarget {

    void function1();

    void function2();

    void fucntion3(ParamsWrapperDefinition paramsWrapper);

    void lowPerformanceFunction4();

}
