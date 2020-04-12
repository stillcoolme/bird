## 浪尖大纲

来点猛料，广播变量的原理及演变过程，使用场景，使用广播变量一定划算吗？大变量咋办呢？

累加器的原理及应用场景，累加器使用有陷阱么？

序列化，反序列化，闭包，垃圾回收机制（过期rdd的回收，cache的回收等）。

checkpoint如何在spark core应用呢？何种场景适合？源码系列教程。

并行度相关配置，这个星球里也反复强调了，合理设置可以大幅度提高性能。

3. spark streaming

spark streaming核心原理大家都知道是微批处理。

基于receiver和direct api两种模式的原理，最好读懂源码。

主要是跟Kafka 结合的两种模式的区别。

direct这种模式如何实现仅一次处理。checkpoint的使用。

如何进行状态管理，upstatebykey，redis，hbase，alluxio作为状态管理存储设备的时候优缺点，然后就是故障恢复会引起的问题及如何避免等等吧。

合理设置批处理时间，为啥批处理时间不能太大，也不能太小，task倾斜，数据倾斜如何解决。

内存申请，kafka分区设置的依据是啥？

并行度问题，这个也是浪尖反复强调的，彻底理解对spark任务调优帮助很大。

blockrdd和kafkardd的底层区别。

与spark sql和hivecontext结合使用。

广播变量的使用及释放机制等。

动态分区发现和topic发现机制。

executor存活监控，task执行情况监控，未处理队列积累的健康告警（非常重要）等价于对lagsize的监控告警。

小文件问题，星球里文章很详细。根源上避免才是王道。顺便提一句**：为啥namenode那么怕小文件呢？**

作为7*24小时的应用程序，如何进行监控告警及故障自动恢复～

4.spark sql

在数仓的领域，实时处理都用它，而且structured streaming也逐步依赖于sql引擎了。常见算子的使用及理解，并行度问题，大小表join，如何广播小表。

join，group by等数据倾斜如何发现及处理方法，这个浪尖还专门录制过视频，星球里球友应该都知道，不知道回去翻看一下。

常见的存储格式，parquet，txt，json，orc对比及对性能的影响。

调优大部分也是针对并行度，文件大小，数据倾斜，task倾斜，内存和cpu合理设置等。



5.structured streaming

动态表，增量sql引擎，仅一次处理，维表join等非常好用，watermark，还有就是繁杂的 join 机制。

当然限制还是很多的，期待后续版本。spark streaming在spark 2.4的时候都没更新了，后面就主推sql引擎相关内容了，还是值得期待的。不过话虽这么说，我觉得flink也相对好用，就是可能bug多些，新版本好点。



## Runtime

### 系统架构

在描述Spark运行基本流程前，我们先介绍Spark基本概念，如图4-1所示：

