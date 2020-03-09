package com.stillcoolme.basic.concurrent;

import java.util.concurrent.CountDownLatch;

/**
 * @author: stillcoolme
 * @date: 2020/3/6 10:39
 * Function:
 *  测试重排序问题！
 *  很容易想到这段代码的运行结果可能为(1,0)、(0,1)或(1,1)，因为线程one可以在线程two开始之前就执行完了，也有可能反之，甚至有可能二者的指令是同时或交替执行的。
 *  但几秒后也可以得到 x == 0 && y == 0 这个结果，
 *  事实上，输出了这一结果，并不代表一定发生了指令重排序，内存可见性问题也会导致这样的输出 ！！！
 */
public class ReSortDemo {
    // 加个 volatile 就可以避免
    private volatile static int x = 0, y = 0;
    private volatile static int a = 0, b =0;

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        for(;;) {
            i++;
            x = 0; y = 0;
            a = 0; b = 0;

            CountDownLatch countDownLatch = new CountDownLatch(1);

            Thread thread1 = new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 编译器是可以将 a = 1 和 x = b 换一下顺序的
                a = 1;
                x = b;
            });
            Thread thread2 = new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                }
                b = 1;
                y = a;
            });
            thread1.start();
            thread2.start();
            // 启动线程
            countDownLatch.countDown();
            thread1.join();
            thread2.join();

            String result = "第" + i + "次 (" + x + "," + y + "）";
            if(x == 0 && y == 0) {
                System.err.println(result);
                break;
            } else {
                System.out.println(result);
            }
        }

    }

}
