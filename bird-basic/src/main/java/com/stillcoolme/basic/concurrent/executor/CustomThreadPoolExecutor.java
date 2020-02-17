package com.stillcoolme.basic.concurrent.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: stillcoolme
 * @date: 2019/12/24 8:22
 * @description:
 *  自定义线程池，看 rejectHandler 形成阻塞与非阻塞
 */
public class CustomThreadPoolExecutor {

    private ThreadPoolExecutor pool = null;

    public void init() {
        pool = new ThreadPoolExecutor(
                1,
                3,
                30,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(5),
                // 这种线程工厂接口还能玩匿名 ： r -> new Thread(null, r, "customThread")
                new CustomThreadFactory("customThread"),
                new CustomRejectedExecutionHandler());
    }

    public void destory() {
        if(pool != null) {
            pool.shutdownNow();
        }
    }

    public ExecutorService getCustomThreadPoolExecutor() {
        return this.pool;
    }


    class CustomThreadFactory implements ThreadFactory {
        AtomicInteger count = new AtomicInteger(0);
        String name;
        public CustomThreadFactory(String name) {
            this.name = name;
        }
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, name + count.addAndGet(1));
            return thread;
        }
    }

    /**
     * 阻塞处理，
     * 当execute源码提交任务被拒绝时，进入拒绝机制，
     * 我们实现拒绝方法，把任务重新用阻塞提交方法put提交，实现阻塞提交任务功能，防止队列过大，OOM，提交被拒绝方法在下面。
     */
    class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                // 核心改造点，由blockingqueue的offer改成put阻塞方法, 构造阻塞线程池
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 非阻塞处理，
     * execute源码中提交到任务队列是用的offer方法，这个方法是非阻塞的，
     * 所以对于大数据量的任务来说，这种线程池，如果不设置队列长度会OOM，设置队列长度，会有任务得不到处理。
     */
    /*class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 记录异常
            // 报警处理等
            System.out.println("error.............");
        }
    }*/

    // 测试构造的线程池
    public static void main(String[] args) {

        CustomThreadPoolExecutor exec = new CustomThreadPoolExecutor();
        // 1.初始化
        exec.init();

        ExecutorService pool = exec.getCustomThreadPoolExecutor();

        for(int i=1; i<100; i++) {
            System.out.println("提交第" + i + "个任务!");
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(">>>task is running=====");
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        // 2.销毁----此处不能销毁,因为任务没有提交执行完,如果销毁线程池,任务也就无法执行了
        // exec.destory();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
