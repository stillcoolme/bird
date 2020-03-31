package com.stillcoolme.framework.disruptor;

/**
 * @author: stillcoolme
 * @date: 2020/3/26 22:32
 * Function:
 *  参考：https://juejin.im/post/5b744557518825612a228111
 *
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        DisruptorManager.init(new DataEventHandler());
        for (long l = 0; true; l++) {
            DisruptorManager.putDataToQueue(l);
            Thread.sleep(1000);
        }
    }
}
