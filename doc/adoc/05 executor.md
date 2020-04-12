[toc]

## 是什么
1. Executor 是 任务执行的接口；
2. ExecutorService 继承 Executor 接口 提供生命周期方法；
3. ThreadPoolExecutor 的顶层接口是 ExecutorService，使用线程池中的线程执行每个提交的任务。

## 为什么
线程池解决了什么问题：

1. 线程是稀缺资源，不能频繁的创建。将其放入一个池子中进行资源管理，可以给其他任务进行复用，使得性能提升明显； 
2. 统计信息：每个ThreadPoolExecutor保持一些基本的统计信息，例如完成的任务数量。为了在广泛的上下文中有用，此类提供了许多可调参数和可扩展性钩子。

## 构造线程池

### 通过ThreadPoolExecutor自定义创建

```
coreSize 最小线程数，核心线程数
maxSize 最大线程数
keepAliveTime 线程保活时间，超过coreSize数的线程在等待超过该时间还没接到task会被干掉，线程池可以动态的调整池中的线程数！！！
workQueue 任务队列
threadFactory 线程工厂，用于创建线程（executor接收的是Runnable，需要创建Thread）；
handler 当提交任务数超过maxSize+workQueue之和都满了之后的饱和策略，如何拒绝任务，任务会交给RejectedExecutionHandler来处理；
```

#### 相关逻辑
通过 execute(Runnable)方法被添加到线程池，任务就是一个 Runnable类型的对象。

当一个任务通过execute(Runnable)方法欲添加到线程池时：
1. 如果线程池中的数量小于corePoolSize，即使线程池中有线程处于空闲状态，也要创建新的线程来处理被添加的任务。
2. 如果此时线程池中的数量等于 corePoolSize，但是缓冲队列 workQueue未满，那么任务被放入缓冲队列。
3. 如果线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量小于maximumPoolSize，建新的线程来处理被添加的任务。
4. 如果此时线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量等于maximumPoolSize，那么通过 handler所指定的策略来处理此任务。

所以处理任务的优先级为：

核心线程corePoolSize、任务队列workQueue、最大线程maximumPoolSize，如果三者都满了，使用handler处理被拒绝的任务。

ThreadPoolExecutor就是依靠BlockingQueue的阻塞机制来维持线程池，当池子里的线程无事可干的时候就通过workQueue.take()阻塞住！！！

handler有四个选择：
```
ThreadPoolExecutor.AbortPolicy()      //抛出java.util.concurrent.RejectedExecutionException异常
ThreadPoolExecutor.CallerRunsPolicy():     //重试添加当前的任务，他会自动重复调用execute()方法
ThreadPoolExecutor.DiscardOldestPolicy():     //抛弃旧的任务
ThreadPoolExecutor.DiscardPolicy():     //抛弃当前的任务
```

总结： 
1. 用ThreadPoolExecutor自定义线程池，看线程是的用途，如果任务量不大，可以用无界队列，如果任务量非常大，要用有界队列，防止OOM !
2. 如果任务量很大，还要求每个任务都处理成功，要对提交的任务进行阻塞提交，重写拒绝机制，改为阻塞提交。保证不抛弃一个任务。
3. 最大线程数一般设为2N+1最好，N是CPU核数 
4. 核心线程数，看应用，如果是任务，一天跑一次，设置为0，因为跑完就停掉了；如果是常用线程池，看任务量，是保留一个核心还是几个核心线程数 
5. 如果要获取任务执行结果，用CompletionService，但是注意，获取任务的结果的要重新开一个线程获取，如果在主线程获取，就要等任务都提交后才获取，就会阻塞大量任务结果，队列过大OOM，所以最好异步开个线程获取结果



### 创建自带的线程池

通过```Executors.xxx```创建下面几种系统自带的线程池。

1. newCachedThreadPool：因为`coresize = 0`，如果线程池长度超过任务需要，就回收空闲线程；因为`SynchronousQueue`是一种进一个等待消费完再进的队列，没有可用线程，则新建线程。
```
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```
**注意 SynchronousQueue**

2. newFixedThreadPool：创建一个固定大小的线程池，可控制线程最大并发数，超出的线程会在队列中等待
```
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
```
3. newScheduledThreadPool：创建一个定长线程池，适用定时及周期性任务执行
```
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }
```
4. newSingleThreadExecutor：创建一个线程容量为1的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序执行。里边使用的是LinkedBlockingQueue
```
public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```


### 线程池关闭

首先看源码中的一句注释：

> A pool that is no longer referenced in a program and has no remaining threads will be shutdown automatically.
如果程序中不再持有线程池的引用，并且线程池中没有线程时，线程池将会自动关闭。

可知，线程池自动关闭的两个条件：
1. 线程池的引用不可达；比如 executorService = null;
2. 线程池中没有线程；比如 coresize 为 0 的话，执行完所有任务，然后keepAliveTime以后，所有线程回收，线程池也关闭。


有些线程池一般是持续工作的全局场景，如数据库连接池。但对于一些确实需要关闭的，调用shutdown方法，会经历什么？

1. shutdown方法执行线程池关闭后，继续提交新任务会调用handler策略拒绝任务；
2. shutdown方法执行线程池关闭后，等待队列里的任务依然能继续执行；要使用 shutdownNow() 才能将不继续执行。
3. 正在执行的任务是否会立即中断？需要先了解线程中断机制interrupt()，shutdown()方法不会 interrupt 运行中线程；shutdownNow() 则调用 interrupt 方法，通知线程interrupt，线程如果没有对interrupt相关的处理就会执行完 。
   
> interrupt() 常识

`Thread.stop, Thread.suspend, Thread.resume`这些强制中断线程会破坏线程中原子操作，已经被废弃了。因此「一个线程不应该由其他线程来强制中断或停止，而是应该由线程自己自行停止」。 
`Thread.interrupt()`的作用是「将该线程的中断标志设置为 true，通知线程该中断了」，由被通知的线程自己处理中断还是继续运行，当然还能通过`ctrl c`来停线程。
1. 如果线程处于被阻塞状态（例如处于sleep, wait, join 等状态），那么线程将立即退出被阻塞状态，并抛出一个InterruptedException异常。
2. 如果线程处于正常活动状态，那么会将该线程的中断标志设置为 true。被设置中断标志的线程将继续正常运行，不受影响。通过代码判断：
```
if (Thread.interrupted()) {
      // 因为 interrupted() 会将 interupted 标志设回 false，需要再调一下 interrupt() 恢复一下现场
      Thread.currentThread().interrupt();
      throw new InterruptedException();
}
```


### 编程时要注意的点

就是可以进行调参的地方。改个参数或者加个参数就不同的行为的地方记一下。。。



### 参考

[简书](https://www.jianshu.com/p/c41e942bcd64)

[jie](https://segmentfault.com/a/1190000015808897)

[江南白衣](http://calvin1978.blogcn.com/articles/java-threadpool.html)

[Tomcat线程池，更符合大家想象的可扩展线程池](http://calvin1978.blogcn.com/articles/tomcat-threadpool.html)