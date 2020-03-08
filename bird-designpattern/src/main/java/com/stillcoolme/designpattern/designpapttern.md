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

#### 写法一：将 Builder 静态类 直接写在 某个XX类里面

> 特点
1. 设置每个属性，然后return this;
2. 最后通过 build() 来 set 各属性，然后 return 构造好的主类实例；
3. 最后要验证各属性是否符合要求，必set的有没有set，就 throw new RuntimeException(");


#### 写法二：直接写一个某个XX类的 Builder 类

这样外部应用使用的是`XXBuilder.newBuilder().setFiled().setFiled().build()`

例子:

* quartz 的 TriggerBuilder，JobBuilder