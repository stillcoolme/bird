package com.stillcoolme.basic.async;

import cn.hutool.core.thread.ThreadUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-12-29 15:04:00
 * @Description
 *   烧水泡茶程序，我们分了3个任务：
 *   任务1负责洗水壶、烧开水，任务2负责洗茶壶、洗茶杯和拿茶叶，任务3负责泡茶。
 *   其中任务3要等待任务1和任务2都完成后才能开始。
 *   这叫多线程编排？有个京东的工具 asyncTool
 */
public class CompletableFutureTest {

    /**
     * @param args
     * ## 创建CompletableFuture对象
     * 创建CompletableFuture对象主要靠下面代码中展示的这4个静态方法，我们先看前两个。
     *
     * // 使用默认公共ForkJoinPool线程池，共用的，线程数为CPU核数
     * static CompletableFuture<Void> runAsync(Runnable runnable)
     * static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
     * // 可以指定线程池
     * static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
     * static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
     *
     * 在烧水泡茶的例子中，我们已经使用了runAsync(Runnable runnable)和supplyAsync(Supplier<U> supplier)，
     * 它们之间的区别是：Runnable 接口的run()方法没有返回值，而Supplier接口的get()方法是有返回值的。
     *
     * 对于一个异步操作，你需要关注两个问题：一个是异步操作什么时候结束，另一个是如何获取异步操作的执行结果。
     * 因为CompletableFuture类实现了Future接口，所以这两个问题你都可以通过Future接口来解决。
     * 另外，CompletableFuture类还实现了CompletionStage接口，描述任务之间的串行、并行、AND、OR关系 和 异常处理。
     *
     *
     */
    public static void main(String[] args)  {

        //任务1：洗水壶->烧开水
        CompletableFuture<Void> f1 =
                CompletableFuture.runAsync(()->{
                    ThreadUtil.sleep(1000);

                    System.out.println("T1:洗水壶...");
                    ThreadUtil.sleep(1000);
                    System.out.println("T1:烧开水...");
                    ThreadUtil.sleep(10000);
                });

        //任务2：洗茶壶->洗茶杯->拿茶叶
        CompletableFuture<String> f2 =
                CompletableFuture.supplyAsync(()->{
                    ThreadUtil.sleep(1000);

                    System.out.println("T2:洗茶壶...");
                    ThreadUtil.sleep(1000);

                    System.out.println("T2:洗茶杯...");
                    ThreadUtil.sleep(1000);

                    System.out.println("T2:拿茶叶...");
                    ThreadUtil.sleep(1000);
                    return "龙井";
                });

        //任务3：任务1和任务2完成后执行：泡茶
        CompletableFuture<String> f3 =
                f1.thenCombine(f2, (__, tf)->{
                    System.out.println("T3:拿到茶叶:" + tf);
                    System.out.println("T3:泡茶...");
                    return "上茶:" + tf;
                });

        System.out.println("进行到哪里了1");

        //等待任务3执行结果
        System.out.println(f3.join());

        System.out.println("进行到哪里了2");

    }

    /**
     * 测试 一旦 complete 设置成功，CompletableFuture 返回结果就不会被更改，即使后续 CompletableFuture 任务执行结束。
     */
    private static void test1() throws ExecutionException {
        try {
            // 执行异步任务
            CompletableFuture cf = CompletableFuture.supplyAsync(() -> {
                System.out.println("cf 任务执行开始");
                ThreadUtil.sleep(10000);
                System.out.println("cf 任务执行结束");
                return "楼下小黑哥";
            });

            //
            Executors.newSingleThreadScheduledExecutor().execute(() -> {
                ThreadUtil.sleep(5000);

                System.out.println("主动设置 cf 任务结果");
                // 设置任务结果，由于 cf 任务未执行结束，结果返回 true
                cf.complete("程序通事");

            });

            // 由于 cf 未执行结束，将会被阻塞。5 秒后，另外一个线程主动设置任务结果
            System.out.println("get:" + cf.get());

            // 等待 cf 任务执行结束
            Thread.sleep(5000);
            // 由于已经设置任务结果，cf 执行结束任务结果将会被抛弃
            System.out.println("get:" + cf.get());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
