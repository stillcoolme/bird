package com.stillcoolme.basic.concurrent.countdown;

import com.stillcoolme.basic.concurrent.SynDemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: stillcoolme
 * Date: 2020/3/4 22:08
 * Description:
 *  编写两个线程，一个线程打印1~25，另一个线程打印字母A~Z，打印顺序为12A34B56C……Y5152Z
 */
public class CyclicBarrierDemo2 {

    private final CyclicBarrier cyclicBarrier;
    private final List list;

    public CyclicBarrierDemo2() {
        this.cyclicBarrier = new CyclicBarrier(2, newBarrierAction());
        this.list = new ArrayList<String>();
    }

    private Runnable newBarrierAction(){
        return new Runnable() {
            @Override
            public void run() {
                Collections.sort(list);
                list.forEach(c->System.out.print(c));
                list.clear();
            }
        };
    }

    public Runnable newThreadOne() {
        final String[] inputArr = Helper.buildNoArr(52);
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < inputArr.length; i += 2) {
                    try {
                        list.add(inputArr[i]);
                        list.add(inputArr[i + 1]);
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public Runnable newThreadTwo() {
        final String[] inputArr = Helper.buildCharArr(26);
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < inputArr.length; i++) {
                    try {
                        list.add(inputArr[i]);
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public static void main(String[] args) {
        CyclicBarrierDemo2 cyclicBarrierDemo2 = new CyclicBarrierDemo2();
        Helper.tPool.execute(cyclicBarrierDemo2.newThreadOne());
        Helper.tPool.execute(cyclicBarrierDemo2.newThreadTwo());
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
