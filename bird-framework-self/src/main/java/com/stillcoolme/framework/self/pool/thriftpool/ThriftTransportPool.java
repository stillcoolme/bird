package com.stillcoolme.framework.self.pool.thriftpool;

import com.sun.javafx.logging.JFRInputEvent;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Author: stillcoolme
 * Date: 2020/2/14 11:06
 * Description: Thrift连接池
 */
public class ThriftTransportPool {

    Semaphore access = null;
    TransfortWrapper[] pool = null;
    // 连接池大小
    int poolSize;
    // 池中保持激活的最少连接数， minSize <= poolSize
    int minSize;
    int maxSize;
    // 单个连接最大空闲时间，超过此值的连接将被断开
    int maxIdleSecond = 300;
    boolean allowCheck = true;
    // 每隔多少秒检查一次空闲连接 ms
    int checkInvervalSecond = 60;
    Thread checkThread;
    List<ServerInfo> serverInfos;

    /**
     * 连接池构造函数
     *
     * @param poolSize            连接池大小
     * @param minSize             池中保持激活的最少连接数
     * @param maxIdleSecond       单个连接最大空闲时间，超过此值的连接将被断开
     * @param checkInvervalSecond 每隔多少秒检查一次空闲连接
     * @param serverList          服务器列表
     */
    public ThriftTransportPool(int poolSize, int minSize,
                               int maxIdleSecond, int checkInvervalSecond,
                               List<ServerInfo> serverList) {
        if (poolSize <= 0) {
            poolSize = 1;
        }
        if (minSize > poolSize) {
            minSize = poolSize;
        }
        if (minSize <= 0) {
            minSize = 0;
        }
        this.maxIdleSecond = maxIdleSecond;
        this.minSize = minSize;
        this.poolSize = poolSize;
        this.serverInfos = serverList;
        this.allowCheck = true;
        this.checkInvervalSecond = checkInvervalSecond;
        init();
        check();
    }

    /**
     * 连接池构造函数（默认最大空闲时间300秒）
     *
     * @param poolSize   连接池大小
     * @param minSize    池中保持激活的最少连接数
     * @param serverList 服务器列表
     */
    public ThriftTransportPool(int poolSize, int minSize, List<ServerInfo> serverList) {
        this(poolSize, minSize, 300, 60, serverList);
    }

    public ThriftTransportPool(int poolSize, List<ServerInfo> serverList) {
        this(poolSize, 1, 300, 60, serverList);
    }

    public ThriftTransportPool(List<ServerInfo> serverList) {
        this(serverList.size(), 1, 300, 60, serverList);
    }

    /**
     * 初始化连接池
     */
    private void init() {
        // 一个要被争夺的信号量资源
        access = new Semaphore(poolSize);
        pool = new TransfortWrapper[poolSize];

        for (int i = 0; i < pool.length; i++) {
            int j = i % serverInfos.size();
            TSocket socket = new TSocket(serverInfos.get(j).getHost(),
                    serverInfos.get(j).getPort());
            if(i < minSize) {
                pool[i] = new TransfortWrapper(socket, serverInfos.get(j).getHost(), serverInfos.get(j).getPort(), true);
            } else {
                // 超过激活数不open
                pool[i] = new TransfortWrapper(socket, serverInfos.get(j).getHost(), serverInfos.get(j).getPort());
            }
        }

    }

