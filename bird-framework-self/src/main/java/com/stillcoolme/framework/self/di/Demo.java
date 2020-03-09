package com.stillcoolme.framework.self.di;

import com.stillcoolme.framework.self.di.impl.ClassPathXmlApplicationContext;
import com.stillcoolme.framework.self.di.model.MyClient;

/**
 * @author: stillcoolme
 * @date: 2020/3/9 10:38
 * Function:
 *  实现简单的 DI 依赖注入框架，通过在配置文件中配置bean，然后解析配置，自动构建bean，没有管理生命周期功能
 */
public class Demo {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("beans.json");
        MyClient client = (MyClient) context.getBean("myClient");
        System.out.println(client.getClient().getName());
    }

}
