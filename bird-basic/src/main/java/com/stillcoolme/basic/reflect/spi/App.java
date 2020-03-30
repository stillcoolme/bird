package com.stillcoolme.basic.reflect.spi;

import java.util.ServiceLoader;

/**
 * @author: stillcoolme
 * @date: 2020/3/25 15:30
 * Function:
 *   ServiceLoader是SPI的是一种实现，所谓SPI，即Service Provider Interface，用于一些服务提供给第三方实现或者扩展，可以增强框架的扩展或者替换一些组件。
 *   注意：要将实现类名称配置在 实现类项目 的固定目录下： resources/META-INF/services/接口全称。
 *
 *   这个在大数据的应用中颇为广泛，
 *   比如 spark sql 的外部集群管理器接口 ExternalClusterManager、数据源的接入接口DataSourceRegister
 *
 *   https://cloud.tencent.com/developer/article/1110670
 */
public class App {

    static ServiceLoader<IDoSomething> loader = ServiceLoader.load(IDoSomething.class);
    public static void main(String[] args) {
        for(IDoSomething impl: loader) {
            // 实现对用户自定义实现 DoSomethingImpl 的调用
            if (impl.shortName().equalsIgnoreCase("DoSomethingImpl")) {
                impl.doSomething();
            }
        }


    }

}
