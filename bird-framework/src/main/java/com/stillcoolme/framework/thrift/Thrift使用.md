## Thrift简介

> The Apache Thrift software framework, for scalable cross-language services development, combines a software stack with a code generation engine to build services that work efficiently and seamlessly between C++, Java and other languages.

通过官方介绍，我们可以了解到Thrift是一个软件框架，可以提供跨语言的服务开发。Thrift框架包含一个软件栈，包含生成各种语言的引擎，我们通过Thrift提供的接口定义语言（IDL）来定义接口，然后引擎会自动生成各种语言的代码。

类似于计算机网络，Thrift包含一套完整的栈来创建客户端和服务端程序。

- 顶层部分是由Thrift定义生成的代码。而服务则由这个文件客户端和处理器代码生成。在生成的代码里会创建不同于内建类型的数据结构，并将其作为结果发送。
- 协议层和传输层是运行时库的一部分。有了Thrift，就可以定义一个服务或改变通讯和传输协议，而无需重新编译代码。除了客户端部分之外，Thrift还包括服务器基础设施来集成协议和传输，如阻塞、非阻塞及多线程服务器。
- IO层在栈中作为基础部分对于不同的语言则有不同的实现。

![](https://raw.githubusercontent.com/stillcoolme/mypic/master/2020/202003/thrift-layer.jpg)

![img](https:////upload-images.jianshu.io/upload_images/5086559-6529adc5b5516f8f.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/400/format/webp)



## 安装与Demo

[官方介绍安装下载](http://thrift.apache.org/tutorial/ )

修改名称为Thrift.exe，放到 Thrift文件夹，将文件路径添加到系统变量Path中，执行`thrift -version`

添加依赖

```xml
<dependency>
    <groupId>org.apache.thrift</groupId>
    <artifactId>libthrift</artifactId>
    <version>0.12.0</version>
</dependency>
```

**定义helloServer.thrift文件**

定义了一个用户对象User，包含两个字段，用户姓名username，用户年龄age。

定义了一个接口sayHello(User user)，返回用户姓名和年龄。

```csharp
namespace java com.stillcoolme.framework.thrift

struct User {
    1: string username,
    2: i32 age,
}

service UserService{
	string sayHello(1:User user)
}
```

定义完成`helloServer.thrift`文件之后，在`helloServer.thrift`文件目录，使用命令`thrift -r --gen java helloServer.thrift`生成代码。

可以看到生成的代码包含两个类，UserService.java 和 User.java 类，再将`java`文件加到项目对应包结构中。

**UserServiceImpl 对thrift接口进行实现**

```java
public class UserServiceImpl implements UserService.Iface{
    @Override
    public String sayHello(User user) throws TException {
        return "Hi,My name is " + user.username + " and My age is " + user.age;
    }

}
```

**客户端代码**
客户端主要是将User对象发送给服务端，首先，实例化TTransport ，设置IP地址、服务端口port、超时时间timeout等。然后设置协议TProtocol ，注意要和服务端保持一致。接着调用接口sayHello把User对象传递过去。

```java
public class UserClient {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8090;
    private static final int TIMEOUT = 30000;

    public void startClient() {
        TTransport transport = null;
        try {
            transport = new TSocket(SERVER_IP, SERVER_PORT);
            // 协议要和服务端一致
            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            // TProtocol protocol = new TCompactProtocol(transport);
            // TProtocol protocol = new TJSONProtocol(transport);
            UserService.Client client = new UserService.Client(protocol);
            transport.open();
            User user = new User();
            user.username = "bobo";
            user.age = 26;
            String result = client.sayHello(user);
            System.out.println("Thrift client get result: " + result);

        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        UserClient client = new UserClient();
        client.startClient();

    }

}
```

**服务端代码**

服务端代码首先要实例化TProcessor，传入我们具体的HelloWorldImpl类，本例中我们使用简单的单线程服务模型TSimpleServer来开启一个服务。

```csharp
public class UserServer {

    private static final int SERVER_PORT = 8090;

    public void startServer() {
        try {
            System.out.println("HelloWorld TSimpleServer start ....");
            TProcessor tprocessor = new UserService.Processor<>(new UserServiceImpl());

            // 简单的单线程服务模型，一般用于测试
            TServerSocket serverTransport = new TServerSocket(SERVER_PORT);
            TServer.Args tArgs = new TServer.Args(serverTransport);
            tArgs.processor(tprocessor);
            tArgs.protocolFactory(new TBinaryProtocol.Factory());
            TServer server = new TSimpleServer(tArgs);
            server.serve();

        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        UserServer server = new UserServer();
        server.startServer();
    }

}

```

**执行**

先启动`UserServer`，再启动`UserClient`获得调用的返回结果。可以发现，通过`thrift`我们只要继承`thrift`文件接口，然后在服务端编写好业务实现，就可以提供给远程客户端调用，轻松实现`RPC`。



## 进阶使用



对于开发人员而言，使用原生的`Thrift`框架，仅需要关注以下四个核心**内部接口/类**：`Iface`, `AsyncIface`, `Client`和`AsyncClient`。（唉，我怎么没早看出这个啊。。。）

- **Iface**：**服务端**通过实现`HelloWorldService.Iface`接口，向**客户端**的提供具体的**同步**业务逻辑。
- **AsyncIface**：**服务端**通过实现`HelloWorldService.Iface`接口，向**客户端**的提供具体的**异步**业务逻辑。
- **Client**：**客户端**通过`HelloWorldService.Client`的实例对象，以**同步**的方式**访问服务端**提供的服务方法。
- **AsyncClient**：**客户端**通过`HelloWorldService.AsyncClient`的实例对象，以**异步**的方式**访问服务端**提供的服务方法。