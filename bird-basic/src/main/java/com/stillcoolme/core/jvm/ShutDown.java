package com.stillcoolme.core.jvm;

import java.sql.Time;

/**
 * Author: stillcoolme
 * Date: 2020/2/6 10:22
 * Description:
 */
public class ShutDown {

    /**
     * Adds the user supplied function as a shutdown hook for cleanup.
     * Also adds a function that sleeps for a second and then halts the
     * runtime to avoid any zombie process in case cleanup function hangs.
     */
    public static void addShutdownHookWithForceKillIn1Sec(Runnable func) {
        addShutdownHookWithForceKillIn1Sec(func, 1);
    }

    /**
     * Utils.addShutdownHookWithDelayedForceKill(worker::shutdown, workerShutdownSleepSecs);
     * @param func
     * @param second
     */
    public static void addShutdownHookWithForceKillIn1Sec(Runnable func, Integer second) {
        final Thread sleepKill = new Thread(() -> {
            try {
                System.out.println("Halting after {} seconds" + second);
                Thread.sleep(second);
                // LOG.warn("Forcing Halt... {}", Utils.threadDump());
                Runtime.getRuntime().halt(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in the ShutDownHook" + e);
            }
        });
        sleepKill.setDaemon(true);

        Thread wrappedFunc = new Thread(() -> {
            func.run();
            sleepKill.interrupt();
        });

        Runtime.getRuntime().addShutdownHook(wrappedFunc);
        Runtime.getRuntime().addShutdownHook(sleepKill);
    }


    // 执行真正的关闭线程池等操作
    public static void shutdownTest() {

    }

    public static void main(String[] args) {
        // 怎么调用？ 以下两种方式：

        // 1 通过传匿名函数调用
        ShutDown.addShutdownHookWithForceKillIn1Sec(() -> {

        });

        // 2 通过写个和Runnable接口的默认方法签名一样的也可以
        ShutDown.addShutdownHookWithForceKillIn1Sec(ShutDown::shutdownTest);

    }

}
