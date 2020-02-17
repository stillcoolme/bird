package com.stillcoolme.basic.concurrent.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: stillcoolme
 * @date: 2019/11/27 11:10
 * @description:
 *  Java的 Future 在执行 future.get() 会阻塞，等待拿到结果再继续执行剩下的逻辑；
 *  这种适用于 剩下的业务逻辑需要拿到 多线程执行结果的；
 *  不好的地方是 如果前面的一个线程的执行结果出错或者消耗过多时间，导致get不到，然后错误，就一直阻塞
 */
public class JavaFutureTest {

    //定义一个线程池，用于处理所有任务
    final static ExecutorService service = new ThreadPoolExecutor(
            10,
            20,
            3,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10));

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Long t1 = System.currentTimeMillis();
        List<Future> list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Future<String> future = service.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Thread.sleep(5000);
                    return "Hello World";
                }
            });
            list.add(future);
        }

        for (int i = 0; i < list.size(); i++) {
            String string = (String) list.get(i).get();
            System.out.println(string + i);
        }

        // 执行时间
        System.out.println("time: " + (System.currentTimeMillis() - t1));

        System.out.println("后面的业务逻辑...");

        service.shutdown();
    }

}
