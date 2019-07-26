package com.stillcoolme.core.concurrent.guava;


import com.google.common.util.concurrent.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;


/**
 * ListenableFuture是非阻塞的，会先执行下面的逻辑
 * Author: stillcoolme
 * Date: 2019/7/25 19:35
 * Description:
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
                System.err.println("StringTask: " + result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });

        // 执行时间
        System.err.println("time: " + (System.currentTimeMillis() - t1));
        System.out.println("后面的逻辑。。。");
    }

}
