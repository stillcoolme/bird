package com.stillcoolme.core.concurrent.countdown;

import java.util.concurrent.*;

/**
 * @author: stillcoolme
 * @date: 2019/9/6 15:55
 * @description:
 *
 * CyclicBarrier也是一种多线程并发控制的实用工具，相比于CountDownLatch可以实现多次的并行执行。
 *
 * CountDownLatch 和 CyclicBarrier 两者各有不同侧重点的：
 * 1. CountDownLatch强调一个线程等多个线程完成某件事情。
 *  CyclicBarrier是多个线程互等，到都完成，再携手同时执行。
 *
 * 2. 调用CountDownLatch的countDown方法后，当前线程并不会阻塞，会继续往下执行；
 *  而调用CyclicBarrier的await方法，会阻塞当前线程，直到CyclicBarrier指定的线程全部都到达了指定点的时候，才能继续往下执行；
 *
 * 4. CountDownLatch方法比较少，操作比较简单，而CyclicBarrier提供的方法更多，比如能够通过getNumberWaiting()，isBroken()这些方法获取当前多个线程的状态，
 *  并且CyclicBarrier的构造方法可以传入barrierAction，指定当所有线程都到达时执行的业务功能；
 *
 * 5. CountDownLatch是不能复用的，而CyclicBarrier是可以复用的。
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
