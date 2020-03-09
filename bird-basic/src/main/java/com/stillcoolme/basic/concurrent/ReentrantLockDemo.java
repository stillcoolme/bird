package com.stillcoolme.basic.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: stillcoolme
 * Date: 2020/3/4 21:04
 * Description:
 *  使用 ReentrantLock，condition 实现线程间的通信
 *  编写两个线程，一个线程打印1~25，另一个线程打印字母A~Z，打印顺序为12A34B56C……Y5152Z
 */
public class ReentrantLockDemo {

    class ThreadToGo {
        int value = 1;
    }

    private final ThreadToGo threadToGo = new ThreadToGo();

    private Lock lock = new ReentrantLock(true);
    private Condition condition = lock.newCondition();

    public Runnable newThreadOne() {
        final String[] inputArr = SynDemo.Helper.buildNoArr(52);
        return new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < inputArr.length; i += 2) {
                        lock.lock();
                        while (threadToGo.value == 2) {
                            condition.wait();
                        }
                        SynDemo.Helper.print(inputArr[i], inputArr[i + 1]);
                        threadToGo.value = 2;
                        condition.signal();
                    }
                } catch (Exception e) {

                } finally {
                    lock.unlock();
                }

            }
        };
    }

    public Runnable newThreadTwo() {
        final String[] inputArr = SynDemo.Helper.buildCharArr(26);
        return new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < inputArr.length; i++) {
                        lock.lock();
                        while (threadToGo.value == 1) {
                            condition.wait();
                        }
                        SynDemo.Helper.print(inputArr[i], inputArr[i + 1]);
                        threadToGo.value = 1;
                        condition.signal();
                    }
                } catch (Exception e) {

                } finally {
                    lock.unlock();
                }
            }
        };
    }


    public static void main(String[] args) {

    }

    private static class Helper {

        private static final ExecutorService tPool = Executors.newFixedThreadPool(2);

        private String[] buildNoChar(int max) {
            String[] noArr = new String[max];
            for (int i = 0; i < max; i++) {
                noArr[i] = Integer.toString(i + 1);
            }
            return noArr;
        }

        public static String[] buildCharArr(int max) {
            String[] charArr = new String[max];
            int tmp = 65;
            for (int i = 0; i < max; i++) {
                charArr[i] = String.valueOf((char) (tmp + i));
            }
            return charArr;
        }

        private static void print(String... input) {
            if (input == null)
                return;
            for (String each : input) {
                System.out.print(each);
            }
        }
    }
}