![](http://spark.apache.org/docs/latest/img/cluster-overview.png)

- **Applocation**：是指用户编写的Spark应用程序，包含驱动程序 Driver 和分布在集群多个节点上运行的Executor代码，在执行过程中由一到多个作业组成。
- **Driver( 驱动程序)：**运行上述Application的 main函数 并创建SparkContext。创建SparkContext 为了准备Spark Application 的运行环境。SparkContext 负责与ClusterManager通信，让 ClusterManager 进行**资源申请、任务分配和监控**等。当Executor执行完毕，Driver负责将SparkContext关闭。
- **Deploy mode**：where the driver process runs. In "cluster" mode, the framework launches the driver inside of the cluster. In "client" mode, the submitter launches the driver outside of the cluster.
- **Cluster Manager(集群资源管理器)**：是指在集群上获取资源的外部服务，目前有以下几种资源管理器：
	1. **Standalone：**Spark原生资源管理，由**Master节点**负责资源管理
	2. **Hadoop Yarn：**由Yarn的ResourceManager负责资源的管理
	3. **Mesos：**由Mesos中的Mesos Master负责管理
- **Master(总控进程)**：**Spark Standalone模式**下的主节点，负责管理和分配集群资源来运行Spark Application。
- **Worker(工作节点)**：集群中任何可以运行Application代码的节点。在 Yarn 和 Spark on Yarn模式中指的是NodeManager；在Standalone中指的是通过Slave文件配置的Worker节点。
- **Executor(执行进程)**：在Worker节点上运行Aplication的一个进程，该进程负责从线程池中抽取空余线程运行Task，Task并行的数量取决于分配给Executor进程的CPU数量。最终Executor将结果写入内存或磁盘中，每个Application都独立拥有一批Executor。

Spark的作业和任务调度系统是其核心，能够有效地进行调度根本原因是对任务划分有向无环图（DAG）和容错，使得从底层到顶层的各个模块之间调用和处理游刃有余。

- **Job**：由 Action 组成的 一个或多个 Stage，由谁提交？

- **Stage**：每个Job会因为 Shuflle ~~(RDD之间的依赖关系)~~ 拆分成多组任务集合（TaskSet），调度阶段的划分是由DAGScheduler来划分的，调度阶段有Shuffle Map Stage和Result Stage两种。

- **Task**：分发到 executor 的工作任务，Spark执行应用的最小单元，就是一个线程。**RDD分成 n个partiiton，就有 n个 task，每个task会给到一个cpu core执行。**

- **DAGScheduler：**DAGScheduler是面向调度阶段的任务调度器，负责接收**Spark应用**提交的作业，根据RDD的依赖关系划分 Stage ，并提交调度阶段给TaskScheduler。

- **TaskScheduler：**TaskScheduler是面向任务的调度器，接收DAGScheduler提交过来的调度阶段，把 Task任务 分发到worker节点执行，由Worker节点的Executor来实际运行任务。

  

**高手的问题就是如何给两者分配合适内存**，然后 executor 执行 task 倾斜 的表现及如何解决。这些都在星球里分享过了。然后如何对executor的存活挂掉新增进行监控告警。executor动态分配表现及带来的问题。

再高级一点就是driver和executor的类加载器及加载类的原理及过程，当然包括rpc，依赖传输，task调度等。



### 相关流程

#### 作业提交流程

1、Driver程序的代码运行到action操作，触发了SparkContext的runJob方法。
2、SparkContext调用DAGScheduler的runJob函数。
3、DAGScheduler把Job划分stage，然后把stage转化为相应的Tasks，把Tasks交给TaskScheduler。
4、通过TaskScheduler把Tasks添加到任务队列当中，交给SchedulerBackend进行资源分配和任务调度。
5、调度器给Task分配执行Executor，ExecutorBackend负责执行Task。

![](https://raw.githubusercontent.com/janehzhang/mypic/master/2020/03spark-submit.png)



## Core

### RDD特性

rdd不就是作为记录数据元数据的结构，里面有个partition记录数据在哪个分片，真实的数据是在 block 里面。

我们人脸项目里面也是这样玩的。



1. A list of partitions

RDD是一个由多个partition（某个节点里的某一片连续的数据）组成的的list；将数据加载为RDD时，一般会遵循数据的本地性（一般一个hdfs里的block会加载为一个partition）。

2. A function for computing each split

RDD的每个partition上面都会有function，也就是函数应用，其作用是实现RDD之间partition的转换。

3. A list of dependencies on other RDDs

RDD会记录它的依赖 ，为了容错（重算，cache，checkpoint），也就是说在内存中的RDD操作时出错或丢失会进行重算。

4. Optionally, a Partitioner for Key-value RDDs

 可选项，如果RDD里面存的数据是key-value形式，则可以传递一个自定义的Partitioner进行重新分区，例如这里自定义的Partitioner是基于key进行分区，那则会将不同RDD里面的相同key的数据放到同一个partition里面

5. Optionally, a list of preferred locations to compute each split on

最优的位置去计算，也就是数据的本地性。   



### 宽依赖窄依赖

依据 Shuffle 为界来进行 Stage 的划分。

讲到这可以先回顾一下Spark了，主要三个概念：

**1. Shuffle**

Spark job 中 shuffle个数决定着stage个数。

**2. 分区**

Spark 算子中 RDD的分区数 决定着 stage任务的并行度。

**3.** **分区传递**

复杂的入union，join等暂不提。简单的调用链如下：

rdd.map-->filter-->reducebykey-->map。

例子中假设rdd有6个分区，map到fliter的分区数传递是不变，filter到redcuebykey分区就变了，reducebykey的分区有个默认计算公式，星球里讲过了，假设我们在使用reducebykey的时候传入了一个分区数12。

分区数，map是6，filter也是6，reducebykey后面的map就是12。map这类转换完全继承了父RDD的分区器和分区数，默认无法人为设置并行度，只有在shuffle的时候，我们才可以传入并行度。

```java
// 根据父rdd的 partitioner 来
override val partitioner = if (preservesPartitioning) firstParent[T].partitioner else None

override def getPartitions: Array[Partition] = firstParent[T].partitions
```







### DAG的生成，划分，task的调度执行





## Shuffle

读过源码？可以说说 SparkShuffle 啊。





### **使用相同分区方式的join可以避免Shuffle**

**如果RDD有相同数目的分区，**join操作不需要额外的shuffle操作。

因为RDD是相同分区的，rdd1中任何一个分区的key集合都只能出现在rdd2中的单个分区中。

**因此转换到 rdd3 的时候 就是 窄依赖（独生）的，不需要 Shuffle，任何一个输出分区的内容仅仅依赖 rdd1 和 rdd2 中的单个分区**。

rdd1 = someRdd.reduceByKey(...) rdd2 = someOtherRdd.reduceByKey(...) rdd3 = rdd1.join(rdd2)

那如果rdd1和rdd2使用不同的分区器，或者使用**默认的hash分区器但配置不同的分区数呢？**

那样的话，仅仅只有一个rdd（较少分区的RDD）需要重新shuffle后再join。





## 数据倾斜

### 如何定位数据倾斜



## Spark Yarn

spark中Yarn的工作流程和一些总结

![spark-yarn](https://raw.githubusercontent.com/stillcoolme/mypic/master/2020/202003/spark-yarn.png)

### 解耦思想

- ResourceManager管理资源调度，与NodeManager直接联系；Driver负责执行计算，与Executor也就是一个个Task直接联系。
- **计算和资源调度解耦**：ResourceManager和Driver靠中间件AppMaster联系起来；Executor和NodeManager靠中间件Container联系起来
- 此时计算框架是**可插拔**的，如：spark计算框架（紫色部分）代替mapreduce。

### Client和Cluster模式

[Spark中yarn模式两种提交任务方式](https://www.cnblogs.com/LHWorldBlog/p/8414342.html)

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





## 面试问题



1. cache 和 persist 区别

   With cache(), you use only the default storage level :

- MEMORY_ONLY for **RDD**

- MEMORY_AND_DISK for **Dataset**

  With persist(), you can specify which storage level you want for both **RDD** and **Dataset**.

2. 4399的校招笔试题：海量日志数据，找出最近一周登录游戏最多的ip，讲一下解决方案。大佬们谈谈想法？

   经典TOPN问题， 1 日志按天分区; 2 刷选7天内的数据; 3 根据ip计数和group by; 4 同一分区里排序，取最大那条; 5 综合所有分区排序取第最大的那条。








## 参考

翻一下书好了，看来看去又看回最基础的，最基础的应该联想的起来啊，还是在山脚下徘徊，没有长进。