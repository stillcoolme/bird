## 先熟悉 TCP/IP 协议
1. 何为TCP协议；
2. 何为Socket通信，Socker通信怎么建立连接；

## BIO
BIO（blocking I/O）（同步阻塞型）还是JDK1.4版本之前常用的编程方式。

BIO服务端从启动到收到客户端数据将会有两次阻塞的过程，可查看项目中BIO实现一个单线程服务器、多线程服务器代码:
1. 服务器端在启动后，首先需要等待客户端的连接请求（第一次阻塞），如果没有客户端连接，服务端将一直阻塞等待；
2. 然后当第一个客户端连接后，服务器会等待客户端发送数据（第二次阻塞），如果客户端没有发送数据，那么服务端将会一直阻塞等待这第一个客户端发送数据，后面连接上的客户端都要干等。

所以，服务实现模式为一个连接一个线程：即客户端有连接请求时，服务端就需要启动一个线程进行处理，如果这个连接不做任务事情就会一直阻塞着，造成不必要的线程开销，

> 缺点：
1. 虽然可以用线程池优化技术，但还是避免不了一个客户端连接请求创建一个线程资源的局面，每个线程都需要一部分内存，内存会被迅速消耗，线程切换的开销也非常大。
2. 连接建立后，如果当前线程暂时没有数据可读，则线程就阻塞在 Read 操作上，造成线程资源浪费。

> 应用：

适用于连接数目比较小且固定的架构，对服务器资源要求比较高，并发局限于应用中。


## NIO

NIO 有三大核心部分Channel(通道)，Buffer(缓冲区), Selector(选择器) 。
* NIO 基于Channel和Buffer进行操作，Channel是双向的，数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中（BIO 中的 stream 是单向的，FileInputStream 对象只能进行读取数据的操作）。
* 面向缓冲区(Buffer)或者块编程：数据读取到一个稍后处理的缓冲区，且可在缓冲区中前后移动，这可以提供非阻塞式的 高伸缩性网络，也就是说缓冲区实现了NIO的非阻塞模式：处理的线程可以去做其他事直到Buffer填充完再来处理。
* NIO的非阻塞模式：使一个线程从某通道发送请求或者读取数据，但是它仅能得到目前可用的数据，如果目前没有数据可用时，就什么都不会获取，不会线程阻塞，直至数据变的可以读取之前，该线程可以继续做其他的事情。
* Selector(选择器，Java的多路复用器)用于实现非阻塞IO（解决了BIO的问题！），用于一个线程监听多个通道的事件（比如：连接请求，数据到达等），看看上面是否有通道是准备好的，当通道准备好可读或可写，然后才去开始真正的读写。

