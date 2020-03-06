package com.stillcoolme.designpattern;

/**
 * @author: stillcoolme
 * @date: 2019/7/20 9:16
 * @description:
 * 单例模式懒汉式：在第一次调用的时候初始化
 * 1. 适用于单线程环境（不推荐）
 * 2. 方法同步，适用于多线程环境，但效率不高（不推荐）
 * 3. 双重检查加锁（推荐），只是在实例未被创建时再加锁！！在加锁处理里面还需要判断一次实例是否已存在。
 * 4. 使用validate解决方法3的指令重排问题
 * 5. 静态内部类，老版《Effective Java》中推荐的方式。
 *   https://www.race604.com/java-double-checked-singleton/
 **/
public class SingletonLazy {

    private static SingletonLazy instance = null;

    private SingletonLazy() {
    }

    /**
     * 1. 适用于单线程环境（不推荐）
     * 如果两个线程同时运行到判断instance是否为null的if语句，并且instance的确没有被创建时，
     * 那么两个线程都会创建一个实例，此时类型Singleton1就不再满足单例模式的要求了。
     * @return
     */
    public static SingletonLazy getInstanceA() {
        if(instance == null){
            instance = new SingletonLazy();
        }
        return instance;
    }

    /**
     * 2. 方法同步，适用于多线程环境，但效率不高（不推荐）
     * 每次调用getInstanceB()方法时都被synchronized关键字锁住了，会引起线程阻塞，影响程序的性能。
     * @return
     */
    public static synchronized SingletonLazy getInstanceB() {
        if(instance == null){
            instance = new SingletonLazy();
        }
        return instance;
    }

    /**
     * 3. 双重检查加锁（推荐）
     * 只是在实例未被创建时再加锁！！在加锁处理里面还需要判断一次实例是否已存在。
     * @return
     */
    public static SingletonLazy getInstanceC() {
        if(instance == null) {
            synchronized (SingletonLazy.class) {
                if(instance == null) {
                    instance =  new SingletonLazy();
                }
            }
        }
        return instance;
    }

    /**
     * 方法3中的 instance =  new SingletonLazy(); 这句，
     * 并非是一个原子操作，事实上在 JVM 中这句话大概做了下面 3 件事情：
     * 1）给 instance 分配内存；
     * 2）执行 new SingletonLazy(); 调用 SingletonLazy 的构造函数来初始化成员变量，形成实例；
     * 3）将 instance 对象指向分配的内存空间（执行完这步 instance 才是非 null 了）
     * 但是在 JVM 的即时编译器中存在指令重排序的优化。
     * 也就是说上面的第二步和第三步的顺序是不能保证的，最终的执行顺序可能是 1-2-3 也可能是 1-3-2。
     * 如果是后者，则在 3 执行完毕、2 未执行之前，被线程二抢占了，这时 instance 已经是非 null 了（但却没有初始化），
     * 所以线程二会直接返回 instance，然后使用，然后顺理成章地报错。
     *
     * 对此，我们只需要把singleton声明成 volatile 就可以了。
     * @return
     */
    /**
     * 使用 volatile 有两个功用：
     * 1）这个变量不会在多个线程中存在复本，直接从内存读取。
     * 2）这个关键字会禁止指令重排序优化。也就是说，在 volatile 变量的赋值操作后面会有一个内存屏障（生成的汇编代码上），读操作不会被重排序到内存屏障之前。
     * 但是，这个事情仅在Java 1.5版后有用，1.5版之前用这个变量也有问题，因为老版本的Java的内存模型是有缺陷的。
     */
    /**
     * 相比方法3，只要将singleton声明为volatile即可
     * // private volatile static SingletonLazy singleton = null;
     */
    private volatile static SingletonLazy singleton = null;
    public static SingletonLazy getInstanceD() {
        SingletonLazy inst = singleton;  // <<< 在这里创建临时变量
        if (inst == null)  {
            synchronized (SingletonLazy.class) {
                if (inst == null)  {
                    inst = new SingletonLazy();
                    singleton = inst;
                }
            }
        }
        return inst;    // <<< 在这里返回临时变量
    }

    /**
     * 5. 静态内部类
     * 使用JVM本身机制保证了线程安全问题，因为一个类被加载，当且仅当其某个静态成员（静态域、构造器、静态方法等）被调用时发生。
     * 所以加载外部类时，其内部类不会同时被加载。
     * 由于静态内部类的特性，只有在其被第一次引用的时候才会被加载，所以可以保证其线程安全性。
     * 而且通过反射，是不能从外部类获取内部类的属性的，所以安全不会被反射入侵。
     * 总结：
     * 优势：兼顾了懒汉模式的内存优化（使用时才初始化）以及饿汉模式的安全性（不会被反射入侵）。
     * 劣势：需要两个类去做到这一点，虽然不会创建静态内部类的对象，但是其 Class 对象还是会被创建，而且是属于永久代的对象。
     */
    public static SingletonLazy getInstanceE() {
        return SingletonLazyHolder.INSTANCE;
    }
    // SingletonLazyHolder 是私有的，除了 getInstance() 之外没有办法访问它，因此它只有在getInstance()被调用时才会真正创建；
    // 同时读取实例的时候不会进行同步，没有性能缺陷；也不依赖 JDK 版本。
    private static class SingletonLazyHolder{
        private final static SingletonLazy INSTANCE = new SingletonLazy();
    }


    public static void main(String[] args) {
        SingletonLazy singletonLazy1 = SingletonLazy.getInstanceE();
        SingletonLazy singletonLazy2 = SingletonLazy.getInstanceE();
        if(singletonLazy1.equals(singletonLazy2)) {
            System.out.println(true);
        }
    }
}
