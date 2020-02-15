package com.stillcoolme.framework.self.pool.commonpool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Author: stillcoolme
 * Date: 2020/2/12 16:11
 * Description: 定义 对象工厂
 */
public class ConnectFactory extends BasePooledObjectFactory<Connect> {

    private String ip;
    private Integer port;
    private boolean keepAlive = true;

    public ConnectFactory(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
        this.keepAlive = keepAlive;
    }

    @Override
    public Connect create() throws Exception {
        // 或者new Connect的实现类
        return new Connect(ip, port);
    }

    @Override
    public PooledObject<Connect> wrap(Connect connect) {
        return new DefaultPooledObject<>(connect);
    }


    /**
     * 对象钝化(即：从激活状态转入非激活状态，returnObject时触发）
     *
     * @param pooledObject
     */
    @Override
    public void passivateObject(PooledObject<Connect> pooledObject) {
        if (!keepAlive) {
            // pooledObject.getObject().flush();
        }
    }

    /**
     * 对象激活(borrowObject时触发）
     *
     * @param pooledObject
     */
    @Override
    public void activateObject(PooledObject<Connect> pooledObject) {
        /*if (!pooledObject.getObject().isOpen()) {
            pooledObject.getObject().getTransport().open();
        }*/
    }

}
