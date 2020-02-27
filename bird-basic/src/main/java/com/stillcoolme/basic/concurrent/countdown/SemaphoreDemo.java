package com.stillcoolme.basic.concurrent.countdown;

import lombok.AllArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author: stillcoolme
 * @date: 2020/2/24 13:37
 *
 * Semaphore 表示需要被竞争的量
 * 这里假设只有一个坑位的厕所，被多个使用者竞争使用
 *
 */
@AllArgsConstructor
public class SemaphoreDemo implements Runnable {

    private Integer id;
    private Semaphore position;

    @Override
    public void run() {
        if(position.availablePermits()>0){
            System.out.println("顾客["+this.id+"]进入厕所，有空位");
        } else{
            System.out.println("顾客["+this.id+"]进入厕所，没空位，排队");
        }
        try {
            position.acquire();
            System.out.println("顾客["+this.id+"]获得坑位");
            Thread.sleep((int)(10000));
            System.out.println("顾客["+this.id+"]使用完毕");
            position.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        Semaphore position = new Semaphore(2);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executorService.execute(new SemaphoreDemo(i, position));
        }
        executorService.shutdown();
        position.acquireUninterruptibly(2);
        System.out.println("使用完毕，需要清扫了");
        position.release(1);
    }


}
