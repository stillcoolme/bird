package com.stillcoolme.basic.concurrent.countdown;

import java.util.concurrent.*;

/**
 * @author: stillcoolme
 * @date: 2019/9/6 15:55
 * @description:
 *
 */
public class CyclicBarrierDemo {

    //指定必须有6个运动员到达才行
    private static CyclicBarrier cyclicBarrier = new CyclicBarrier(6, () -> {
        System.out.println("所有任务到达起始线，开始同时执行！！！！！");
    });

    public static void main(String[] args) {
        System.out.println("任务准备开始............");
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 6; i++) {
            executorService.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 任务准备...");
                    cyclicBarrier.await();
                    System.out.println(Thread.currentThread().getName() + "  任务第一次同时执行！！");
                    cyclicBarrier.await();
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + "  任务第二次同时执行！！");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
