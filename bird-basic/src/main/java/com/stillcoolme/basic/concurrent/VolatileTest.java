package com.stillcoolme.basic.concurrent;

/**
 * @author: stillcoolme
 * @date: 2019/10/11 10:32
 * @description:
 *  加volatile，是因为每个线程读取static变量时，
 *  会创建一个local cache，也就是copy了一份static变量，
 *  local cache是计算机级别的东西，不是JVM的，是CPU的cache机制导致的，
 *
 *  多线程环境下，一个线程对共享变量的操作对其他线程是不可见的。就算是static变量也会像上面说的有系统层面的local cache。。。。
 *  Java提供了volatile来保证可见性，当一个变量被volatile修饰后，表示线程本地内存无效，
 *  当一个线程修改共享变量后他会立即被更新到主存(Main Memory)中，其他线程读取共享变量时，会直接从主存中读取。
 *  当然，synchronize和Lock都可以保证可见性。
 *  synchronized和Lock能保证同一时刻只有一个线程获取锁然后执行同步代码，并且在释放锁之前会将对变量的修改刷新到主存当中。
 *
 *  JVM底层volatile是采用 内存屏障 来实现的。
 *  观察加入volatile关键字生成的汇编代码发现，会多出一个lock前缀指令，
 *  lock前缀指令实际上相当于一个内存屏障（也成内存栅栏），内存屏障会提供3个功能：
 *    I. 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内
 *       存屏障的后面；
 *    II. 它会强制将对缓存的修改操作立即写入主存；
 *    III. 如果是写操作，它会导致其他CPU中对应的缓存行无效。
 *
 *  volatile不适用的场景：复合操作
 *  例如，inc++ 不是一个原子性操作，可以由读取、加、赋值3步组成。
 *  要使 volatile 变量提供理想的线程安全，必须同时满足下面两个条件：
 *   对变量的写操作不依赖于当前值。
 *   该变量没有包含在具有其他变量的不变式中。
 *  所以一般就是在设置 true 或 flase 的变量时使用吧
 *
 */
public class VolatileTest {

    // volatile keyword here makes sure that
    // the changes made in one thread are
    // immediately reflect in other thread
    static volatile int sharedVar = 6;

}
