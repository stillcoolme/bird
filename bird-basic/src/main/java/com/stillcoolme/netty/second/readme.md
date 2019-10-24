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