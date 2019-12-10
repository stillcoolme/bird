package com.stillcoolme;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
public class Test {

    public static void main(String[] args) {
        TestThread testThread = new TestThread();
        new Thread(testThread).start();
        System.out.println("i am main " + Thread.currentThread().getName());
    }

    static class TestThread implements Runnable {

        @Override
        public void run() {
            System.out.println("i am thread " + Thread.currentThread().getName());
        }
    }
}