三大核心**接口**使用可参考：[javadoop](https://www.javadoop.com/post/java-nio)

> 应用 

NIO方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，弹幕系统，服务器间通讯等。


### NIO底层
NIO 中 Selector 是对底层操作系统实现的一个抽象，管理通道状态其实都是底层系统实现的，主动地去感知有数据的socket。
* select（window）：上世纪 80 年代就实现了，它支持注册 FD_SETSIZE(1024) 个 socket，在那个年代肯定是够用的，不过现在嘛，肯定是不行了。(我们也不必觉得 Windows 很落后，在 Windows 中 IOCP 提供的异步 IO 是比较强大的。)
* poll（linux）：1997 年，出现了 poll 作为 select 的替代者，最大的区别就是，poll 不再限制 socket 数量。
* select 和 poll 都有一个共同的问题，那就是它们都只会告诉你有几个通道准备好了，但是不会告诉你具体是哪几个通道，还是需要自己进行一次扫描，一旦通道的数量是几十万个以上的时候，扫描一次的时间都很可观了，时间复杂度 O(n)。
* epoll：2002 年随 Linux 内核 2.5.44 发布，epoll 能直接返回具体的准备好的通道，时间复杂度 O(1)。
* 毕竟 JVM 就是这么一个屏蔽底层实现的平台，我们面向 Selector 编程就可以了！！！


* 假如有五个请求，系统select会将五个请求从用户态空间全量复制一份到内核态空间，在内核态空间来判断每个请求是否准备好数据，避免了4次频繁的上下文切换。

> select
* select是一个阻塞函数，如果select没有查询到到有数据的请求，那么将会一直阻塞。

* 如果有一个或者多个请求已经准备好数据了，那么select将会先将有数据的文件描述符置位，然后select返回。返回后通过遍历查看哪个请求有数据。

select的缺点：
* 底层存储依赖bitmap，处理的请求是有上限的，为1024。
* 文件描述符是会置位的，如果当被置位的文件描述符需要重新使用时，需要重新赋空值。
* fd（文件描述符）从用户态拷贝到内核态仍然有一笔开销。
* select返回后还要再次遍历，来获知是哪一个请求有数据。

> poll函数

poll的工作原理和select很像，先来看一段poll内部使用的一个结构体。
struct pollfd{
    int fd;
    short events;
    short revents;
}

* poll同样会将所有的请求拷贝到内核态，和select一样，poll同样是一个阻塞函数，
* 当一个或多个请求有数据的时候，也同样会进行置位，但是它置位的是结构体pollfd中的events或者revents置位，而不是对fd本身进行置位
所以在下一次使用的时候不需要再进行重新赋空值的操作。
* poll内部存储不依赖bitmap，而是使用pollfd数组的这样一个数据结构，数组的大小肯定是大于1024的。解决了select 1、2两点的缺点。

> epoll函数

epoll是最新的一种多路IO复用的函数。

* 它的fd是共享在用户态和内核态之间的，所以可以不必进行从用户态到内核态的一个拷贝，这样可以节约系统资源；
* 在select和poll中，如果某个请求的数据已经准备好，它们会将所有的请求都返回，供程序去遍历查看哪个请求存在数据，但是epoll只会返回存在数据的请求
* 这是因为epoll在发现某个请求存在数据时，首先会进行一个重排操作，将所有有数据的fd放到最前面的位置，然后返回（返回值为存在数据请求的个数N），那么我们的上层程序就可以不必将所有请求都轮询，而是直接遍历epoll返回的前N个请求，这些请求都是有数据的请求。


## NIO.2 异步 IO
随 JDK 1.7 发布，包括了引入异步 IO 接口和 Paths 等文件访问接口。

通常，我们会有一个线程池用于执行异步任务，提交任务的线程将任务提交到线程池就可以立马返回，不必等到任务真正完成。而通常是通过传递一个回调函数的方式，任务结束后去调用这个函数得到结果。

同样的原理，Java 中的异步 IO 也是一样的，都是由一个线程池来负责执行任务，然后使用回调或自己去查询结果。

异步 IO 主要是为了控制线程数量，减少过多的线程带来的内存消耗和 CPU 在线程调度上的开销。

**在 Unix/Linux 等系统中，JDK 使用了并发包中的线程池来管理任务**，具体可以查看 AsynchronousChannelGroup 的源码。

在 Windows 操作系统中，提供了一个叫做 [I/O Completion Ports](https://msdn.microsoft.com/en-us/library/windows/desktop/aa365198.aspx) 的方案，通常简称为 **IOCP**，操作系统负责管理线程池，其性能非常优异，所以**在 Windows 中 JDK 直接采用了 IOCP 的支持**，使用系统支持，把更多的操作信息暴露给操作系统，也使得操作系统能够对我们的 IO 进行一定程度的优化。

> 在 Linux 中其实也是有异步 IO 系统实现的，但是限制比较多，性能也一般，所以 JDK 采用了自建线程池的方式。

本文还是以实用为主，想要了解更多信息请自行查找其他资料，下面对 Java 异步 IO 进行实践性的介绍。

总共有三个类需要我们关注，分别是 **AsynchronousSocketChannel**，**AsynchronousServerSocketChannel** 和 **AsynchronousFileChannel**

Java 异步 IO 提供了两种使用方式，分别是**返回 Future 实例**和**使用回调函数**。

> 1、返回 Future 实例

返回 java.util.concurrent.Future 实例的方式我们应该很熟悉，JDK 线程池就是这么使用的。Future 接口的几个方法语义在这里也是通用的，这里先做简单介绍。

- future.isDone();

  判断操作是否已经完成，包括了**正常完成、异常抛出、取消**

- future.cancel(true);

  取消操作，方式是中断。参数 true 说的是，即使这个任务正在执行，也会进行中断。

- future.isCancelled();

  是否被取消，只有在任务正常结束之前被取消，这个方法才会返回 true

- future.get(); 

  这是我们的老朋友，获取执行结果，阻塞。

- future.get(10, TimeUnit.SECONDS);

  如果上面的 get() 方法的阻塞你不满意，那就设置个超时时间。

> 2、提供 CompletionHandler 回调函数

java.nio.channels.CompletionHandler 接口定义：

```java
public interface CompletionHandler<V,A> {

    void completed(V result, A attachment);

    void failed(Throwable exc, A attachment);
}
```

> 注意，参数上有个 attachment，虽然不常用，我们可以在各个支持的方法中传递这个参数值

```java
AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open().bind(null);

// accept 方法的第一个参数可以传递 attachment
listener.accept(attachment, new CompletionHandler<AsynchronousSocketChannel, Object>() {
    public void completed(
      AsynchronousSocketChannel client, Object attachment) {
          // 
      }
    public void failed(Throwable exc, Object attachment) {
          // 
      }
});
```

### AsynchronousFileChannel

网上关于 Non-Blocking IO 的介绍文章很多，但是 Asynchronous IO 的文章相对就少得多了，所以我这边会多介绍一些相关内容。

首先，我们就来关注异步的文件 IO，前面我们说了，文件 IO 在所有的操作系统中都不支持非阻塞模式，但是我们可以对文件 IO 采用异步的方式来提高性能。

下面，我会介绍 AsynchronousFileChannel 里面的一些重要的接口，都很简单，读者要是觉得无趣，直接滑到下一个标题就可以了。

实例化：

```java
AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("/Users/hongjie/test.txt"));
```

一旦实例化完成，我们就可以着手准备将数据读入到 Buffer 中：

```java
ByteBuffer buffer = ByteBuffer.allocate(1024);
Future<Integer> result = channel.read(buffer, 0);
```

> 异步文件通道的读操作和写操作都需要提供一个文件的开始位置，文件开始位置为 0

除了使用返回 Future 实例的方式，也可以采用回调函数进行操作，接口如下：

```java
public abstract <A> void read(ByteBuffer dst,
                              long position,
                              A attachment,
                              CompletionHandler<Integer,? super A> handler);
```

顺便也贴一下写操作的两个版本的接口：

```java
public abstract Future<Integer> write(ByteBuffer src, long position);

public abstract <A> void write(ByteBuffer src,
                               long position,
                               A attachment,
                               CompletionHandler<Integer,? super A> handler);
```

我们可以看到，AIO 的读写主要也还是与 Buffer 打交道，这个与 NIO 是一脉相承的。

另外，还提供了用于将内存中的数据刷入到磁盘的方法：

```java
public abstract void force(boolean metaData) throws IOException;
```

> 因为我们对文件的写操作，操作系统并不会直接针对文件操作，系统会缓存，然后周期性地刷入到磁盘。如果希望将数据及时写入到磁盘中，以免断电引发部分数据丢失，可以调用此方法。参数如果设置为 true，意味着同时也将文件属性信息更新到磁盘。

还有，还提供了对文件的锁定功能，我们可以锁定文件的部分数据，这样可以进行排他性的操作。

```java
public abstract Future<FileLock> lock(long position, long size, boolean shared);
```

> position 是要锁定内容的开始位置，size 指示了要锁定的区域大小，shared 指示需要的是共享锁还是排他锁

当然，也可以使用回调函数的版本：

```java
public abstract <A> void lock(long position,
                              long size,
                              boolean shared,
                              A attachment,
                              CompletionHandler<FileLock,? super A> handler);
```

文件锁定功能上还提供了 tryLock 方法，此方法会快速返回结果：

```java
public abstract FileLock tryLock(long position, long size, boolean shared)
    throws IOException;
```

> 这个方法很简单，就是尝试去获取锁，如果该区域已被其他线程或其他应用锁住，那么立刻返回 null，否则返回  FileLock 对象。

AsynchronousFileChannel 操作大体上也就以上介绍的这些接口，还是比较简单的，这里就少一些废话早点结束好了。


### Asynchronous Channel Groups

为了知识的完整性，有必要对 group 进行介绍，其实也就是介绍 AsynchronousChannelGroup 这个类。之前我们说过，异步 IO 一定存在一个线程池，这个线程池负责接收任务、处理 IO 事件、回调等。

这个线程池就在 group 内部，group 一旦关闭，那么相应的线程池就会关闭。

当我们调用 AsynchronousServerSocketChannel 或 AsynchronousSocketChannel 的 open() 方法的时候，相应的 channel 就属于默认的 group，这个 group 由 JVM 自动构造并管理。

如果我们想要配置这个默认的 group，可以在 JVM 启动参数中指定以下系统变量：

- java.nio.channels.DefaultThreadPool.threadFactory

  此系统变量用于设置 ThreadFactory，它应该是 java.util.concurrent.ThreadFactory 实现类的全限定类名。一旦我们指定了这个 ThreadFactory 以后，group 中的线程就会使用该类产生。

- java.nio.channels.DefaultThreadPool.initialSize

  此系统变量也很好理解，用于设置线程池的初始大小。

可能你会想要使用自己定义的 group，这样可以对其中的线程进行更多的控制，使用以下几个方法即可：

- AsynchronousChannelGroup.withCachedThreadPool(ExecutorService executor, int initialSize)
- AsynchronousChannelGroup.withFixedThreadPool(int nThreads, ThreadFactory threadFactory)
- AsynchronousChannelGroup.withThreadPool(ExecutorService executor)

至于 group 的使用就很简单了，代码一看就懂：

```java
AsynchronousChannelGroup group = AsynchronousChannelGroup
        .withFixedThreadPool(10, Executors.defaultThreadFactory());
AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
AsynchronousSocketChannel client = AsynchronousSocketChannel.open(group);
```

**AsynchronousFileChannels 不属于 group**。但是它们也是关联到一个线程池的，如果不指定，会使用系统默认的线程池，如果想要使用指定的线程池，可以在实例化的时候使用以下方法：

```java
public static AsynchronousFileChannel open(Path file,
                                           Set<? extends OpenOption> options,
                                           ExecutorService executor,
                                           FileAttribute<?>... attrs) {
    ...
}
```



## 同步、异步、阻塞、非阻塞

https://baijiahao.baidu.com/s?id=1623908582750588151&wfr=spider&for=pc

https://blog.csdn.net/guanghuichenshao/article/details/79375967

先来个例子理解一下概念，以银行取款为例：
* 同步 ： 自己亲自出马持银行卡到银行取钱（使用同步IO时，Java自己处理IO读写）。
* 异步 ： 委托一小弟拿银行卡到银行取钱，然后给你（使用异步IO时，Java将IO读写委托给OS处理，需要将数据缓冲区地址和大小传给OS(银行卡和密码)，OS需要支持异步IO操作API）。
* 阻塞 ： ATM排队取款，你只能等待（使用阻塞IO时，Java调用会一直阻塞到读写完成才返回）。
* 非阻塞 ： 柜台取款，取个号，然后坐在椅子上做其它事，等号广播会通知你办理，没到号你就不能去，你可以不断问大堂经理排到了没有，大堂经理如果说还没到你就不能去（使用非阻塞IO时，如果不能读写Java调用会马上返回，当IO事件分发器会通知可读写时再继续进行读写，不断循环直到读写完成）。




## 其他

### Windows OS环境下使用nc命令，实现TCP方式方式通信

1. 在`nc.exe`所在的目录打开`cmd`， 然后执行`nc.exe -l [IP] -p [端口号]`打开server端，例如`nc.exe -l -p 8989`；（不管写程序还是nc，监听端口时，一定要尽量明确指出IP地址，避免出现一起监听到同一端口而没响应的问题）
2. 另外在`nc.exe`所在的目录打开`cmd`，然后执行`nc [服务器端IP地址] [端口号]`打开client端，例如`nc.exe localhost 8989`
3. 在client端输入字符，可以在server端接到。

使用telnet来连nc server
1. window打开telnet：去卸载软件那里，左边启用或关闭windwow功能里找到telnet client打开；
2. cmd输入`telnet [ip] [port]`即可连接上nc.exe的服务端，然后输入字符实时在服务端显示，telnet就是个像ssh的连接工具。

### 参考


[我不是猪八戒](https://juejin.im/post/5e04ebe9e51d4557ed5439e9#heading-6)
