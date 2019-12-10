package com.stillcoolme.core.concurrent;

/**
 * @author: stillcoolme
 * @date: 2019/9/16 11:18
 * @description:
 *  http://www.jasongj.com/java/threadlocal/
 * ThreadLocal是线程 Thread 中 属性threadLocals 的管理者；
 * 它会为 每个线程 分别存储一份唯一的数据， 你所创建出来变量对每个线程实例来说都是唯一的。
 * 类似于 每个人都一张银行卡；每个人获取银行卡余额都必须通过该银行的管理系统；每个人都只能获取自己卡持有的余额信息，他人的不可访问。
 *
 * ThreadLocal是如何做到为每一个线程维护变量的副本的呢？
 * 其实实现的思路很简单：在ThreadLocal类中有一个Map，用于存储每一个线程的变量副本，Map中元素的键为线程对象，而值对应线程的变量副本。
 *
 * 以上我们复现了ThreadLocal不正当使用，引起的内存泄漏。demo在这里。
 *     所以我们总结了使用ThreadLocal时会发生内存泄漏的前提条件：
 *  * ThreadLocal引用被设置为null，且后面没有set，get,remove操作。
 *  * 线程一直运行，不停止。（线程池）
 *  * 触发了垃圾回收。（Minor GC或Full GC）
 *
 * 我们看到ThreadLocal出现内存泄漏条件还是很苛刻的，只要破坏其中一个条件就可以避免内存泄漏，
 * 为了更好的避免这种情况的发生我们使用ThreadLocal时遵守以下两个小原则:
 *  * ThreadLocal申明为private static final。
 *      Private与final 尽可能不让他人修改变更引用，
 *      Static 表示为类属性，只有在程序结束才会被回收。
 *  * ThreadLocal使用后务必调用remove方法。最简单有效的方法是使用后将其移除。
 */
public class ThreadLocalTest {

    public static void main(String[] args) {
        Account account = new Account();
        Customer customer = new Customer(account);
        Thread thread1 = new Thread(customer);
        thread1.start();
        Thread thread2 = new Thread(customer);
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 在Main线程中的account输出的则是初始值
        System.out.println("main: " + account.getAccount());
    }
}

class Account{
    public Account() {
    }

    static ThreadLocal<Integer> account = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };

    public Integer getAccount() {
        return account.get();
    }

    public void addAccount(Integer num) {
        account.set(account.get() + num);
    }

    public void setAccount(Integer num) {
        account.set(num);
    }
}

class Customer implements Runnable {
    private Account account;
    public Customer(Account account) {
        this.account = account;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            account.addAccount(10);
            System.out.println(Thread.currentThread().getName() + ": " + account.getAccount());
        }
    }
}