package com.stillcoolme.framework.self.pool.commonpool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Author: stillcoolme
 * Date: 2020/2/12 16:43
 * Description:
 * common-pools提供了对象池的默认实现：GenericObjectPool
 * 但是默认对象池中，对于处于空闲的对象，需要手动调用clear来释放空闲对象，
 * 如果希望改变这一行为，可以自己派生自己的子类，重写returnObject方法
 */
public class ConnectPool extends GenericObjectPool<Connect> {
    /**
     * @param factory 传入 对象池工厂
     */
    public ConnectPool(PooledObjectFactory<Connect> factory) {
        super(factory);
    }

    public ConnectPool(PooledObjectFactory<Connect> factory, GenericObjectPoolConfig config) {
        super(factory);
    }

    @Override
    public void returnObject(Connect obj) {
        super.returnObject(obj);
        //空闲数 >=激活数时，清理掉空闲连接
        if (getNumIdle() >= getNumActive()) {
            clear();
        }
    }
}
