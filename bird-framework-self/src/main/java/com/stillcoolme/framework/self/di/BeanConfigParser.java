package com.stillcoolme.framework.self.di;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2020/3/9 10:43
 * Function:
 */
public interface BeanConfigParser {

    public List<BeanDefinition> parse(String configLocation);
}
