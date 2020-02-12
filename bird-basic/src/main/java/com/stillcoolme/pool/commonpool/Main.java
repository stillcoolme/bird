package com.stillcoolme.pool.commonpool;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author: stillcoolme
 * Date: 2020/2/12 21:07
 * Description: 测试 对象池 的使用
 * 需要从对象池取一个对象时，调用borrowObject（背后会调用activeObject激活对象），
 * 类似的，对象使用完之后，需要调用returnObject将对象放回对象池（背后会调用passivateObject使对象钝化）
 */
public class Main {

    public static void main(String[] args) throws Exception {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);

        ObjectPool<Connect> objectPool = new ConnectPool(
                new ConnectFactory("172.161.1.1", 9091),
                poolConfig
        );

        List<Connect> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Connect connect = objectPool.borrowObject();
            System.out.println(connect.toString());
            if (i % 2 == 0) {
                //10个连接中，将偶数归还
                objectPool.returnObject(connect);
            } else {
                list.add(connect);
            }
        }

        Random rnd = new Random();
        while (true) {
            System.out.println(String.format("active:%d, idel:%d",
                    objectPool.getNumActive(),
                    objectPool.getNumIdle()));
            Thread.sleep(5000);
            //每次还一个
            if (list.size() > 0) {
                int i = rnd.nextInt(list.size());
                objectPool.returnObject(list.get(i));
                list.remove(i);
            }
            //直到全部还完
            if (objectPool.getNumActive() <= 0) {
                break;
            }
        }

    }
}
