package com.stillcoolme.framework.self.di.impl;

import com.stillcoolme.framework.self.di.ApplicationContext;
import com.stillcoolme.framework.self.di.BeanConfigParser;
import com.stillcoolme.framework.self.di.BeanDefinition;
import com.stillcoolme.framework.self.di.BeansFactory;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2020/3/9 10:38
 * Function:
 */
public class ClassPathXmlApplicationContext implements ApplicationContext {

    private BeansFactory beansFactory;
    private BeanConfigParser beanConfigParser;

    public ClassPathXmlApplicationContext(String configLocation) {
        this.beansFactory = new BeansFactory();
        this.beanConfigParser = new XmlBeanConfigParser();
        loadBeanDefinitions(configLocation);
    }

    /**
     * 从 classpath 中加载配置文件，通过 BeanConfigParser 解析为统一的 BeanDefinition 格式，
     * 然后 BeansFactory 根据 BeanDefinition 来创建对象
     * @param configLocation
     */
    private void loadBeanDefinitions(String configLocation) {
        List<BeanDefinition> beanDefinitions = beanConfigParser.parse(configLocation);
        beansFactory.addBeanDefinitions(beanDefinitions);
    }

    @Override
    public Object getBean(String beanId) {
        return beansFactory.getBean(beanId);
    }
}
