package com.stillcoolme.framework.self.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

class App implements Runnable{
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Object> queue = new ArrayBlockingQueue<>(4);
        CustomThreadPool pool =
                new CustomThreadPool(3, 5, 1, TimeUnit.SECONDS, queue, null);
        for (int i = 0; i < 20; i++) {
            pool.execute(new App(i));
        }
        System.out.println("活跃线程数： " + pool.getWorkerCount());
        TimeUnit.SECONDS.sleep(5);
        System.out.println("活跃线程数： " + pool.getWorkerCount());
        for (int i = 21; i < 30; i++) {
            pool.execute(new App(i));
        }
        TimeUnit.SECONDS.sleep(5);
        System.out.println("活跃线程数： " + pool.getWorkerCount());

        pool.shutDown();
    }

    private Integer num;

    public App(Integer num) {
        this.num = num;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " 业务线程逻辑执行中。。。。" + num);
    }
}