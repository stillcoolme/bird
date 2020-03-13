package com.stillcoolme.designpattern.establish.facMethod;

import com.stillcoolme.designpattern.establish.facMethod.impl.JsonRuleConfigParserFactory;
import com.stillcoolme.designpattern.establish.facMethod.impl.XmlRuleConfigParserFactory;
import com.stillcoolme.designpattern.establish.facMethod.impl.YamlRuleConfigParserFactory;
import com.stillcoolme.designpattern.establish.simpleFac.IRuleConfigParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: stillcoolme
 * Date: 2020/3/8 22:08
 * Description:
 *  使用类
 */
public class RuleConfigSource {

    /**
     * 使用版本1： 差劲
     *   工厂类对象的创建逻辑又耦合进了 load() 函数中，跟我们最初的简单工厂代码版本非常相似，
     *   引入工厂方法非但没有解决问题，反倒让设计变得更加复杂了。
     * @return
     */
    public Object loadRuleConfig() {
        String ruleConfigFileExtension = "json";

        IRuleConfigParserFactory parserFactory = null;

        if ("json".equalsIgnoreCase(ruleConfigFileExtension)) {
            parserFactory = new JsonRuleConfigParserFactory();
        } else if ("xml".equalsIgnoreCase(ruleConfigFileExtension)) {
            parserFactory = new XmlRuleConfigParserFactory();
        } else if ("yaml".equalsIgnoreCase(ruleConfigFileExtension)) {
            parserFactory = new YamlRuleConfigParserFactory();
        } else {
            throw new IllegalArgumentException("Rule config file format is not support");
        }
        IRuleConfigParser parser = parserFactory.createParser();

        return null;
    }

    // =======================================

    /**
     * 使用版本2：
     *  我们可以为工厂类再创建一个简单工厂，也就是工厂的工厂，用来创建工厂类对象。
     * @return
     */
    public Object loadRuleConfig2() {
        String ruleConfigFileExtension = "json";

        IRuleConfigParserFactory parserFactory =
                RuleConfigParserFactoryMap.getFactory(ruleConfigFileExtension);
        IRuleConfigParser parser = parserFactory.createParser();

        return null;
    }

    /**
     * 因为工厂类只包含方法，不包含成员变量，完全可以复用！！
     * 不需要每次都创建新的工厂类对象，所以，简单工厂模式的第二种实现思路更加合适。
     */
    private static class RuleConfigParserFactoryMap {
        private static final Map<String, IRuleConfigParserFactory> cachedFactories = new ConcurrentHashMap();
        static {
            cachedFactories.put("json", new JsonRuleConfigParserFactory());
            cachedFactories.put("xml", new XmlRuleConfigParserFactory());
            cachedFactories.put("yaml", new YamlRuleConfigParserFactory());
        }
        private static IRuleConfigParserFactory getFactory(String type) {
            if (type == null || type.isEmpty()) {
                return null;
            }
            IRuleConfigParserFactory fatory = cachedFactories.get(type);
            return fatory;
        }
    }

}
