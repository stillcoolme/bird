package com.stillcoolme.basic.concurrent.countdown;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author: stillcoolme
 * @date: 2020/3/6 16:53
 * Function:
 */
public class SemaphoreDemo2 implements Runnable {

    Semaphore semaphore;

    public SemaphoreDemo2(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            if(semaphore.availablePermits() > 1) {

            }
            semaphore.acquire(2);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        // 表示有5个叉子
        Semaphore semaphore = new Semaphore(5);
        // 表示5个哲学家
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.execute(new SemaphoreDemo2(semaphore));

    }
}
