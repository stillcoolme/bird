![](https://raw.githubusercontent.com/stillcoolme/mypic/master/2020/202003/design-all.png)



## 面向对象





## 设计原则





## 创建型

### Singleton

一个类只允许创建唯一 一个对象（或者实例），那这个类就是一个单例类，这种设计模式就叫作单例模式。  

> **为什么使用单例？**

1. 防止多个相同对象资源访问冲突；
   1. 这种也可以通过加分布式锁 或者 队列 来玩，但是还是单例简单；
2. 表示全局唯一类，减少类创建。比如比如配置信息类、连接池类。  

> 如何实现单例?

* 构造函数需要是 private 访问权限的，这样才能避免外部通过 new 创建实例；
* 考虑对象创建时的线程安全问题；
* 考虑是否支持延迟加载；
* 考虑 getInstance() 性能是否高（是否加锁）。  

#### 饿汉式

有人觉得这种实现方式不好，因为不支持延迟加载，如果实例占用资源多（比如占用内存多）
或初始化耗时长（比如需要加载各种配置文件），提前初始化实例是一种浪费资源的行为。如果需要依赖其他类来进行初始化也不好弄。

不过，如果初始化耗时长，那我们最好不要等到真正要用它的时候，才去执行这个耗时长的初始化过
程，这会影响到系统的性能。采用饿汉式实现方式，将耗时的初始化操作，提前到
程序启动的时候完成，这样就能避免在程序运行的时候，再去初始化导致的性能问题。 

#### 懒汉式

懒汉式相对于饿汉式的优势是支持延迟加载。



#### 单例存在哪些问题？

* 对 OOP 4大特性特性的支持不友好
* 会隐藏类之间的依赖关系
* 对代码的扩展性不友好
* 对代码的可测试性不友好
* 不支持有参数的构造函数，解决思路：
  1. 加一个 init(参数1, 参数2) 方法来传递参数，先调用 init() 里面 new 单例对象，然后才能调用 getInstance() 方法
  2. 将参数放到另外一个全局静态变量中，变量的值写死，或配置文件中读取得到。

\2. 单例有什么替代解决方案？
为了保证全局唯一，除了使用单例，我们还可以用静态方法来实现。不过，静态方法这种实现
思路，并不能解决我们之前提到的问题。如果要完全解决这些问题，我们可能要从根上，寻找
其他方式来实现全局唯一类了。比如，通过工厂模式、IOC 容器（比如 Spring IOC 容器）来
保证，由过程序员自己来保证（自己在编写代码的时候自己保证不要创建两个类对象）。
有人把单例当作反模式，主张杜绝在项目中使用。我个人觉得这有点极端。模式没有对错，关
键看你怎么用。如果单例类并没有后续扩展的需求，并且不依赖外部系统，那设计成单例类就



### SimpleFac

也叫静态工厂方法

工厂类中创建对象的方法一般都是 createXX()，但有的也命名为 getInstance()、createInstance() 、valueOf()  

两种形式：

1. 直接 switch 来 new 具体实现对象的；

   ```java
   public static IRuleConfigParser createParser(String configFormat) {
       IRuleConfigParser parser = null;
       if ("json".equalsIgnoreCase(configFormat)) {
           parser = new JsonRuleConfigParser();
       } else if ("xml".equalsIgnoreCase(configFormat)) {
           parser = new XmlRuleConfigParser();
       } else if ("yaml".equalsIgnoreCase(configFormat)) {
           parser = new YamlRuleConfigParser();
       }
       return parser;
   }
   ```

2. 提前注册各种具体实现到 map 中，然后 get

   ```java
   // 注册式的简单工厂
   public static IRuleConfigParser createParser2(String configFormat) {
       if(configFormat == null || configFormat.isEmpty()) {
           return null;    //返回null还是IllegalArgumentException全凭你自己说了算
       }
       IRuleConfigParser ruleConfigParser =  cachedParsers.get(configFormat.toLowerCase());
       return ruleConfigParser;
   }
   
   private static final Map<String, IRuleConfigParser> cachedParsers = new ConcurrentHashMap();
   static {
       cachedParsers.put("json", new JsonRuleConfigParser());
       cachedParsers.put("xml", new XmlRuleConfigParser());
       cachedParsers.put("yaml", new YamlRuleConfigParser());
   }
   ```

> 优缺点

尽管简单工厂模式的代码实现中，有多处 if 分支判断逻辑，一旦修改就要动 Factory 类的代码，违背开闭原则。

但权衡扩展性和可读性，这样的代码实现在大多数情况下（比如，不需要频繁地添加parser，也没有太多的 parser）是没有问题的。  

### FacMethod

如果我们非得要将 if 分支逻辑去掉，那该怎么办呢？比较经典处理方法就是利用多态。

把 工厂类 弄成接口了！具体工厂具体写实现类。当我们新增一种 parser 的时候，只需要新增一个实现了 IRuleConfigParserFactory 接口的 Factory 类即可。所以，工厂方法模式比起简单工厂模式更加符合开闭原则。

```java
public interface IRuleConfigParserFactory {

    public IRuleConfigParser createParser(String configFormat);

}

public class JsonRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParser createParser(String configFormat) {
        return null;
    }
}
```

但是这样写，使用的时候又得对用哪个具体工厂在  if else 了！

其实，我们可以为工厂类再创建一个简单工厂，也就是工厂的工厂，用来创建工厂类对象。  沿用 简单工厂的第二种实现，将各种 工厂 也放到Map里面缓存起来！！！

> 优缺点

对于规则配置文件解析这个应用场景来说，工厂模式需要额外创建诸多 Factory类，也会增加代码的复杂性，而且，每个 Factory 类只是做简单的 new 操作，功能非常单薄（只有一行代码），也没必要设计成独立的类，所以，在这个应用场景下，简单工厂模式简单好用，比工方法厂模式更加合适。  

哈哈，从 简单工厂 到 工厂方法 ，发现  复杂度无法被消除，只能被转移  



### DI

DI 容器跟我们讲的工厂模式又有何区别和联系？DI 容器的核心功能有哪些，以及如何实现一个简单的 DI 容器？  

DI 容器相当于一个大的工厂类，负责在程序启动的时候，根据配置（要创建哪些类对象，每个类对象的创建需要依赖哪
些其他类对象）事先创建好对象。当应用程序需要使用某个类对象的时候，直接从容器中获取即可。正是因为它持有一堆对象，所以这个框架才被称为“容器”。  

一个工厂类只负责某个类对象或者某一组相关类对象（继承自同一抽象类或者接口的子类）的创建，而 DI 容器负责的是整个应用中所有类对象的创建。

DI 容器负责的事情要比单纯的工厂模式要多。比如，它还包括配置的解析、对象创建（BeansFactory 牛逼)、对象生命周期管理 （添加了 beforeInit()，afterDestory() 等方法）。    





