package com.stillcoolme.basic.concurrent.executor;

import cn.hutool.core.date.StopWatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author: stillcoolme
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
        // 生产 导入标签图书馆外部打标标签 就是这样用的，在一个ServiceImpl类里面起一个 ThreadPoolExecutor，不过参数要设置一下
//        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        ThreadFactory NAME_THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat("label-import-%d").build();
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(
                400, 400, 1000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024000),
                NAME_THREAD_FACTORY,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 生产常用这种匿名方法提交任务，就不用在 Service类里写内部类了
        executorService.execute(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                // labelService.do()

                stopWatch.stop();
                log.info("ExecutorUtls 异步执行完成，duration: ", stopWatch.getTotalTimeMillis());
            } catch (Exception e) {
                log.info("ExecutorUtls 异步执行异常", e);
            }
        });

        threadPoolStatus(executorService, "test");

    }

}
