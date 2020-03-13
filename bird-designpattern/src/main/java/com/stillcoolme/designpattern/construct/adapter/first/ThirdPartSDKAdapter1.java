package com.stillcoolme.designpattern.construct.adapter.first;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 20:34
 * Description:
 *  通过组合的方式实现适配
 */
public class ThirdPartSDKAdapter1 implements ITarget {

    private ThirdPartSDK thirdPartSDK;

    @Override
    public void function1() {
        thirdPartSDK.staticFunction1();
    }

    @Override
    public void function2() {
        thirdPartSDK.uglyNamingFunction2();
    }

    @Override
    public void fucntion3(ParamsWrapperDefinition paramsWrapper) {
        thirdPartSDK.tooManyParamsFunction3(paramsWrapper.getParamA(), paramsWrapper.getParamB());
    }

    @Override
    public void lowPerformanceFunction4() {
        thirdPartSDK.lowPerformanceFunction4();
    }

}
