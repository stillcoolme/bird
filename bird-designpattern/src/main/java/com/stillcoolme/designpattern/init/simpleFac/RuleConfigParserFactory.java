package com.stillcoolme.designpattern.init.simpleFac;

import com.stillcoolme.designpattern.init.simpleFac.impl.JsonRuleConfigParser;
import com.stillcoolme.designpattern.init.simpleFac.impl.XmlRuleConfigParser;
import com.stillcoolme.designpattern.init.simpleFac.impl.YamlRuleConfigParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: stillcoolme
 * Date: 2020/3/8 21:07
 * Description:
 *  简单工厂模式，也叫静态工厂方法模式
 */
public class RuleConfigParserFactory {

    public static IRuleConfigParser createParser(String configFormat) {
        IRuleConfigParser parser = null;
        if ("json".equalsIgnoreCase(configFormat)) {
            parser = new JsonRuleConfigParser();
        } else if ("xml".equalsIgnoreCase(configFormat)) {
            parser = new XmlRuleConfigParser();
        } else if ("yaml".equalsIgnoreCase(configFormat)) {
            parser = new YamlRuleConfigParser();
        }
        return parser;
    }

    /**
     * 在上面的代码实现中，我们每次调用 RuleConfigParserFactory 的 createParser() 的时候，都要创建一个新的 parser。
     * 实际上，**如果** parser 可以复用，为了节省内存和对象创建的时间，
     * 我们可以将 parser 事先创建好缓存起来。
     * 当调用 createParser() 函数的时候，我们从缓存中取出 parser 对象直接使用。
     * 这有点类似单例模式和简单工厂模式的结合。
     * @param configFormat
     * @return
     */
    // 注册式的简单工厂
    public static IRuleConfigParser createParser2(String configFormat) {
        if(configFormat == null || configFormat.isEmpty()) {
            return null;    //返回null还是IllegalArgumentException全凭你自己说了算
        }
        IRuleConfigParser ruleConfigParser =  cachedParsers.get(configFormat.toLowerCase());
        return ruleConfigParser;
    }

    private static final Map<String, IRuleConfigParser> cachedParsers = new ConcurrentHashMap();
    static {
        cachedParsers.put("json", new JsonRuleConfigParser());
        cachedParsers.put("xml", new XmlRuleConfigParser());
        cachedParsers.put("yaml", new YamlRuleConfigParser());
    }

}
