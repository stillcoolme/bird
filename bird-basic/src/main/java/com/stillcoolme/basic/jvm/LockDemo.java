package com.stillcoolme.basic.jvm;

/**
 * Author: stillcoolme
 * Date: 2020/2/29 19:42
 * Description:
 *  通过 jconsole 看线程状态
 */
public class LockDemo {

    /**
     * 线程锁等待演示
     *
     * lock-thread 线程会处于正常的活锁等待，
     * 只要 object 对象的notify()被调用，线程就能继续执行
     *
     * @param object
     */
    public static void createLockThread(Object object) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (object) {
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "lock-thread").start();
    }

    /**
     * 死锁，处于 Blocked状态
     */
    static class SynAddRunnable implements Runnable {
        int a, b;
        public SynAddRunnable(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public void run() {
            synchronized (Integer.valueOf(a)) {
                synchronized (Integer.valueOf(b)) {
                    System.out.println(a + b);
                }
            }
        }
    }

    public static void main(String[] args) {
        createLockThread(new String("test"));

        for (int i = 0; i < 100; i++) {
            new Thread(new SynAddRunnable(1, 2)).start();
            new Thread(new SynAddRunnable(2,1 )).start();
        }
    }
}
