package com.stillcoolme.basic.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * JDK自带的Future是阻塞的，不会先执行下面的逻辑
 * Author: stillcoolme
 * Date: 2019/7/26 9:38
 * Description:
 */
public class FutureTest {

    public static class Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("execute!!!");
            Thread.sleep(2000);
            return "complete";
        }
    }

    public static void main(String[] args) throws InterruptedException,
            ExecutionException {
        List<Future<String>> results = new ArrayList<Future<String>>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            results.add(executorService.submit(new Task()));
        }
        for (Future<String> future : results) {
            System.out.println(future.get());
        }

        System.out.println("Main complete");

        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

