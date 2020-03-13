package com.stillcoolme.designpattern.construct.adapter.first;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 20:26
 * Description:
 *   对于这个第三方的SDK，方法很烂，我们无法改动其接口，就可以用 适配器模式 来封装
 */
public class ThirdPartSDK {

    //...
    public static void staticFunction1() { //...

    }

    public void uglyNamingFunction2() { //...

    }

    public void tooManyParamsFunction3(int paramA, int paramB) {
        //...
    }

    public void lowPerformanceFunction4() {

    }

}
