package com.stillcoolme.designpattern.construct.adapter.first;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 20:35
 * Description:
 *   通过继承的方式实现适配
 */
public class ThirdPartSDKAdapter2 extends ThirdPartSDK implements ITarget {

    @Override
    public void function1() {
        super.staticFunction1();
    }

    @Override
    public void function2() {
        super.uglyNamingFunction2();
    }

    @Override
    public void fucntion3(ParamsWrapperDefinition paramsWrapper) {
        super.tooManyParamsFunction3(paramsWrapper.getParamA(), paramsWrapper.getParamB());
    }

    // 对于 lowPerformanceFunction4() 可以直接复用父类方法

}
