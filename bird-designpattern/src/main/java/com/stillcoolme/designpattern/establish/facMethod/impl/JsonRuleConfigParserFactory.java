package com.stillcoolme.designpattern.establish.facMethod.impl;

import com.stillcoolme.designpattern.establish.facMethod.IRuleConfigParserFactory;
import com.stillcoolme.designpattern.establish.simpleFac.IRuleConfigParser;
import com.stillcoolme.designpattern.establish.simpleFac.impl.JsonRuleConfigParser;

/**
 * Author: stillcoolme
 * Date: 2020/3/8 21:59
 * Description:
 */
public class JsonRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParser createParser() {
        return new JsonRuleConfigParser();
    }
}
