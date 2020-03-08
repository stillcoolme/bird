package com.stillcoolme.designpattern.init.facMethod.impl;

import com.stillcoolme.designpattern.init.facMethod.IRuleConfigParserFactory;
import com.stillcoolme.designpattern.init.simpleFac.IRuleConfigParser;
import com.stillcoolme.designpattern.init.simpleFac.impl.XmlRuleConfigParser;

/**
 * Author: stillcoolme
 * Date: 2020/3/8 21:59
 * Description:
 */
public class XmlRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParser createParser() {
        return new XmlRuleConfigParser();
    }
}