    /**
     * 检查空闲连接
     */
    private void check() {
        checkThread = new Thread(() -> {
            while (allowCheck) {
                System.out.println("开始检测空闲连接...");
                for (int i = 0; i < pool.length; i++) {
                    if(pool[i].isAvailable() && pool[i].getLastUseTime() != null) {
                        long idleTime = new Date().getTime() - pool[i].getLastUseTime().getTime();
                        // 空闲太久，关闭连接
                        if(idleTime > maxIdleSecond * 1000) {
                            if(getActiveCount() > minSize) {
                                pool[i].getTransport().close();
                                pool[i].setBusy(false);
                                System.out.println(pool[i].hashCode() + "," + pool[i].getHost() + ":" + pool[i].getPort() + " 超过空闲时间阀值被断开！");
                            }
                        }
                    }
                }
                System.out.println("当前活动连接数：" + getActiveCount());
                try {
                    Thread.sleep(checkInvervalSecond * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        checkThread.start();
    }

    /**
     * 从池中取一个可用连接
     *   是否有空闲的连接
     *   没有空闲的连接的话，现在是否超过最大连接数，没有超过则创建一个
     *
     * @return
     */
    public TTransport get() {
        try {
            // 并发控制，多个线程同时调用get()想从池中取可用连接时，可用Semaphore+Lock的机制来加以控制
            if(access.tryAcquire(3, TimeUnit.SECONDS)) {
                synchronized (this) {
                    for (int i = 0; i < pool.length; i++) {
                        if(pool[i].isAvailable()) {
                            pool[i].setBusy(true);
                            pool[i].setLastUseTime(new Date());
                            return pool[i].getTransport();
                        }
                    }
                    //尝试激活更多连接
                    for (int i = 0; i < pool.length; i++) {
                        if (!pool[i].isBusy() && !pool[i].isDead()
                                && !pool[i].getTransport().isOpen()) {
                            try {
                                pool[i].getTransport().open();
                                pool[i].setBusy(true);
                                pool[i].setLastUseTime(new Date());
                                return pool[i].getTransport();
                            } catch (Exception e) {
                                //e.printStackTrace();
                                System.err.println(pool[i].getHost() + ":" + pool[i].getPort() + " " + e.getMessage());
                                pool[i].setDead(true);
                            }
                        }
                    }
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("can not get available client");
        }
        throw new RuntimeException("all client is too busy");
    }

    /**
     * 客户端调用完成后，必须手动调用此方法，将TTransport恢复为可用状态
     *
     * @param client
     */
    public void release(TTransport client) {
        boolean released = false;
        synchronized (this) {
            for (int i = 0; i < pool.length; i++) {
                if(client == pool[i].getTransport() && pool[i].isBusy()) {
                    pool[i].setBusy(false);
                    released = true;
                    break;
                }
            }
        }
        if (released) {
            access.release();
        }
    }

    public void destory() {
        if (pool != null) {
            for (int i = 0; i < pool.length; i++) {
                pool[i].getTransport().close();
            }
        }
        allowCheck = false;
        checkThread = null;
        System.out.print("连接池被销毁！");
    }


    private int getActiveCount() {
        int result = 0;
        for (int i = 0; i < pool.length; i++) {
            if (!pool[i].isDead() && pool[i].getTransport().isOpen()) {
                result += 1;
            }
        }
        return result;
    }

    /**
     * 获取当前繁忙状态的连接数
     *
     * @return
     */
    public int getBusyCount() {
        int result = 0;
        for (int i = 0; i < pool.length; i++) {
            if (!pool[i].isDead() && pool[i].isBusy()) {
                result += 1;
            }
        }
        return result;
    }

    /**
     * 获取当前已"挂"掉连接数
     *
     * @return
     */
    public int getDeadCount() {
        int result = 0;
        for (int i = 0; i < pool.length; i++) {
            if (pool[i].isDead()) {
                result += 1;
            }
        }
        return result;
    }

    public String toString() {
        return "poolsize:" + pool.length +
                ",minSize:" + minSize +
                ",maxIdleSecond:" + maxIdleSecond +
                ",checkInvervalSecond:" + checkInvervalSecond +
                ",active:" + getActiveCount() +
                ",busy:" + getBusyCount() +
                ",dead:" + getDeadCount();
    }

    public String getWrapperInfo(TTransport client) {
        for (int i = 0; i < pool.length; i++) {
            if (pool[i].getTransport() == client) {
                return pool[i].toString();
            }
        }
        return "";
    }

}
