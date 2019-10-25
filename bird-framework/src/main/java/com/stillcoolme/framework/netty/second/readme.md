## 将编解码换成自定义的对象来传输数据，jdk方式的序列化

first包中传输字符串用的是框架自带的StringEncoder，StringDecoder编解码器。

现在想要通过对象来传输数据，该怎么弄呢？

既然StringEncoder和StringDecoder可以传输字符串，我们来看看这2个类的源码不就知道它们到底做了一些什么工作。

通过源码可以知道，StringEncoder通过继承MessageToMessageEncoder，重写encode方法来进行编码操作，将字符串编码成ByteBuf进行输出。

StringDecoder继承MessageToMessageDecoder，重写decode方法，将ByteBuf数据直接转成字符串进行输出，解码完成。

通过上面的源码分析，我们发现编解码的原理无非就是在数据传输前进行一次处理，接收后再进行一次处理，

在网络中传输的数据都是字节，我们现在想要传PO对象，

那么必然需要进行编码和解码2个步骤，我们可以自定义编解码器来对对象进行序列化，

然后通过ByteBuf的形式进行传输, 传输对象需要实现java.io.Serializable接口。

## 其他序列化方式高性能数据传输
- [整合kryo序列化高性能数据传输](http://cxytiandi.com/blog/detail/17436)
- [整合Protobuf序列化高性能数据传输](http://cxytiandi.com/blog/detail/17469)
- [高性能NIO框架Netty-Netty4自带编解码器详解](http://cxytiandi.com/blog/detail/17547)


## 添加心跳检测
用Netty实现长连接服务，当发生下面的情况时，会发生断线的情况。

* 网络问题
* 客户端启动时服务端挂掉了，连接不上服务端
* 客户端已经连接服务端，服务端突然挂掉了
* 其它问题等…

1. 心跳机制检测连接存活

长连接是指建立的连接长期保持，不管有无数据包的发送都要保持连接通畅。

心跳是用来检测一个系统是否存活或者网络链路是否通畅的一种方式，

一般的做法是**客户端定时向服务端发送心跳包，服务端收到心跳包后进行回复，客户端收到回复说明服务端存活**。

否则认为服务端已经出故障了，可以重新连接或者选择其他的可用的服务进行连接。

在Netty中提供了一个IdleStateHandler类用于心跳检测

2. 启动时连接重试

连接的操作是客户端这边执行的，重连的逻辑也得加在客户端

增加一个负责重试逻辑的监听器 ConnectionListener

3. 运行中连接断开时重试

使用的过程中服务端突然挂了，就得用另一种方式来重连了，
可以在处理数据的Handler中进行处理。

## http

在后端开发中接触HTTP协议的比较多，
目前大部分都是基于Servlet容器实现的Http服务，
往往有一些核心子系统对性能的要求非常高，
这个时候我们可以考虑采用NIO的网络模型来实现HTTP服务，
Netty除了开发网络应用非常方便，还内置了HTTP相关的编解码器，
让用户可以很方便的开发出高性能的HTTP协议的服务，
Spring Webflux默认是使用的Netty。