### Builder

当创建一个对象时，设置它的成员参数太麻烦的时候就可以用这个。

> 写法一：将 Builder 静态类 直接写在 某个XX类里面

1. 设置每个属性，然后return this;
2. 最后通过 build() 来 set 各属性，然后 return 构造好的主类实例；
3. 最后要验证各属性是否符合要求，必set的有没有set，就 throw new RuntimeException(");



> 写法二：直接写一个某个XX类的 Builder 类

这样外部应用使用的是`XXBuilder.newBuilder().setFiled().setFiled().build()`

例子:

* quartz 的 TriggerBuilder，JobBuilder



## 结构型

代理、桥接、装饰器、适配器，这 4 种模式是比较常用的结构型设计模式。它们的代码结构非常相似。笼统来说，它们都可以称为 Wrapper 模式，也就是通过 Wrapper 类二次封装原始类。  

尽管代码结构相似，但这 4 种设计模式的用意完全不同，也就是说要解决的问题、应用场景不同，这也是它们的主要区别。这里我就简单说一下它们之间的区别。

* 代理模式：代理模式在不改变原始类接口的条件下，为原始类定义一个代理类，主要目的是控制访问，而非加强功能，这是它跟装饰器模式最大的不同。
* 桥接模式：桥接模式的目的是将接口部分和实现部分分离，从而让它们可以较为容易、也相对独立地加以改变。
* 装饰器模式：装饰者模式在不改变原始类接口的情况下，对原始类功能进行增强，并且支持多个装饰器的嵌套使用。
* 适配器模式：适配器模式是一种**事后的补救策略。适配器提供跟原始类不同的接口**，而**代理模式、装饰器模式提供的都是跟原始类相同的接口**。  

### 代理模式

#### 静态代理

为了将框架代码和业务代码解耦，代理模式就派上用场了。

> 静态代理1：实现相同接口，再委托

代理类 UserControllerProxy和原始类 UserController 实现相同的接口 IUserController。代理类 UserControllerProxy 负责在业务代码执行前后附加其他逻辑代码，并**通过委托的方式调用原始类**来执行业务代码。  （其实是组合的方式）

```java
public class UserControllerProxy implements IUserController {
    private MetricsCollector metricsCollector;
    private UserController userController;
    
    public UserControllerProxy(UserController userController) {
        this.userController = userController;
        this.metricsCollector = new MetricsCollector();
    }
    
    @Override
    public UserVo login(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();
        // 委托，真实业务调用
        UserVo userVo = userController.login(telephone, password);
        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimes);
        metricsCollector.recordRequest(requestInfo);
        return userVo;
    } 
    @Override
    public UserVo register(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();
        // 委托，真实业务调用
        UserVo userVo = userController.register(telephone, password);
        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("register", responseTime, startTimes);
        metricsCollector.recordRequest(requestInfo);
        return userVo;
    }
}
```



> 静态代理2：继承业务类

如果原始类并没有定义接口，并且原始类代码并不是我们开发维护的（比如它来自一个第三方的类库），我们也没办法直接修改原始类，给它重新定义一个接口。在这种情况下，我们一般都是采用继承的方式。  

```java
public class UserControllerProxy extends UserController {
    private MetricsCollector metricsCollector;
    
    public UserControllerProxy() {
    	this.metricsCollector = new MetricsCollector();
    }
    
    public UserVo login(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();
        // 
        UserVo userVo = super.login(telephone, password);
        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimes
        metricsCollector.recordRequest(requestInfo);
        return userVo;
    } 
    public UserVo register(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();
        //
        UserVo userVo = super.register(telephone, password);
        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("register", responseTime, startTi
        metricsCollector.recordRequest(requestInfo);
        return userVo;
    }
}
//UserControllerProxy使用举例
UserController userController = new UserControllerProxy();
```



上面两种方式的代码实现还是有点问题。

一方面，我们需要在代理类中，将原始类中的所有的方法，都重新实现一遍，**并且为每个方法都附加相似的代码逻辑**。另一方面，如果要添加的附加功能的类有不止一个，我们需要针对每个类都创建一个代理类。  

所以就有了动态代理

#### 动态代理

所谓动态代理（Dynamic Proxy），就是我们不事先为每个原始类编写代理类，而是**在运行时**，动态地创建原始类对应的代理类，然后在系统中用代理类替换掉原始类。

> 代理模式的应用场景  

1.  业务系统的非功能性需求开发。比如：监控、统计、鉴权、限流、事务、幂等、日志。  
2. 代理模式在 RPC、缓存中的应用。
   1. 实际上，RPC 框架也可以看作一种代理模式。通过远程代理，将网络通信、数据编解码等细节隐藏起来。客户端在使用 RPC 服务的时候，就像使用本地函数一样，无需了解跟服务器交互的细节。RPC 服务的开发者也只需要开发业务逻辑，就像开发本地使用的函数一样，不需要关注跟客户端的交互细节。
   2. 在缓存时的使用。加入接口要支持从缓存取用户数据 和 实时取。就可以将缓存取的逻辑写到代理里。当请求参数有 getcache = true，就走代理的缓存逻辑直接拿。



### 装饰器模式

```java
InputStream in = new FileInputStream("/user/wangzheng/test.txt");
InputStream bin = new BufferedInputStream(in);
byte[] data = new byte[128];
while (bin.read(data) != -1) {
    // ...
}
```

初看上面的代码，我们会觉得 Java IO 的用法比较麻烦，需要先创建一个 FileInputStream 对象，然后再传递给 BufferedInputStream 对象来使用。

为什么不设计一个继承 FileInputStream 并且支持缓存的 BufferedFileInputStream 类呢？这样我们就可以像下面的代码中这样，直接创建一个 BufferedFileInputStream 类对象，打开文件读取数据，用起来岂不是更加简单？  

```java
InputStream bin = new BufferedFileInputStream("/user/wangzheng/test.txt");
byte[] data = new byte[128];
while (bin.read(data) != -1) {
//...
}
```

> 基于继承的设计方案

如果 InputStream 只有一个子类 FileInputStream 的话，那我们在 FileInputStream 基础之上，再设计一个孙子类 BufferedFileInputStream，也算是可以接受的，毕竟继承结构还算简单。

但实际上，继承 InputStream 的子类有很多。我们需要给每一个 InputStream 的子类，再继续派生支持缓存读取的子类。
除了支持缓存读取之外，如果我们还需要对功能进行其他方面的增强，比如下面的 DataInputStream 类，支持按照基本数据类型（int、boolean、long 等）来读取数据。  

在这种情况下，如果我们继续按照继承的方式来实现的话，就需要再继续派生出 DataFileInputStream、DataPipedInputStream 等类。如果我们还需要既支持缓存、又支持按照基本类型读取数据的类，那就要再继续派生出 BufferedDataFileInputStream、BufferedDataPipedInputStream 等 n 多类。

类继承结构变得无比复杂，代码既不好扩展，也不好维护。

> 基于装饰器模式的设计方案

针对刚刚的继承结构过于复杂的问题，我们可以通过将继承关系改为组合关系来解决。下面的代码展示
了 Java IO 的这种设计思路。

```java
public abstract class InputStream {

    public int read(byte b[]) throws IOException {
    	return read(b, 0, b.length);
	}
    public int read(byte b[], int off, int len) throws IOException {

    }
    public long skip(long n) throws IOException {
    
    }
    public int available() throws IOException {
    	return 0;
    }
    public void close() throws IOException {}
    public synchronized void mark(int readlimit) {}
    public synchronized void reset() throws IOException {
    	throw new IOException("mark/reset not supported");
    }
    public boolean markSupported() {
    	return false;
    }
}

public class BufferedInputStream extends InputStream {
    // 组合进来
    protected volatile InputStream in;
    protected BufferedInputStream(InputStream in) {
    	this.in = in;
    }
	//...实现基于缓存的读数据接口...
}

public class DataInputStream extends InputStream {
    // 组合进来
    protected volatile InputStream in;
    protected DataInputStream(InputStream in) {
    	this.in = in;
    }
	//...实现读取基本类型数据的接口
}   
        
```

看了上面的代码，你可能会问，那装饰器模式就是简单的“用组合替代继承”吗？当然不是。从 Java IO 的设计来看，装饰器模式相对于简单的组合关系，还有两个比较特殊的地方。

1. 装饰器类和原始类都**继承同样的父类（抽象类或接口）**，这样我们可以对原始类“嵌套”多个装饰器类。比如，我们对 FileInputStream 嵌套了两个装饰器类：BufferedInputStream 和 DataInputStream，让它既支持缓存读取，又支持按照基本数据类型来读取数据。  
2. 装饰器类是对功能的增强（装饰器模式应用场景的重要特点）。实际上，符合 “组合关系” 这种代码结构的设计模式有很多， 尽管代码结构很相似，但是每种设计模式的意图是不同的。代理模式中，**代理类附加的是跟原始类无关的功能**，而在装饰器模式中，装饰器类附加的是跟原始类相关的增强功能，**继承关系过于复杂的问题，通过组合来替代继承**。

实际上，如果去查看 JDK 的源码，你会发现，BufferedInputStream、DataInputStream 并非继承自 InputStream，而是另外一个叫 FilterInputStream 的类，然后 FilterInputStream 才继承 InputStream 。那这又是出于什么样的设计意图，才引入这样一个类呢？  

为了避免代码重复，Java IO 抽象出了一个装饰器父类 FilterInputStream 。InputStream 的所有的装饰器类（BufferedInputStream、DataInputStream）都继承自这个装饰器父类。这样，装饰器类只需要实现它需要增强的方法就可以了，其他不用增强的方法继承装饰器父类的默认实现。  



### 适配器模式

适配器模式（Adapter Design Pattern），它将不兼容的接口转换为可兼容的接口，让原本由于接口不兼容而不能一起工作的类可以一起工作。  

适配器模式有两种实现方式：类适配器和对象适配器。其中，类适配器使用继承关系来实现，对象适配器使用组合关系来实现。 

```java
// 假设现在有这个 不通用的业务接口 Adaptee 需要适配
public class Adaptee {  // 
    public void fa() { //... }
    public void fb() { //... }
    public void fc() { //... }
}

// 首先 自己额外定义这个接口
public interface ITarget {
    void f1();
    void f2();
    void fc();
}

// 1. 类适配器: 基于继承
public class Adaptor extends Adaptee implements ITarget {
    public void f1() {
    	super.fa();
    }
    public void f2() {
    //...重新实现f2()...
    } 
    // 这里fc()不需要实现，直接继承自Adaptee，这是跟对象适配器最大的不同点！！！
 } 

// 2. 对象适配器：基于组合
public class Adaptor implements ITarget {
    // 将要适配的接口 组合进来
    private Adaptee adaptee;
    
    public Adaptor(Adaptee adaptee) {
 	   this.adaptee = adaptee;
    }
    
    public void f1() {
    	adaptee.fa();	//委托给Adaptee
    }
    public void f2() {
    //...重新实现f2()...
    } 
    
    public void fc() {
		adaptee.fc();
	}
 } 
```



针对这两种实现方式，在实际的开发中如何选择使用哪一种呢？判断的标准主要有两个，一个是 Adaptee 接口的个数，另一个是 Adaptee 和 ITarget 的契合程度。  

* 如果 Adaptee 接口并不多，那两种实现方式都可以。
* 如果 Adaptee 接口很多，而且 Adaptee 和 ITarget 接口定义大部分都相同，那推荐使用类适配器，**因为 Adaptor 可以复用父类 Adaptee 的接口**。
* 如果 Adaptee 接口很多，而且 Adaptee 和 ITarget 接口定义大部分都不相同，那推荐使用对象适配器，因为组合结构相对于继承更加灵活。
* 另外，如果有多个 Adaptee ，也是这样玩，就多个 Adaptor 实现类，多态的味道。

一般来说，适配器模式可以看作一种“补偿模式”，用来补救设计上的缺陷。应用这种模式算是“无奈之举”。如果在设计初期，我们就能协调规避接口不兼容的问题，那这种模式就没有应用的机会了。我们反复提到，适配器模式的应用场景是“接口不兼容”。那在实际的开发中，什么情况下才会出现接口不兼容呢？  

**1. 封装有缺陷的接口设计**

假设我们依赖的外部系统在接口设计方面有缺陷（比如包含大量静态方法影响测试，方法参数多需要封成对象）。为了隔离设计上的缺陷，我们希望对外部系统提供的接口进行二次封装，抽象出更好的接口设计，这个时候就可以使用适配器模式了。  

**2.统一多个类的接口设计**

某个功能的实现依赖多个外部系统（或者说类）。通过适配器模式，将它们的接口**适配为统一的接口定义**，然后我们就可以使用多态的特性来复用代码逻辑。

假设我们的系统要对用户输入的文本内容做敏感词过滤，为了提高过滤的召回率，我们引入
了多款第三方敏感词过滤系统，依次对用户输入的敏感内容进行过滤。但是，每个系统提供的过滤接口都是不同的。这就意味着我们没法复用一套逻辑来调用各个系统，我们就可以使用适配器模式，将所有系统的接口适配为统一的接口定义，复用调用敏感词过滤的代码。  

**3.替换依赖的外部系统**
当我们把项目中依赖的一个外部系统A替换为另一个外部系统B的时候，利用适配器模式，可以减少对代码的改动。  

比如 外部系统A 首先实现了接口 IA，那么我来个  Badapter implements IA，再将外部系统B 组合到 Badapter 里面就可以完成适配。

**4.兼容老版本接口**
在做版本升级的时候，对于一些要废弃的接口，我们不直接将其删除，而是暂时保留，并且标注 deprecated，并将内部实现逻辑委托为新的接口实现。这样做的好处是，让使用它的项目有个过渡期，而不是强制进行代码修改。这也可以粗略地看作适配器模式的一个应用场景。  

JDK1.0 中包含一个遍历集合容器的类 Enumeration。JDK2.0 对这个类进行了重构，将它改名为 Iterator 类，并且对它的代码实现做了优化。但是考虑到如果将 Enumeration 直接从 JDK2.0 中删除，那使用 JDK1.0 的项目如果切换到 JDK2.0，代码就会编译不通过。为了避免这种情况的发生，我们必须把项目中所有使用到 Enumeration 的地方，都修改为使用 Iterator 才行。  

```java
// 暂时保留 Enumeration 类，并将其实现替换为直接调用 Itertor。
public class Collections {
    public static Emueration emumeration(final Collection c) {
        return new Enumeration() {
            Iterator i = c.iterator();
            public boolean hasMoreElments() {
            	return i.hashNext();
            }
            public Object nextElement() {
            	return i.next():
            }
        }
    }
}
```

**5.适配不同格式的数据**
前面我们讲到，适配器模式主要用于接口的适配。实际上，它还可以用在不同格式的数据之间的适配。比如，把从不同征信系统拉取的不同格式的征信数据，统一为相同的格式，以方便存储和使用。再比如，Java 中的 Arrays.asList() 也可以看作一种数据适配器，将数组类型的数据转化为集合容器类型：`List<String> stooges = Arrays.asList("Larry", "Moe", "Curly");  `

**6.剖析适配器模式在 Java 日志中的应用**

Slf4j 这个日志框架你肯定不陌生，它相当于 JDBC 规范，提供了一套打印日志的统一接口规范。不过，它只定义了接口，并没有提供具体的实现，需要配合其他日志框架（log4j、logback……）来使用。

不仅如此，Slf4j 的出现晚于 JUL、JCL、log4j 等日志框架，所以，这些日志框架也不可能牺牲掉版本兼容性，将接口改造成符合 Slf4j 接口规范。所以，**Slf4j 不仅仅提供了统一的接口定义，还提供了针对不同日志框架的适配器**。对不同日志框架的接口进行二次封装，适配成统一的 Slf4j 接口定义。  

```java
// 适配器接口！！！
public interface Logger {
    public boolean isTraceEnabled();
    public void debug(String msg);
    public void debug(String format, Object arg);
    //...省略info、warn、error等一堆接口
}

// log4j日志框架的适配器
// Log4jLoggerAdapter 实现了 LocationAwareLogger 接口，
// 其中LocationAwareLogger继承自Logger接口，也就相当于Log4jLoggerAdapter实现了Logger接口。
public final class Log4jLoggerAdapter extends MarkerIgnoringBase implements LocationAwareLogger {
    final transient org.apache.log4j.Logger logger; // 将需要适配的 log4j 组合进来
    public boolean isDebugEnabled() {
    	return logger.isDebugEnabled();
    } 
    public void debug(String msg) {
    	logger.log(FQCN, Level.DEBUG, msg, null);
    }
    public void debug(String format, Object arg) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
}
```

所以，在开发业务系统或者开发框架、组件的时候，我们统一使用 Slf4j 提供的接口来编写打印日志的代码，具体使用哪种日志框架实现（log4j、logback……），是可以动态地指定的（使用 Java 的 SPI 技术，这里我不多解释，你自行研究吧），只需要将相应的 SDK 导入到项目中即可。

不过，你可能会说，如果一些老的项目没有使用 Slf4j，而是直接使用比如 JCL 来打印日志，那如果想要替换成其他日志框架，比如 log4j，该怎么办呢？实际上，Slf4j 不仅仅提供了从其他日志框架到 Slf4j 的适配器，还提供了反向适配器，也就是从 Slf4j 到其他日志框架的适配。我们可以先将 JCL 切换为 Slf4j，然后再将 Slf4j 切换为 log4j。经过两次适配器的转换，我们能就成功将 JCL 切换为了 log4j2。  





### 门面模式

门面模式（Facade Design Pattern）原理和实现都特别简单：***门面模式为子系统提供一组统一的接口，定义一组高层接口让子系统更易用**。应用场景比较明确，主要在接口设计的粒度方面问题，解决接口的可复用性和易用性之间的矛盾：

* 为了保证接口的可复用性（或者叫通用性），我们需要将接口尽量设计得细粒度一点，职责单一一点。但如果接口的粒度过小，在接口的使用者开发一个业务功能时，就会导致需要调用 n 多细粒度的接口才能完成。调用者肯定会抱怨接口不好用 。
* 相反，如果接口粒度设计得太大，一个接口返回 n 多数据，要做 n 多事情，就会导致接口不够通用、可复用性不好。接口不可复用，那针对不同的调用者的业务需求，我们就需要开发不同的接口来满足，这就会导致系统的接口无限膨胀。



适配器模式和门面模式的共同点是，将不好用的接口适配成好用的接口。你可以试着总结一下它们的区别吗？  

适配器是做接口转换，解决的是原接口和目标接口不匹配的问题。
门面模式做接口整合，解决的是多接口调用带来的问题。  