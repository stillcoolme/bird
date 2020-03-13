package com.stillcoolme.designpattern.establish.facMethod;

import com.stillcoolme.designpattern.establish.simpleFac.IRuleConfigParser;

/**
 * Author: stillcoolme
 * Date: 2020/3/8 21:07
 * Description:
 *  工厂方法 把 工厂弄成接口了！！ 具体工厂单独写成类！！
 *  这样当我们新增一种 parser 的时候，
 *  只需要新增一个实现了 IRuleConfigParserFactory 接口的 Factory 类即可。
 *  所以，工厂方法模式比起简单工厂模式更加符合开闭原则。
 *
 *  关键看怎么使用！！
 */
public interface IRuleConfigParserFactory {

    public IRuleConfigParser createParser();

}
