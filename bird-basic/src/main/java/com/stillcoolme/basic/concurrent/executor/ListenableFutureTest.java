package com.stillcoolme.basic.concurrent.executor;


import com.google.common.util.concurrent.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;


/**
 * Author: stillcoolme
 * Date: 2019/7/25 19:35
 * Description:
 *  Guava的 ListenableFuture 是异步，非阻塞的，会先执行下面的逻辑
 *
 *  http://ifeve.com/google-guava-listenablefuture/
 */
public class ListenableFutureTest {
    //定义一个线程池，用于处理所有任务
    final static ListeningExecutorService service
            = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    public static void main(String[] args){
        Long t1 = System.currentTimeMillis();

        // 任务2
        ListenableFuture<String> stringTask = service.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(10000);
                return "Hello World";
            }
        });

        Futures.addCallback(stringTask, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("获取StringTask结果: " + result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });

        // 执行时间
        System.out.println("time: " + (System.currentTimeMillis() - t1));

        System.out.println("后面的业务逻辑...");
    }

}
