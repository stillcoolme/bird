## Spark核心架构

在描述Spark运行基本流程前，我们先介绍Spark基本概念，如图4-1所示：

![](http://spark.apache.org/docs/latest/img/cluster-overview.png)

- **Applocation**：是指用户编写的Spark应用程序，包含驱动程序 Driver 和分布在集群多个节点上运行的Executor代码，在执行过程中由一到多个作业组成。
- **Driver( 驱动程序)：**运行上述Application的 main函数 并创建SparkContext。创建SparkContext 为了准备Spark Application 的运行环境。SparkContext 负责与ClusterManager通信，让 ClusterManager 进行**资源申请、任务分配和监控**等。当Executor执行完毕，Driver负责将SparkContext关闭。
- **Deploy mode**：where the driver process runs. In "cluster" mode, the framework launches the driver inside of the cluster. In "client" mode, the submitter launches the driver outside of the cluster.
- **Cluster Manager(集群资源管理器)**：是指在集群上获取资源的外部服务，目前有以下几种资源管理器：
	1. **Standalone：**Spark原生资源管理，由**Master节点**负责资源管理
	2. **Hadoop Yarn：**由Yarn的ResourceManager负责资源的管理
	3. **Mesos：**由Mesos中的Mesos Master负责管理
- **Worker(工作节点)**：集群中任何可以运行Application代码的节点。在Yarn和Spark on Yarn模式中指的是NodeManager；在Standalone中指的是通过Slave文件配置的Worker节点。
- **Master(总控进程)**：**Spark Standalone模式**下的主节点，负责管理和分配集群资源来运行Spark Application。
- **Executor(执行进程)**：在Worker节点上运行Aplication的一个进程，该进程负责从线程池中抽取空余线程运行Task，Task并行的数量取决于分配给Executor进程的CPU数量。最终Executor将结果写入内存或磁盘中，每个Application都独立拥有一批Executor。

Spark的作业和任务调度系统是其核心，能够有效地进行调度根本原因是对任务划分有向无环图（DAG）和容错，使得从底层到顶层的各个模块之间调用和处理游刃有余。

- **Task**：分发到 executor 的工作任务，Spark执行应用的最小单元，就是一个线程。**RDD分成 n个partiiton，就有 n个 task，每个task会给到一个cpu core执行。**
- **Job**：由 Action 组成的 一个或多个 Stage，由谁提交？
- **Stage**：每个Job会因为RDD之间的依赖关系拆分成多组任务集合（TaskSet），调度阶段的划分是由DAGScheduler来划分的，调度阶段有Shuffle Map Stage和Result Stage两种。
- **DAGScheduler：**DAGScheduler是面向调度阶段的任务调度器，负责接收**Spark应用**提交的作业，根据RDD的依赖关系划分 Stage ，并提交调度阶段给TaskScheduler。
- **TaskScheduler：**TaskScheduler是面向任务的调度器，接收DAGScheduler提交过来的调度阶段，把 Task任务 分发到worker节点执行，由Worker节点的Executor来实际运行任务。

### 相关流程

#### 作业提交流程

1、Driver程序的代码运行到action操作，触发了SparkContext的runJob方法。
2、SparkContext调用DAGScheduler的runJob函数。
3、DAGScheduler把Job划分stage，然后把stage转化为相应的Tasks，把Tasks交给TaskScheduler。
4、通过TaskScheduler把Tasks添加到任务队列当中，交给SchedulerBackend进行资源分配和任务调度。
5、调度器给Task分配执行Executor，ExecutorBackend负责执行Task。

![](https://raw.githubusercontent.com/janehzhang/mypic/master/2020/03spark-submit.png)



## SparkShuffle

读过源码？可以说说 SparkShuffle 啊

Shuffle 是连接 MapTask 和 ReduceTask 过程的桥梁，MapTask 的输出必须要经过 Shuffle 过程才能变成 ReduceTask 的输入，在分布式集群中，ReduceTask 需要跨节点去拉取 MapTask 的输出结果，这中间涉及到数据的网络传输和磁盘IO，所以Shuffle的好坏将直接影响整个应用程序的性能，通常我们将Shuffle过程分成两部分：MapTask端的结果输出成为ShuffleWrite, ReduceTask端的数据拉取称为ShuffleRead。

**普通Shuffle原理**





Spark2.x中已经将Hash Shuffle废弃，我自己也去看了Spark2.2.0的源码，在Spark-env初始化中只保留了两种Shuffle：Sort、Tungsten-Sort









## Spark Yarn

spark中Yarn的工作流程和一些总结

![spark-yarn](https://raw.githubusercontent.com/stillcoolme/mypic/master/2020/202003/spark-yarn.png)

### 解耦思想

- ResourceManager管理资源调度，与NodeManager直接联系；Driver负责执行计算，与Executor也就是一个个Task直接联系。
- **计算和资源调度解耦**：ResourceManager和Driver靠中间件AppMaster联系起来；Executor和NodeManager靠中间件Container联系起来
- 此时计算框架是**可插拔**的，如：spark计算框架（紫色部分）代替mapreduce。

### Client和Cluster模式

spark上yarn有两种管理模式，**YARN-Client**和**YARN-Cluster**。

主要区别是：SparkContext初始化位置不同，也就是了Driver所在位置的不同。

|           client           |       master        |
| :------------------------: | :-----------------: |
|      driver在Client上      | driver在AppMaster上 |
| 日志可以直接在Client上看到 |  日志在某个节点上   |
|     Client连接不能断开     | Client连接可以断开  |
|       适合交互和调试       |    适合生产环境     |





## RDD、DataFrame和DataSet

**RDD**

**类型安全，面向对象编程风格，序列化以及反序列化开销大。**

**DataFrame**

类似传统数据库的二维表格，除了数据以外，还记录数据的结构信息，即schema。也就是普通RDD添加结构化信息得到。可以视为Row类型的Dataset (Dataset[Row])，非类型安全，不是面向对象编程风格。

**DataSet**

强类型的，类型安全，面向对象编程风格存储的是对象。继承了RDD和DataFrame的优点。数据以编码的二进制形式存储，将对象的schema映射为SparkSQL类型，不需要反序列化就可以进行shuffle等操作，每条记录存储的则是强类型值。

相同点

- 都是基于RDD的，所以都有RDD的特性，如懒加载，分布式，不可修改，分区等等。但执行sql性能比RDD高，因为spark自动会使用优化策略执行。
- 均支持sparksql的操作，还能注册临时表，进行sql语句操作
- `DataFrame`和`Dataset`均可使用模式匹配获取各个字段的值和类型
- `DataFrame`也叫`Dataset[Row]`，每一行的类型是Row

不同点

因为`DataFrame`也叫`Dataset[Row]`，所以我们理解了**Row**和**普通对象**的区别就好办了

- **Row**的数据结构类似一个**数组**，只有顺序，切记。**普通对象**的数据结构也就是对象。
- 因此，访问**Row**只能通过如：`getInt(i: Int)`解析数据 或者 通过模式匹配得到数据；而**普通对象**可以通过 `.`号 直接访问对象中成员变量。
- 同理，**Row**中数据没类型，没办法在编译的时候检查是否有类型错误（弱类型的概念）；相反**普通对象**可以（强类型）。


### 三者的转换

![](https://raw.githubusercontent.com/stillcoolme/mypic/master/2020/202003/df-convert.png)

![dataframe2dataset.png](https://i.loli.net/2020/01/16/PkpdJSz4EYGv9sR.png)



**注意**

- 原始RDD类型是 `RDD[(Int, String)]`
- DataFrame -> RDD 时，变成了`RDD[Row]`
- DataSet -> RDD时，变成了`RDD[User]`
- DataSet<Row> -> RDD时，变成`RDD[Row]`

**使用**

```scala
// 创建一个 DataFrame、DataSet<Row>
val df: DataFrame = spark.read().textFile("file1.txt");  
或者
val df: DataFrame = spark.read.json("people.json")
```

**SQL风格（主要）**

```
// 通过SQL语句实现对表的操作
df.createOrReplaceTempView("people")
spark.sql("SELECT * FROM people").show()
```

**DSL风格（次要）**

```
// 使用DataFrame的api
df.select("name").show()
df.select($"name", $"age" + 1).show()
```










## 参考

翻一下书好了，看来看去又看回最基础的，最基础的应该联想的起来啊，还是在山脚下徘徊，没有长进。