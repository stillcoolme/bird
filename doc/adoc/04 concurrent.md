## 为什么需要并发

并发其实是一种解耦合的策略，它帮助我们把做什么（目标）和什么时候做（时机）分开。这样做可以明显改进应用程序的吞吐量（获得更多的CPU调度时间）和结构（程序有多个部分在协同工作）。

Java的线程模型建立在**抢占式线程调度**的基础上：

- 所有线程可以很容易的共享同一进程中的对象，都可以修改这些对象。
- 为了保护数据，对象可以被锁住。



并发编程中，需要处理两个关键问题：线程之间如何通信？如何同步？

线程之间通信的方式可以概括为两种：共享内存，消息传递。

同步**包括两方面的含义： **独占性和可见性。

同步的话，就会 并发的挑战：CPU上下文切换，死锁

线程安全3个基本特性：原子性、可见性和有序性。



**并发编程原则技巧**

1. 如果有临界区，那就得同步；
2. 使用数据副本：数据副本是避免共享数据的好方法，CopyOnWriteArrayList。
3. 线程应尽可能独立：让线程存在于自己的世界中，不与其他线程共享数据。Servlet就是以单实例多线程的方式工作，和每个请求相关的数据都是通过Servlet子类的service方法（或者是doGet或doPost方法）的参数传入的。只要Servlet中的代码只使用局部变量，Servlet就不会导致同步问题。springMVC的控制器也是这么做的，从请求中获得的对象都是以方法的参数传入而不是作为类的成员，很明显Struts 2的做法就正好相反，因此Struts 2中作为控制器的Action类都是每个请求对应一个实例。
   1. 分段处理？ ConcurrentHashMap



