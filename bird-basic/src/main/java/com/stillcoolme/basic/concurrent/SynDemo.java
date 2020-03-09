package com.stillcoolme.basic.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: stillcoolme
 * Date: 2020/3/4 20:30
 * Description:
 *  使用 Synchronized，wait, notify 实现线程间的通信
 *  编写两个线程，一个线程打印1~25，另一个线程打印字母A~Z，打印顺序为12A34B56C……Y5152Z
 */
public class SynDemo {

    class ThreadToGo {
        int value = 1;
    }

    private final ThreadToGo threadToGo = new ThreadToGo();

    public Runnable newThreadOne() {
        final String[] inputArr = Helper.buildNoArr(52);
        return new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < inputArr.length; i += 2) {
                        synchronized (threadToGo) {
                            while (threadToGo.value == 2) {
                                threadToGo.wait();
                            }
                            Helper.print(inputArr[i], inputArr[i + 1]);
                            threadToGo.value = 2;
                            threadToGo.notify();
                        }
                    }
                } catch (Exception e) {

                }

            }
        };
    }

    public Runnable newThreadTwo() {
        final String[] inputArr = Helper.buildCharArr(26);
        return new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < inputArr.length; i++) {
                        synchronized (threadToGo) {
                            while (threadToGo.value == 1) {
                                threadToGo.wait();
                            }
                            Helper.print(inputArr[i], inputArr[i + 1]);
                            threadToGo.value = 1;
                            threadToGo.notify();
                        }
                    }
                } catch (Exception e) {

                }
            }
        };
    }

    public static void main(String[] args) {
        SynDemo synTest = new SynDemo();
        Runnable runnable1 = synTest.newThreadOne();
        Runnable runnable2 = synTest.newThreadTwo();
        Helper.tPool.execute(runnable1);
        Helper.tPool.execute(runnable2);
        Helper.tPool.shutdown();
    }

    static class Helper {
        private static final ExecutorService tPool = Executors.newFixedThreadPool(2);

        public static String[] buildNoArr(int max) {
            String[] noArr = new String[max];
            for(int i=0;i<max;i++){
                noArr[i] = Integer.toString(i+1);
            }
            return noArr;
        }

        public static String[] buildCharArr(int max) {
            String[] charArr = new String[max];
            int tmp = 65;
            for(int i=0;i<max;i++){
                charArr[i] = String.valueOf((char)(tmp+i));
            }
            return charArr;
        }

        public static void print(String... input){
            if (input == null)
                return;
            for (String each : input) {
                System.out.print(each);
            }
        }
    }
}


