package com.stillcoolme.basic.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: stillcoolme
 * @date: 2019/12/10 8:38
 * @description:
 *  测试 HashMap 为什么线程不安全 ？
 *  首先 定义个 HashMapThread，两个静态成员变量是全局共享的， new 多个 HashMapThread 都用这两个变量！！
 */
public class MapTest {

    public static void main(String[] args) {
        HashMapThread thread0 = new HashMapThread();
        HashMapThread thread1 = new HashMapThread();
        HashMapThread thread2 = new HashMapThread();
        HashMapThread thread3 = new HashMapThread();
        HashMapThread thread4 = new HashMapThread();
        thread0.start();
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
    }

    static class HashMapThread extends Thread {

        private static AtomicInteger atomicInteger = new AtomicInteger(0);
        private static Map map = new HashMap<>();

        @Override
        public void run() {
            while (atomicInteger.get() < 10000000) {
                System.out.println("i am thread " + Thread.currentThread().getName() + " aa: " + atomicInteger.get());
                map.put(atomicInteger.get(), atomicInteger.get());
                atomicInteger.incrementAndGet();
            }
        }
    }
}
