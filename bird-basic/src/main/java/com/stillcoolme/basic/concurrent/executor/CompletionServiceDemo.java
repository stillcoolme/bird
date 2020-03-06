package com.stillcoolme.basic.concurrent.executor;

import java.util.concurrent.*;

/**
 * @author: stillcoolme
 * @date: 2020/3/2 10:27
 * Function:
 * 将生产新的异步任务与使用已完成任务的结果分离开来的服务。生产者 submit 执行的任务。使用者 take 已完成的任务，
 * 并按照完成这些任务的顺序处理它们的结果。例如，CompletionService 可以用来管理异步 IO ，执行读操作的任务作为程序或系统的一部分提交，
 * 然后，当完成读操作时，会在程序的不同部分执行其他操作，执行操作的顺序可能与所请求的顺序不同。
 */
public class CompletionServiceDemo implements Callable<String> {
    private int id;

    public CompletionServiceDemo(int i) {
        this.id = i;
    }

    @Override
    public String call() throws Exception {
        Integer time = (int) (Math.random() * 1000);
        try {
            System.out.println(this.id + " start");
            Thread.sleep(time);
            System.out.println(this.id + " end");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.id + ":" + time;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService service = Executors.newCachedThreadPool();
        CompletionService<String> completion = new ExecutorCompletionService<String>(service);
        for (int i = 0; i < 10; i++) {
            completion.submit(new CompletionServiceDemo(i));
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(completion.take().get());
        }
        service.shutdown();
    }
}
