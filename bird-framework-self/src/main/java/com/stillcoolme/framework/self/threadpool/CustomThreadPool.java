package com.stillcoolme.framework.self.threadpool;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: stillcoolme
 * @date: 2019/11/17 14:41
 * @description: 自定义线程池对象
 *
 */
public class CustomThreadPool {

    /**
     * 最小线程数，也叫核心线程数
     * miniSize 由于会在多线程场景下使用，所以也用 volatile 关键字来保证可见性
    */
    private volatile int minSize;
    private int maxSize;
    private long keepAliveTime;
    private TimeUnit unit;
    private BlockingQueue workQueue;
    private Notify notify;

    private Set<Worker> workers;

    // 居然有个锁
    private final ReentrantLock lock = new ReentrantLock();
    // 提交到线程池中的任务总数
    private AtomicInteger totalTask = new AtomicInteger();
    // 是否关闭线程池标志
    private AtomicBoolean isShutDown = new AtomicBoolean(false);
    // 线程池任务全部执行完毕后的通知组件
    private Object shutDownNotify = new Object();

    /**
     *
     * @param minSize 最小线程数，等效于 ThreadPool 中的核心线程数
     * @param maxSize 最大线程数
     * @param keepAliveTime 线程保活时间
     * @param workQueue 阻塞队列
     * @param notify 通知接口
     */
    public CustomThreadPool(int minSize, int maxSize, long keepAliveTime, TimeUnit timeUnit, BlockingQueue workQueue, Notify notify) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = timeUnit;
        this.workQueue = workQueue;
        this.notify = notify;

        workers = new ConcurrentHashSet<>();

    }

    /**
     * 往线程池中丢一个任务的时候其实要做的事情还蛮多的，最重要的事情莫过于创建线程存放到线程池中了。
     * @param runnable 需要执行的任务
     */
    public void execute(Runnable runnable) {
        if(runnable == null) {
            throw new NullPointerException("runnable null");
        }
        if(isShutDown.get()) {
            System.out.println("线程池已经关闭，不能再提交任务！");
            return;
        }
        int workerSize = workers.size();
        // 核心线程数小于minSize，则创建线程执行任务
        if(workers.size() < minSize) {
            addWorker(runnable);
        } else {
            // 优先写入队列，offer方法是队列满了就返回false
            boolean offer = workQueue.offer(runnable);
            // 无法继续写入队列
            if(!offer) {
                // 是否大于最大线程数，如果是则阻塞写入队列（官方为执行拒绝策略），否则创建新的线程执行
                if (workerSize > maxSize) {
                    System.out.println("超过最大线程数，阻塞");
                    try {
                        workQueue.put(runnable);
                    } catch (InterruptedException e) {

                    }
                } else {
                    addWorker(runnable);
                    return;
                }
            }

        }
    }

    /**
     * 创建新线程执行任务的时候会创建 Worker 对象，利用它的 startTask() 方法来执行任务
     * @param runnable
     */
    private void addWorker(Runnable runnable) {
        Worker worker = new Worker(runnable, true);
        worker.startTask();
        workers.add(worker);
    }

    public int getWorkerCount() {
        return workers.size();
    }

    /**
     * 从队列里获取task，
     */

    private Runnable getTask() {
        // shutdown() 将线程池的状态置为关闭状态，这样只要worker线程尝试从队列里获取任务时就会直接返回空，导致 worker 线程被回收。
        if(isShutDown.get() && totalTask.get() == 0) {
            return null;
        }
        // 加锁的原因是这里肯定会出现并发情况，
        // 不加锁会导致 workers.size()>miniSize 条件多次执行，从而导致线程被全部回收完毕
        lock.lock();
        try{
            Runnable task = null;
            if(workers.size() > minSize) {
                // 线程数 大于 核心线程数需要使用保活时间获取任务，返回队列头部的元素，如果队列为空，则返回null
                task = (Runnable) workQueue.poll(keepAliveTime, unit);
            } else {
                // 移除并返回队列头部的元素，如果队列为空，则阻塞
                task = (Runnable) workQueue.take();
            }
            if(task != null) {
                return task;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        // 队列中没有任务
        return null;
    }


    /**
     * 立即关闭线程池，会造成任务丢失
     */
    public void shutDownNow() {
        isShutDown.set(true);
        tryClose(false);
    }

    /**
     * 任务执行完毕后关闭线程池
     */
    public void shutDown() {
        isShutDown.set(true);
        tryClose(false);
    }

    /**
     * 关闭线程池
     * @param isTry true 尝试关闭      --> 会等待所有任务执行完毕
     *              false 立即关闭线程池--> 任务有丢失的可能
     */
    private void tryClose(boolean isTry) {
        if(!isTry) {
            closeAllTask();
        } else {
            if(isShutDown.get() && totalTask.get() == 0) {
                closeAllTask();
            }
        }
    }

    private void closeAllTask() {
        for(Worker worker: workers) {
            worker.close();
        }
    }


    /**
     * @author: stillcoolme
     * @date: 2019/11/17 17:22
     * @description:
     *  本身也是一个线程，将接收到需要执行的任务存放到成员变量 task 处，
     *  最为关键的则是执行任务 startTask() 这一方法，
     *  其实就是运行了 worker 线程自己
     */
    public class Worker extends Thread{
        private Runnable task;
        private Thread thread;

        // true -> 创建新的线程执行任务
        // false -> 从队列中获取线程执行
        private boolean isNewTask;

        public Worker(Runnable task, boolean isNewTask) {
            this.task = task;
            this.isNewTask = isNewTask;
            thread = this;
        }

        /**
         * 真正执行任务的方法
         */
        public void startTask() {
            thread.start();
        }

        @Override
        public void run(){
            Runnable task = null;
            if(isNewTask) {
                task = this.task;
            }
            try{
                while (task != null || (task = getTask()) != null) {
                    try{
                        task.run();
                    } finally {
                        // 任务执行完毕，就要将
                        task = null;
                        int number = totalTask.decrementAndGet();
                        if(number == 0) {
                            synchronized (shutDownNotify) {
                                shutDownNotify.notify();
                            }
                        }
                    }
                }
            } finally {
                // worker 线程获取不到任务后退出，需要将自己从线程池中释放 ？？ 没有就释放？？
                workers.remove(this);
                // 没有正在执行的任务才关闭
                tryClose(true);
            }


        }

        public void close() {
            // 执行中断
            thread.interrupt();
        }
    }

}
