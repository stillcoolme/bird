package com.stillcoolme.core.concurrent.atomic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: stillcoolme
 * @date: 2019/8/29 8:16
 * @description:
 *  多线程并发处理情况下的 数字增减 记得用 Atomic 啊！！！
 *
 *  volatile原理：对于值的操作，会立即更新到主存中，当其他线程获取最新值时会从主存中获取
 *      可见性：在多线程并发的条件下，对于变量的修改，其他线程中能获取到修改后的值
 *
 *  atomic原理：对于值的操作，是基于底层硬件处理器提供的原子指令，保证并发时线程的安全；
 *      当某个线程在执行atomic的方法时，不会被其他线程打断，而别的线程就像自旋锁一样，一直等到该方法执行完成，才由JVM从等待队列中选择一个线程执行
 *      原子性：在多线程并发的条件下，对于变量的操作是线程安全的，不会受到其他线程的干扰
 *      CAS：借助CAS实现了区别于synchronized同步锁的一种乐观锁。
 *           乐观锁就是每次去取数据的时候都乐观的认为数据不会被修改，因此这个过程不会上锁，但是在更新的时候会判断一下在此期间的数据有没有更新
 *
 *  Atomic成员分为四大块
 *      原子方式更新基本类型
 *      原子方式更新数组
 *      原子方式更新引用
 *      原子方式更新字段
 **/
public class AtomicTest {

    static void testUpdateBasicType() {
        AtomicInteger ai = new AtomicInteger(1);
        //先获取，再自增
        System.out.println(ai.getAndIncrement());
        //先自增，再获取
        System.out.println(ai.incrementAndGet());
        //增加一个指定值，先add，再get
        System.out.println(ai.addAndGet(5));
        //增加一个指定值,先get，再set
        System.out.println(ai.getAndSet(5));
    }


    static void testUpdateArray() {
        int[] valueArr = new int[]{1, 2};
        AtomicIntegerArray aiArray = new AtomicIntegerArray(valueArr);
        aiArray.getAndSet(0, 3);
        //不会修改原始数组value
        System.out.println(aiArray.get(0));
        System.out.println(valueArr[0]);
    }

    static void testUpdateRef() {
        AtomicReference<User> atomicUserRef = new AtomicReference<User>();
        User user = new User("bobo", 23);
        User updateUser = new User("Rose", 20);

        atomicUserRef.set(user);
        atomicUserRef.compareAndSet(user, updateUser);

        System.out.println(atomicUserRef.get().getName());
        System.out.println(atomicUserRef.get().getOld());
    }

    static class User {
        private String name;
        private int old;

        public User(String name, int old) {
            this.name = name;
            this.old = old;
        }

        public String getName() {
            return name;
        }

        public int getOld() {
            return old;
        }
    }

    public static void main(String[] args) {
        testUpdateArray();

        testUpdateRef();
    }
}
