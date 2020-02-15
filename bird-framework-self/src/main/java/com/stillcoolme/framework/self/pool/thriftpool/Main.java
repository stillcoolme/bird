package com.stillcoolme.framework.self.pool.thriftpool;

import org.apache.thrift.transport.TSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * Author: stillcoolme
 * Date: 2020/2/15 16:07
 * Description:
 *  参考 https://www.cnblogs.com/yjmyzz/p/thrift-client-pool-demo.html
 *  启动报错 java.net.ConnectException: Connection refused: connect
 *  因为服务端还没启动，所以拒绝客户端连接，那为什么服务端启动不了呢？？？
 */
public class Main {
    public static void main(String[] args) throws Exception {
        //初始化一个连接池（poolsize=15,minsize=1,maxIdleSecond=5,checkInvervalSecond=10）
        ThriftTransportPool thriftTransportPool =
                new ThriftTransportPool(15, 1, 5, 10, getServers());

        createClients(thriftTransportPool);

        //等候清理空闲连接
        Thread.sleep(30000);

        //再模拟一批客户端，验证连接是否会重新增加
        createClients(thriftTransportPool);

        System.out.println("输入任意键退出...");
        System.in.read();
        //销毁连接池
        thriftTransportPool.destory();
    }

    private static void createClients(final ThriftTransportPool pool) throws Exception {

        //模拟5个client端
        int clientCount = 5;

        Thread thread[] = new Thread[clientCount];
        FutureTask<String> task[] = new FutureTask[clientCount];

        for (int i = 0; i < clientCount; i++) {
            task[i] = new FutureTask<String>(() -> {
                TSocket scoket = (TSocket) pool.get();//从池中取一个可用连接
                //模拟调用RPC会持续一段时间
                System.out.println(
                        Thread.currentThread().getName() + " => " + pool.getWrapperInfo(scoket));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pool.release(scoket);
                return Thread.currentThread().getName() + " done.";
            });
            thread[i] = new Thread(task[i], "Thread" + i);
        }

        //启用所有client线程
        for (int i = 0; i < clientCount; i++) {
            thread[i].start();
            Thread.sleep(10);
        }

        System.out.println("--------------");

        //等待所有client调用完成
        for (int i = 0; i < clientCount; i++) {
            System.out.println(task[i].get());
            System.out.println(pool);
            System.out.println("--------------");
            thread[i] = null;
        }
    }

    private static List<ServerInfo> getServers() {
        List<ServerInfo> servers = new ArrayList<ServerInfo>();
        servers.add(new ServerInfo("192.168.43.148", 9000));
        servers.add(new ServerInfo("192.168.43.148", 9001));
        //这一个故意写错的，模拟服务器挂了，连接不上的情景
        servers.add(new ServerInfo("192.168.43.148", 1002));
        return servers;
    }
}
