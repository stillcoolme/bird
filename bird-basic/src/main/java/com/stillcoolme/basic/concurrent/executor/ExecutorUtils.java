package com.stillcoolme.basic.concurrent.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: stillcoolmebnhhhhhh
 *
 *
 *
 * @date: 2020/4/29 20:32
 * Function:
 */
@Slf4j
public class ExecutorUtils {

    public static void threadPoolStatus(ThreadPoolExecutor executor, String name) {
        BlockingQueue<Runnable> queue = executor.getQueue();
        log.info("{} - {}:", name, Thread.currentThread().getName());
        log.info("核心线程数：{}，最大线程数：{}，活动线程数：{}，pool活跃度：{}",
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getActiveCount(),
                    divide(executor.getActiveCount(), executor.getMaximumPoolSize()));
        log.info("队列大小：{}，当前排队线程数：{}，队列剩余大小：{}，队列使用度：{}",
                    (queue.size() + queue.remainingCapacity()),
                    queue.size(),
                    queue.remainingCapacity(),
                    divide(queue.size(), queue.size() + queue.remainingCapacity()));

    }

    public static String divide(int num1, int num2) {
        return String.format("%1.2f%%",
                Double.parseDouble(num1 + "") / Double.parseDouble(num2 + ""));
    }

    public static void main(String[] args) {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        threadPoolStatus(executorService, "test");
        log.debug("hahdfhasdf");
    }

}
