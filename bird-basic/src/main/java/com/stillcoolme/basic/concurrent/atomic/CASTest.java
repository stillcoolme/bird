package com.stillcoolme.basic.concurrent.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: create by stillcoolme
 * @description: com.stillcoolme.basic.concurrent.atomic
 * @date:2019/6/13
 **/
public class CASTest implements Runnable{
    AtomicInteger data;
    int int_data;
    int count = 0;

    public CASTest(AtomicInteger data){
        this.data = data;
    }

    public CASTest(int data){
        this.int_data = data;
    }

    public static void main(String[] args) {
        AtomicInteger data = new AtomicInteger(10);
        int int_data = 10;
        Thread thread1 = new Thread(new CASTest(int_data), "thread1");
        Thread thread2 = new Thread(new CASTest(int_data), "thread2");
        Thread thread3 = new Thread(new CASTest(int_data), "thread3");
        thread1.start();
        thread2.start();
        thread3.start();
    }

//    @Override
//    public void run() {
//        while (count < 10){
//            System.out.println(Thread.currentThread().getName() + " " + data.decrementAndGet());
//            count ++;
//        }
//    }

    @Override
    public void run() {
        while (count < 10){
            System.out.println(Thread.currentThread().getName() + " " + int_data);
            count ++;
        }
    }
}
