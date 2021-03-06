package com.stillcoolme.designpattern.establish.simpleFac;

import com.stillcoolme.designpattern.model.Client;

/**
 * @author: stillcoolme
 * @date: 2019/10/25 13:50
 * @description: effective java 里建议 使用静态工厂方法替代构造方法
 *  优点：
 *  1. 静态工厂方法相对于构造函数有具体的名字
 *  2. 静态工厂方法每次被调用时可以不需要创建新的对象, 单例模式
 *  3. 静态工厂方法可以返回静态工厂函数返回类型的子类型，即多态
 *  缺点：
 *  1. 静态工厂方法构造的类如果没有公有或者受保护的构造器，那么类就不能被继承。
 *  2. 我们不太容易在类文档中找到静态工厂函数，所以尽可能遵守命名规范
 */
public class StaticFactoryMethod {

    // ================================================
    // 方案一
    // ================================================
    /**
     * 接受参数的静态工厂方法
     * @param clientType
     * @return
     */
    public static Client valueOf(int clientType) {
        if(clientType == 1) {
            return new Client("tempClient");
        } else {
            return new Client("realClient");
        }
    }


    // ================================================
    // 方案二
    // ================================================
    /**
     * 获取单例对象
     * * 这种没法传参数进来，需要的参数要通过配置文件读取
     */
    public static Client getClientInstance() {
        return Holder.client;
    }

    /**
     * 静态内部持有类
     */
    private static class Holder{
        private static Client client;
        static {
            client = new Client("testClient");
            // 或者 client = clientBuilder();  再封装client构造逻辑。。。
        }
    }

}
