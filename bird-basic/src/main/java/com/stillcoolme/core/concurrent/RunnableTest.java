package com.stillcoolme.core.concurrent;

/**
 * @author: stillcoolme
 * @date: 2019/12/10 8:21
 * @description:
 *  Runnable类执行run方法 还是在原来那个线程执行啊！
 *  Runnable类自己开不了线程的，你要用个 Thread 来包装着啊！
 */
public class RunnableTest {

    public static void main(String[] args) {
        TestThread testThread = new TestThread();
        new Thread(testThread).start();
        System.out.println("i am main， thread name is: " + Thread.currentThread().getName());
    }

    static class TestThread implements Runnable {
        @Override
        public void run() {
            System.out.println("i am thread， thread name is:  " + Thread.currentThread().getName());
        }
    }
}
