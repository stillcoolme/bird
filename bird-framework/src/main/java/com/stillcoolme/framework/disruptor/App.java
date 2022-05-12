package com.stillcoolme.framework.disruptor;

/**
 * @author: stillcoolme
 * @date: 2020/3/26 22:32
 * Function:
 *  参考：https://juejin.im/post/5b744557518825612a228111
 *  功能上来看，Disruptor 是实现了 队列 的功能，而且是一个有界队列。那么它的应用场景自然就是“生产者-消费者”模型的应用场合了。
 *  并且 Disruptor 能做更多：
 *     同一个“事件”可以有多个消费者，消费者之间既可以并行处理，也可以相互依赖形成处理的先后次序(形成一个依赖图)；
 *     预分配用于存储事件内容的内存空间；
 *     针对极高的性能目标而实现的极度优化和无锁的设计；
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