![](https://img-blog.csdn.net/2018070117435683?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3BhbmdlMTk5MQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)



## 并发模型

在继续下面的探讨之前，我们还是重温一下几个概念：

| 概念     | 解释                                                         |
| :------- | :----------------------------------------------------------- |
| 临界资源 | 并发环境中有着固定数量的资源                                 |
| 互斥     | 对资源的访问是排他式的                                       |
| 饥饿     | 一个或一组线程长时间或永远无法取得进展                       |
| 死锁     | 两个或多个线程相互等待对方结束                               |
| 活锁     | 想要执行的线程总是发现其他的线程正在执行以至于长时间或永远无法执行 |



### 生产者-消费者

一个或多个生产者创建某些工作并将其置于缓冲区或队列中，一个或多个消费者会从队列中获得这些工作并完成。这里的缓冲区或队列是临界资源。

当缓冲区或队列放满的时候，生产这会被阻塞；而缓冲区或队列为空的时候，消费者会被阻塞。生产者和消费者的调度是通过二者相互交换信号完成的。

### 读者-写者

当存在一个主要为读者提供信息的共享资源，它偶尔会被写者更新，但是需要考虑系统的吞吐量，又要防止饥饿和陈旧资源得不到更新的问题。

在这种并发模型中，如何平衡读者和写者是最困难的，当然这个问题至今还是一个被热议的问题，恐怕必须根据具体的场景来提供合适的解决方案而没有那种放之四海而皆准的方法。

### 哲学家进餐

1965年，荷兰计算机科学家图灵奖得主Edsger Wybe Dijkstra提出并解决了一个他称之为哲学家进餐的同步问题。

这个问题可以简单地描述如下：五个哲学家围坐在一张圆桌周围，每个哲学家面前都有一盘通心粉。由于通心粉很滑，所以需要两把叉子才能夹住。相邻两个盘子之间放有一把叉子如下图所示。哲学家的生活中有两种交替活动时段：即吃饭和思考。当一个哲学家觉得饿了时，他就试图分两次去取其左边和右边的叉子，每次拿一把，但不分次序。如果成功地得到了两把叉子，就开始吃饭，吃完后放下叉子继续思考。
把上面问题中的哲学家换成线程，把叉子换成竞争的临界资源，上面的问题就是线程竞争资源的问题。如果没有经过精心的设计，系统就会出现死锁、活锁、吞吐量下降等问题。

![img](http://www.jqhtml.com/wp-content/uploads/2018/03/javabf323-4.jpg)

下面是用信号量原语来解决哲学家进餐问题的代码，使用了Java 5并发工具包中的Semaphore类（代码不够漂亮但是已经足以说明问题了）。

现实中的并发问题基本上都是这三种模型或者是这三种模型的变体。



## 并发三问题

并发编程挑战：线程如何通信，如何同步

并发的挑战：CPU上下文切换，没有同步（JVM对同步的代码数据一致做了保证），死锁，

线程安全3个基本特性：原子性、可见性和有序性。

### 1. 重排序

代码示例见ResortDemo，结合代码来说啊！！

1. 编译器、CPU重排序： 对于**没有数据依赖关系的 操作 或者 指令 的顺序重新排列**。可以直接运行当前有能力立即执行的后续指令，避开获取下一条指令所需数据时造成的等待。

2. 内存访问重排序：由于有缓存的存在，使得程序整体上会表现出乱序的行为。实质是下面的内存可见性！

> 假设不发生编译器重排和指令重排，线程 1 修改了 a 的值，但是修改以后，a 的值可能还没有写回到主存中，那么线程 2 得到 a == 0 就是很自然的事了。

### 2. 内存可见性

计算机系统中，为了尽可能地避免处理器访问主内存的时间开销，处理器大多会利用缓存(cache)以提高性能。每个核心都会将自己需要的数据读到独占缓存中，数据修改后也是写入到缓存中，**然后等待刷入到主存中**。所以会导致 缓存中的数据与 主内存的数据 并不是实时同步的。

所以在同一个时间点，各CPU所看到**同一内存地址的数据的值可能是不一致的**。从程序的视角来看，在同一个时间点，**各个线程所看到的共享变量的值可能是不一致的**。

![处理器Cache模型](https://raw.githubusercontent.com/janehzhang/mypic/master/2020/202001/system_cache_model.png)

虽然我们不再需要关心一级缓存和二级缓存的问题，但是，JMM 抽象了**主内存和本地内存**的概念。

所有的**共享变量存在于主内存中**，**每个线程有自己的本地内存**，线程之间无法直接访问，**读写**共享数据**都**要通过主存完成，所以可见性问题依然是存在的。这里说的本地内存并不是真的是一块给每个线程分配的内存，而是 JMM 的一个抽象，是对于寄存器、一级缓存、二级缓存等的抽象。

### 3. 原子性

在本文中，原子性不是重点，它将作为并发编程中需要考虑的一部分进行介绍。

说到原子性的时候，大家应该都能想到 long 和 double，它们的值需要占用 64 位的内存空间，Java 编程语言规范中提到，对于 64 位的值的写入，可以分为两个 32 位的操作进行写入。本来一个整体的赋值操作，被拆分为低 32 位赋值和高 32 位赋值两个操作，中间如果发生了其他线程对于这个值的读操作，必然就会读到一个奇怪的值。

这个时候我们要使用 volatile 关键字进行控制了，JMM 规定了对于 volatile long 和 volatile double，JVM 需要保证写入操作的原子性。



## Java 5以前的并发编程

### final 关键字

用 final 修饰的类不可以被继承，用 final 修饰的方法不可以被覆写，用 final 修饰的属性一旦初始化以后不可以被修改。当然，我们不关心这些段子，这节，我们来看看 final 带来的内存可见性影响。

之前在说双重检查的单例模式的时候，提过了一句，如果所有的属性都使用了 final 修饰，那么 volatile 也是可以不要的，这就是 final 带来的可见性影响。

在对象的构造方法中设置 final 属性，**同时在对象初始化完成前，不要将此对象的引用写入到其他线程可以访问到的地方**（不要让引用在构造函数中逸出）。如果这个条件满足，当其他线程看到这个对象的时候，那个线程始终可以看到正确初始化后的对象的 final 属性。

### synchronized

在Java 5以前，可以用synchronized关键字来实现锁的功能。一个线程在获取到 **对象的监视器锁 monitor** 后才能进入 synchronized 控制的代码块：

 	1. 首先，该线程对于共享变量的缓存就会失效，synchronized 代码块中对于共享变量的读取需要从主内存中重新获取，也就能获取到最新的值。
 	2. 退出代码块的时候，会将该线程**本地内存**写缓冲区中的数据刷到主内存中，所以随着该线程退出 synchronized 块，数据修改会立即对其他线程可见。
 	3. 不管是同步代码块还是同步方法，每次只有一个线程可以进入，如果其他线程试图进入，JVM会将它们挂起（放入到等锁池中）。这种结构在并发理论中称为**临界区（critical section）**。



这里我们可以对Java中用synchronized实现同步和锁的功能做一个总结：

- 只能锁定对象，不能锁定基本数据类型，数组中的单个对象不会被锁定
- 内部类的同步是独立于外部类的
- synchronized的可重入性： 从互斥锁的设计上来说，当线程a请求一个由线程b持有的对象锁时，该线程a会阻塞。当线程b请求自己持有的对象锁时，如果线程b是重入锁，请求就会成功，否则阻塞。  所以，线程使用synchronized方法时调用该对象另一个synchronized方法，是**永远可以拿到锁的**。 

看以下单例模式的错误写法：

```java
public class Singleton {

    private static Singleton instance = null;

    private int v;
    private Singleton() {
        this.v = 3;
    }

    public static Singleton getInstance() {
        if (instance == null) { // 1. 第一次检查
            synchronized (Singleton.class) { // 2
                if (instance == null) { // 3. 第二次检查
                    instance = new Singleton(); // 4
                }
            }
        }
        return instance;
    }
}
```

* 假设有两个线程 a 和 b 调用 getInstance() 方法，假设 a 先走，一路走到 4 这一步，执行  `instance = new Singleton()` 这句代码。
* instance = new Singleton() 这句代码首先会申请一段空间，然后将各个属性初始化为零值(0/null)，执行构造方法中的属性赋值[1]，将这个对象的引用赋值给 instance[2]。在这个过程中，**[1] 和 [2] 可能会发生重排序**。即使不发生重排序，线程 a 对于属性的赋值写入到了线程 a 的本地内存中，此时对于线程 b 不可见。
* 此时，线程 b 刚刚进来执行到 1，就有可能会看到 instance 不为 null，然后线程 b 也就不会等待监视器锁，而是直接返回还没构造完的 instance，它里面的属性值可能是初始化的零值(0/false/null)。
* 最后提一点，如果线程 a 从 synchronized 块出来了，那么 instance 一定是正确构造的**完整**实例，这是我们前面说过的 synchronized 的内存可见性保证。
* 解决方案是使用 volatile 关键字 。

### volatile

> 聊聊你对 volatile 的理解  ？？

它是一种简单的同步的处理机制，解决了 内存可见性 和 重排序 问题，但是没解决原子性的问题。相当于轻量级的 synchronized ，因为没有线程上下文切换。

1. 解决可见性：

volatile修饰的变量，就是遵循 happen-before 规则，对一个 volatile域的写 happen-before于任何后续对这个 volatile 域的读：

* 变量值修改之后强制从本地内存写回到主内存中；
* 此时其他线程中的变量会过期，不再允许使用；
* 变量值使用之前从主内存中再读取出来；

2. 解决重排序问题：

JVM底层 volatile 是采用 内存屏障 来实现的，观察加入volatile关键字生成的汇编代码发现，会多出一个lock前缀指令，lock前缀指令实际上相当于一个内存屏障（也成内存栅栏）：一种CPU指令，用于控制特定条件下的重排序和内存可见性问题。Java编译器也会根据内存屏障的规则 1. 禁止重排序，2. 强制将对本地缓存的修改操作立即写入主存，3. 也让其他CPU中对应的缓存行无效。

3. 没解决原子性的问题，volatile不适用的场景：复合操作，例如，inc++ 不是一个原子性操作，可以由读取、加、赋值3步组成。

要使 volatile 变量提供理想的线程安全，必须同时满足下面两个条件：

 *   对变量的写操作不依赖于当前值。
 *   该变量没有包含在具有其他变量的不变式中。

所以一般就是在设置 true 或 flase 的变量时使用吧。

需要保证原子性，要靠 synchronize和Lock，而且都可以保证可见性。synchronized和Lock能保证同一时刻只有一个线程获取锁然后执行同步代码，并且在释放锁之前会将对变量的修改刷新到主存当中。



### CAS

CAS作为下面的AQS等的基础，在这先介绍了。。。。









## Java 5的并发编程

不管今后的Java向着何种方向发展或者灭亡，Java 5绝对是Java发展史中一个极其重要的版本，Doug Lea在Java 5中提供了他里程碑式的杰作java.util.concurrent包：

- 更好的线程安全的容器
- 线程池和相关的工具类
- 非阻塞解决方案
- 显示的锁和信号量机制



### 原子类

Java 5中的java.util.concurrent包下面有一个atomic子包，其中有几个以Atomic打头的类，例如AtomicInteger和AtomicLong。它们利用了现代处理器的特性，可以用非阻塞的方式完成原子操作。

### AQS

> 聊聊你对AQS的理解

并发包下很多API ( ReentrantLock、ReentrantReadWriteLock、CountDownLatch ）都是基于 AQS 来实现的加锁和释放锁等功能的。

其实一句话总结AQS就是一个并发包的基础组件，用来实现各种锁，各种同步组件的。它包含了state变量、加锁线程、等待队列等并发中的核心组件。具体要结合使用的组件来分析。



### Lock

基于synchronized关键字的锁机制有以下问题：

- 锁只有一种类型，而且对所有同步操作都是一样的作用
- 锁只能在代码块或方法开始的地方获得，在结束的地方释放
- 线程要么得到锁，要么阻塞，没有其他的可能性

Java 5对锁机制进行了[重构](http://www.amazon.cn/gp/product/B003BY6PLK/ref=as_li_qf_sp_asin_il_tl?ie=UTF8&tag=importnew-23&linkCode=as2&camp=536&creative=3200&creativeASIN=B003BY6PLK)，提供了显示的锁接口 Lock ，这样可以在以下几个方面提升锁机制：

- 可以添加不同类型的锁，例如读取锁和写入锁
- 可以在一个方法中加锁，在另一个方法中解锁
- 可以使用tryLock方式尝试获得锁，如果得不到锁可以等待、回退或者干点别的事情，当然也可以在超时之后放弃操作

显示的锁都实现了java.util.concurrent.Lock接口，主要有两个实现类：

- ReentrantLock – 比synchronized稍微灵活一些的重入锁
- ReentrantReadWriteLock – 在读操作很多写操作很少时性能更好的一种重入锁

### lock    condition



### ReentrantLock



##### ReentrantLock底层

借着ReentrantLock的加锁和释放锁的过程，讲清楚了其底层依赖的AQS的核心原理。

现在如果有一个线程过来尝试用ReentrantLock的lock()方法进行加锁，会发生什么事情呢？

很简单，这个AQS对象内部有一个核心的变量叫做state，是int类型的，代表了加锁的状态。初始状态下，这个state的值是0。

另外，这个AQS内部还有一个关键变量 加锁线程，用来记录当前加锁的是哪个线程，初始化状态下，这个变量是null。



### CountDownLatch

CountDownLatch是一种简单的同步机制。它能让一个线程等待其他线程完成它们的工作再执行，避免对临界资源并发访问所引发的各种问题。例如主线程要等待一个子线程完成环境相关配置的加载工作，主线程才继续执行。



### CyclicBarrier

`CyclicBarrier`也是一种多线程并发控制的实用工具。`CyclicBarrier`可以实现让一组线程在全部到达`Barrier`时(执行`await()`)，再一起同时执行，并且所有线程释放后，还能复用它，即为Cyclic。

CountDownLatch 和 CyclicBarrier 两者各有不同侧重点的：

* CountDownLatch强调一个线程等多个线程完成某件事情。CyclicBarrier是多个线程互等，到都完成，再携手同时执行。
* 调用CountDownLatch的countDown方法后，当前线程并不会阻塞，会继续往下执行；而调用CyclicBarrier的await方法，会阻塞当前线程，直到CyclicBarrier指定的线程全部都到达了指定点，才能继续往下执行；
* CountDownLatch方法比较少，操作比较简单；而CyclicBarrier提供的方法更多，比如能够通过getNumberWaiting()，isBroken()这些方法获取当前多个线程的状态，并且CyclicBarrier的构造方法可以传入barrierAction，指定当所有线程都到达时执行的业务功能；
* CountDownLatch是不能复用的，而CyclicBarrier是可以复用的。



`CyclicBarrier`其实还是利用lock和condition，无非是多个线程去争抢CyclicBarrier的instance的lock罢了，最终barrierAction执行时，是在抢到CyclicBarrierinstance的那个线程上执行的。



### Semaphore





### ConcurrentHashMap，CopyOnWriteArrayList，Queue等并发集合

CopyOnWriteArrayList是ArrayList在并发环境下的替代品。CopyOnWriteArrayList通过增加写时复制语义来避免并发访问引起的问题，也就是说任何修改操作都会在底层创建一个列表的副本，也就意味着之前已有的迭代器不会碰到意料之外的修改。这种方式对于不要严格读写同步的场景非常有用，因为它提供了更好的性能。记住，要尽量减少锁的使用，因为那势必带来性能的下降（对数据库中数据的并发访问不也是如此吗？如果可以的话就应该放弃悲观锁而使用乐观锁），CopyOnWriteArrayList很明显也是通过牺牲空间获得了时间（在计算机的世界里，时间和空间通常是不可调和的矛盾，可以牺牲空间来提升效率获得时间，当然也可以通过牺牲时间来减少对空间的使用）。

### Queue



队列是一个无处不在的美妙概念，它提供了一种简单又可靠的方式将资源分发给处理单元（也可以说是将工作单元分配给待处理的资源，这取决于你看待问题的方式）。实现中的并发编程模型很多都依赖队列来实现，因为它可以在线程之间传递工作单元。
Java 5中的BlockingQueue就是一个在并发环境下非常好用的工具，在调用put方法向队列中插入元素时，如果队列已满，它会让插入元素的线程等待队列腾出空间；在调用take方法从队列中取元素时，如果队列为空，取出元素的线程就会阻塞。
![img](http://www.jqhtml.com/wp-content/uploads/2018/03/javabf323-3.png)

可以用BlockingQueue来实现生产者-消费者并发模型（下一节中有介绍），当然在Java 5以前也可以通过wait和notify来实现线程调度，比较一下两种代码就知道基于已有的并发工具类来重构并发代码到底好在哪里了。

- 基于wait和notify的实现

- 基于BlockingQueue的实现



使用BlockingQueue后代码优雅了很多。





## 测试并发代码

对并发代码的测试也是非常棘手的事情，棘手到无需说明大家也很清楚的程度，所以这里我们只是探讨一下如何解决这个棘手的问题。我们建议大家编写一些能够发现问题的测试并经常性的在不同的配置和不同的负载下运行这些测试。不要忽略掉任何一次失败的测试，线程代码中的缺陷可能在上万次测试中仅仅出现一次。具体来说有这么几个注意事项：

- 不要将系统的失效归结于偶发事件，就像拉不出屎的时候不能怪地球没有引力。
- 先让非并发代码工作起来，不要试图同时找到并发和非并发代码中的缺陷。
- 编写可以在不同配置环境下运行的线程代码。
- 编写容易调整的线程代码，这样可以调整线程使性能达到最优。
- 让线程的数量多于CPU或CPU核心的数量，这样CPU调度切换过程中潜在的问题才会暴露出来。
- 让并发代码在不同的平台上运行。
- 通过自动化或者硬编码的方式向并发代码中加入一些辅助测试的代码。

## Java 7的并发编程

Java 7中引入了TransferQueue，它比BlockingQueue多了一个叫transfer的方法，如果接收线程处于等待状态，该操作可以马上将任务交给它，否则就会阻塞直至取走该任务的线程出现。可以用TransferQueue代替BlockingQueue，因为它可以获得更好的性能。
刚才忘记了一件事情，Java 5中还引入了Callable接口、Future接口和FutureTask接口，通过他们也可以构建并发应用程序，代码如下所示。

Callable接口也是一个单方法接口，显然这是一个回调方法，类似于函数式编程中的回调函数，在Java 8 以前，Java中还不能使用Lambda表达式来简化这种函数式编程。和Runnable接口不同的是Callable接口的回调方法call方法会返回一个对象，这个对象可以用将来时的方式在线程执行结束的时候获得信息。上面代码中的call方法就是将计算出的10000个0到1之间的随机小数的平均值返回，我们通过一个Future接口的对象得到了这个返回值。目前最新的Java版本中，Callable接口和Runnable接口都被打上了@FunctionalInterface的注解，也就是说它可以用函数式编程的方式（Lambda表达式）创建接口对象。
下面是Future接口的主要方法：

- get()：获取结果。如果结果还没有准备好，get方法会阻塞直到取得结果；当然也可以通过参数设置阻塞超时时间。
- cancel()：在运算结束前取消。
- isDone()：可以用来判断运算是否结束。

Java 7中还提供了分支/合并（fork/join）框架，它可以实现线程池中任务的自动调度，并且这种调度对用户来说是透明的。为了达到这种效果，必须按照用户指定的方式对任务进行分解，然后再将分解出的小型任务的执行结果合并成原来任务的执行结果。这显然是运用了分治法（divide-and-conquer）的思想。下面的代码使用了分支/合并框架来计算1到10000的和，当然对于如此简单的任务根本不需要分支/合并框架，因为分支和合并本身也会带来一定的开销，但是这里我们只是探索一下在代码中如何使用分支/合并框架，让我们的代码能够充分利用现代多核CPU的强大运算能力。



伴随着Java 7的到来，Java中默认的数组排序算法已经不再是经典的快速排序（双枢轴快速排序）了，新的排序算法叫TimSort，它是归并排序和插入排序的混合体，

TimSort可以通过分支合并框架充分利用现代处理器的多核特性，从而获得更好的性能（更短的排序时间）。



### Fork/Join 框架

从JDK1.7开始，Java提供Fork/Join框架用于并行执行任务，它的思想就是讲一个大任务分割成若干小任务，最终汇总每个小任务的结果得到这个大任务的结果。

这种思想和MapReduce很像（input --> split --> map --> reduce --> output）

主要有两步：
第一、任务切分；
第二、结果合并
它的模型大致是这样的：线程池中的每个线程都有自己的工作队列（这一点和ThreadPoolExecutor不同，ThreadPoolExecutor是所有线程公用一个工作队列，所有线程都从这个工作队列中取任务），当自己队列中的任务都完成以后，会从其它线程的工作队列中偷一个任务执行，这样可以充分利用资源。



## 参考文献

* [关于Java并发编程的总结和思考](https://blog.csdn.net/jackfrued/article/details/44499227 )
* [美团技术重排序与内存可见性问题]( https://tech.meituan.com/2014/09/23/java-memory-reordering.html )
*  [synchronized](https://blog.csdn.net/javazejian/article/details/72828483#synchronized代码块底层原理) 
* [coundownlatch说明aqs](https://www.cnblogs.com/fengzheng/p/9153720.html )
* [远洋号--各种并发模型](https://mp.weixin.qq.com/s/5ZFAC1IWOGqPB2mClU05uQ)

## 看书

* Java并发编程的艺术 写的很好



### 锁的使用

* 缩短锁，减少锁的范围

* 分离锁，ReadWriteLock（多线程并发读锁，单线程写锁），BlockingQueue（ArrayBlockingQueue全局一把锁，LinkedBlockingQueue：对头对尾两把锁）

* 分散锁，ConcurrendHashMap（分散成16把锁），LongAdder（代替AtomicLong，计数时分散多个cell，取值时将所有cell求和）

* 无锁

  * CAS，乐观锁，读无消耗，竞争时写有等待（Atomic系列，CooncurrendLinkedQueue，CooncurrentSkipListMap），[cas在jdk8](http://ifeve.com/enhanced-cas-in-jdk8/)
  * ThreadLocal，（ThreadLocalRandom）
  * 不可变对象，不修改对象属性，直接替换为新对象，CopyOnWriteArrayList

* 异步日志与无锁队列的实现（同步日志是阻塞之源）

  * Log4j2的3种无锁队列，JCToolsQueue（各种多生产者多消费者队列），DistruptorQueue（[剖析Disruptor:为什么会这么快？](http://ifeve.com/locks-are-bad/)，LinkedTransferList

  

ThreadLocal的代价：没有银弹，开放地址法的HashMap，Netty的 FastThreadLocal, index原子自增的数组，重载initialValue()来初始终化值  

volatile的代价：Happen Before ；volatile vs Atomic*， 可见性 vs 原子性；访问开销比同步块小，但也不可忽视；存到临时变量使用，需要确保可见性时再次赋值  

线程池：

* tomcat线程池
* 无队列锁的线程池：ForkJoinPool（每个线程独立任务队列），Netty的EventLoop（由一组容量为一的线程池组成）

异步：

* future style：伪异步（支持并行调用，get的时候阻塞调用线程）
* CallBack Style ：烧脑（响应式编程，Guava ListenableFuture）
* Quasar协程：支持同步风格编码（少量线程处理海量请求；遇上阻塞主动让出线程去做其他事情；阻塞返回，随便拉一条线程再续前缘）[quasar](https://colobu.com/2016/07/14/Java-Fiber-Quasar/)



### 常见面试题

#### java怎么实现线程间通信？

1. 共享变量。具体方案：同步（synchronized），锁（CyclicBarrierAPI，RencrentLock），volatile，AtomicInteger，队列

   

### 锁的类型

公平锁（Fair）：加锁前检查是否有排队等待的线程，优先排队等待的线程，先来先得

非公平锁（Nonfair）：加锁时不考虑排队等待问题，直接尝试获取锁，获取不到自动到队尾等待；性能比公平锁高5~10倍，因为公平锁需要在多核的情况下维护一个队列。Java中的ReentrantLock 默认的lock()方法采用的是非公平锁。

#### 悲观锁

总是假设最坏的情况，每次去拿数据的时候都认为别人会修改，所以每次在拿数据的时候都会上锁，这样别人想拿这个数据就会阻塞直到它拿到锁（**共享资源每次只给一个线程使用，其它线程阻塞，用完后再把资源转让给其它线程**）。传统的关系型数据库里边就用到了很多这种锁机制，比如行锁，表锁等，读锁，写锁等，都是在做操作之前先上锁。Java中`synchronized`和`ReentrantLock`等独占锁就是悲观锁思想的实现。

#### 乐观锁

总是假设最好的情况，每次去拿数据的时候都认为别人不会修改，所以不会上锁，但是在更新的时候会判断一下在此期间别人有没有去更新这个数据，可以使用版本号机制和CAS算法实现。**乐观锁适用于多读的应用类型，这样可以提高吞吐量**，像数据库提供的类似于**write_condition机制**，其实都是提供的乐观锁。在Java中`java.util.concurrent.atomic`包下面的原子变量类就是使用了乐观锁的一种实现方式**CAS**实现的。

[乐观锁悲观锁](https://juejin.im/post/5b4977ae5188251b146b2fc8 )



synchronized 锁的状态总共有四种，无锁状态、偏向锁、轻量级锁和重量级锁。随着锁的竞争，锁可以从偏向锁升级到轻量级锁，再升级的重量级锁，但是锁的升级是单向的，也就是说只能从低到高升级，不会出现锁的降级。

可重入锁

synchronized的可重入性

从互斥锁的设计上来说，当一个线程试图操作一个由其他线程持有的对象锁的临界资源时，将会处于阻塞状态，但当一个线程再次请求自己持有对象锁的临界资源时，这种情况属于重入锁，请求将会成功，在java中synchronized是基于原子性的内部锁机制，是可重入的，因此在一个线程调用synchronized方法的同时在其方法体内部调用该对象另一个synchronized方法，也就是说一个线程得到一个对象锁后再次请求该对象锁，是允许的，这就是synchronized的可重入性。
