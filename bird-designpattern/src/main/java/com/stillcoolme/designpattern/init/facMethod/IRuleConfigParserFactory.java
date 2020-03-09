package com.stillcoolme.designpattern.init.facMethod;

import com.stillcoolme.designpattern.init.simpleFac.IRuleConfigParser;
import com.stillcoolme.designpattern.init.simpleFac.impl.JsonRuleConfigParser;
import com.stillcoolme.designpattern.init.simpleFac.impl.XmlRuleConfigParser;
import com.stillcoolme.designpattern.init.simpleFac.impl.YamlRuleConfigParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
