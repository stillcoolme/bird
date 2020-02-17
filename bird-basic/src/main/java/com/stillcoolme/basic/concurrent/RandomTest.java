package com.stillcoolme.basic.concurrent;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: stillcoolme
 * @date: 2019/9/16 10:58
 * @description:
 *  Random 类是线程安全的，但其内部使用 CAS 来保证线程安全性，在多线程并发的情况下的时候它的表现是存在优化空间的。
 *  在 JDK1.7 之后，Java 提供了更好的解决方案 ThreadLocalRandom。
 *  参考： https://www.cnkirito.moe/java-random/
 */
public class RandomTest {

    /**
     * Random 这个类是 JDK 提供的用来生成随机数的一个类，这个类并不是真正的随机，而是伪随机，
     * 伪随机的意思是生成的随机数其实是有一定规律的，而这个规律出现的周期随着伪随机算法的优劣而不同，一般来说周期比较长，但是可以预测。
     *
     * Random在构造方法当中，根据当前时间的种子生成了一个 AtomicLong 类型的 seed
     */
    public static void testRandom() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int int1 = random.nextInt();
            int int2 = random.nextInt(10);
            System.out.println("int1: " + int1);
            System.out.println("int2: " + int2);
        }

    }

    /**
     * Random使用的 CAS 相比加锁有一定的优势，但并不一定意味着高效。
     * 一个立刻被想到的解决方案是每次使用 Random 时都去 new 一个新的线程私有化的 Random 对象，或者使用 ThreadLocal 来维护线程私有化对象，
     * 但除此之外还存在更高效的方案，下面便来介绍 ThreadLocalRandom。
     *
     * 每个线程各自都维护了种子，这个时候并不需要 CAS，直接进行 put，在这里利用线程之间隔离，减少了并发冲突；
     * 相比较 ThreadLocal<Random>，ThreadLocalRandom 不仅仅减少了对象维护的成本，其内部实现也更轻量级。
     * 所以 ThreadLocalRandom 性能很高。
     */
    public static void testTHreadLocalRandom() {
        for (int i = 0; i < 10; i++) {
            // 注意，ThreadLocalRandom 切记不要调用 current 方法之后，作为共享变量使用；
            // 而是要像下面这样 ThreadLocalRandom.current().nextInt(10);
            // 这是因为 ThreadLocalRandom.current() 会使用初始化它的线程来填充随机种子，这会带来导致多个线程使用相同的 seed。
            int int1 =ThreadLocalRandom.current().nextInt();
            int int2 =ThreadLocalRandom.current().nextInt(10);
            System.out.println("int1: " + int1);
            System.out.println("int2: " + int2);
        }

    }

    public static void main(String[] args) {
//        testRandom();
        testTHreadLocalRandom();
    }

}
