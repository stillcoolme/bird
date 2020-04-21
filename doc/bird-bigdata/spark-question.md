为什么相同 key 的会在同一分区，因为都是用的一样的分区函数啊！

## 浪尖大纲

广播变量的原理及演变过程，使用场景，使用广播变量一定划算吗？大变量咋办呢？

累加器的原理及应用场景，累加器使用有陷阱么？

序列化，反序列化，闭包，垃圾回收机制（过期rdd的回收，cache的回收等）。

checkpoint如何在spark core应用呢？何种场景适合？源码系列教程。

并行度相关配置，这个星球里也反复强调了，合理设置可以大幅度提高性能。

3. spark streaming

基于receiver和direct api两种模式的原理，最好读懂源码。主要是跟Kafka 结合的两种模式的区别。

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

https://cloud.tencent.com/developer/news/310841

4.spark sql

在数仓的领域，实时处理都用它，而且structured streaming也逐步依赖于sql引擎了。常见算子的使用及理解，并行度问题，大小表join，如何广播小表。

join，group by等数据倾斜如何发现及处理方法。

常见的存储格式，parquet，txt，json，orc对比及对性能的影响。

调优大部分也是针对并行度，文件大小，数据倾斜，task倾斜，内存和cpu合理设置等。



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

RDD是用来表示数据结构，不是用来存储数据。你要清楚，就是元数据。。

1. A list of partitions   一系列的分片

RDD是一个由多个partition（某个节点里的某一片连续的数据）组成的的list；分区可以增加计算的并行度。

数据源处分区数取决于数据源类型，1. kafkardd分片取决于kafka分区数，2. 文件类型的取决于文件大小文件数，是否压缩；Shuffle处分区数取决于Shuffle分区器，数据倾斜分两种：1 分区数据不均匀，2 key不均匀；

2. A list of dependencies on other RDDs   一系列的父rdd

RDD会记录它的依赖 ，为了容错，故障恢复（重算，cache，checkpoint），也就是说在内存中的RDD操作时出错或丢失会进行重算。

弊端是加入多个子rdd都依赖同一个父rdd，那么都要重新计算父rdd。 

打断血缘关系：1. checkpiont，失效不可恢复；2. cache，缓存失效可以利用血缘恢复。

3. A function for computing each split， 每个分片一个计算函数

RDD的每个partition上面都会有function，也就是函数应用，其作用是实现**同一个stage内部的RDD partition 之间以 pipeline 的形式运行转换**，避免了数据落盘 和 线程切换。（pipeline 这概念我觉得和rdd一样重要）

注意 mapPartition 和 map， foreachPartition 和 foreach 的区别！

4. Optionally, a Partitioner for Key-value RDDs        可选的针对key value型 rdd 的 分区器算法

 可选项，如果RDD里面存的数据是key-value形式，则可以传递一个自定义的Partitioner进行重新分区，例如这里自定义的Partitioner是基于key进行分区，那则会将不同RDD里面的相同key的数据放到同一个partition里面。

在某些特定场合需要我们自定义分区器，比如解决key倾斜的二次聚合。

5. Optionally, a list of preferred locations to compute each split on   可选的数据本地化策略。

最优的位置去计算，也就是数据的本地性。   

为rdd指定数据本地性：process，node，rack，any，no。数据本地可能会造成 task 任务倾斜于某个executor，调优要注意

### 望文生义

**1.1 Resilient**

Spark的核心数据结构有弹性能复原。说明spark在设计之初就考虑把spark应用在大规模的分布式集群中，任何一台正在计算Task的服务器都可能出故障，那么这个子任务自然在这台服务器无法继续执行，这时RDD所具有的“弹性”就派上了用场，它**可以使这个失败的子任务在集群内进行迁移，从而保证整体任务(Job)对故障机器的平滑过渡。**

可能有些同学有疑问了，难道还有系统不具有弹性，是硬邦邦的？还真有，比如很多的即席查询系统，例如presto或者impala，因为**在其上运行的查询，都是秒级的时延，所以如果子任务失败，直接把查询重跑一遍即可。**而spark处理的任务，可能时常要运行分钟级甚至小时级别，那么整个任务完全重跑的代价非常大，而某些task重跑的代价就比较小了，所以spark的数据结构一定要有“弹性”，能自动容错，保证任务只跑一遍。

**1.2 Distributed**

spark的数据结构怎么个分布式法呢？这就涉及到了spark中**分区(partition)的概念**，也就是数据的切分规则，根据一些特定的规则切分后的数据子集，就**可以在独立的task中进行处理**，而这些task又是分散在集群多个服务器上**并行的同时的执行**，这就是spark中Distributed的含义。

spark源码中RDD是个表示数据的基类，在这个基类之上衍生了很多的子RDD，不同的子RDD具有不同的功能，但是他们都要具备的能力就是能够被切分(partition)，比如从HDFS读取数据，那么会有hadoopRDD，这个hadoopRDD的切分规则就是如果一个HDFS文件可按照block(64M或者128M)进行切分，例如txt格式，那么一个Block一个partition，spark会为这个Block生成一个task去处理这个Block的数据，而如果HDFS上的文件不可切分，比如压缩的zip或者gzip格式，那么一个文件对应一个partition。

例如，进行分发(shuffle)，要把相同key的数据“归类”到一起，如果把所有key放到同一个partition里，那么就只能有一个task来进行归类处理，性能会很差，所以这个过程的并行化，就是靠把key进行切分，不同的key在不同的partition中被处理，来实现的group过程并行化。

**1.3 Datasets**

看到这个词，很多人会错误的以为RDD是spark的数据存储结构，其实并非如此，RDD中的Datasets并非真正的“集合”，而是**表示spark中数据处理的逻辑。**    

怎么理解呢？这需要结合两个概念来理解，

第一是spark中RDD 的transform操作，

另一个是spark中的pipeline。

首先看RDD的transform，来看论文中的一个transform图每个长方形都是一个RDD，但是他们**表示的数据结构不同**，注意，这里用的是”表示“，而不是”存储“，例如lines这个RDD，就是最原始的文本行，而errors这个RDD，则只表示以”ERROR“开头的文本行，而HDFSerrors这个RDD则表示包含了”HDFS“关键字的文本行。这就是一个RDD的”变形“过程。

我们回到上边纠结的”表示“和”存储“两个字眼上，看看用不同的字眼表达会有什么不同的结果。如果我们用”存储“，那么上一个RDD经过transform后，需要存储下来，等到全部处理完之后交给下一个处理逻辑(类似我们很久以前用迅雷下载电影，要先下载才能观看，两个过程是串行的)。那么问题来了，在一批数据达到之前，下一个处理逻辑必须要等待，这其实是没有必要的。**所以在上一个处理逻辑处理完一条数据后，如果立马交给下一个处理逻辑，这样就没有等待的过程，整体系统性能会有极大的提升**，而这正是用”表示“这个词来表达的效果(类似后来的流媒体，不需要先下载电影，可以边下载边观看)，这也就是是spark中的pipeline(流水线）处理方式。

**2 spark的lineage**

RDD的三个单词分析完了，球友们可能也有一个疑问，那就是对于pipeline的处理方式，感觉各个处理逻辑的数据都是”悬在空中“，没有落磁盘那么踏实。确实，如果是这种方式的话，spark怎么来保证这种”悬在空中“的流式数据在服务器故障后，能做到”可恢复“呢？这就引出了spark中另外一个重要的概念：lineage(血统)。一个RDD的血统，就是如上图那样的一系列处理逻辑，spark会为每个RDD记录其血统，借用范伟的经典小品的桥段，spark知道每个RDD的子集是”怎么没的“（变形变没的）以及这个子集是 ”怎么来的“（变形变来的），那么当数据子集丢失后，spark就会根据lineage，复原出这个丢失的数据子集，从而保证Datasets的弹性。

**3 注意**

1) 当然如果RDD被cache和做了checkpoint可以理解为spark把一个RDD的数据“存储了下来”，属于后续优化要讲解的内容。

2) RDD在transform时，并非每处理一条就交给下一个RDD，而是使用小批量的方式传递，也属于优化的内容，后续讲解。





### RDD、DataFrame和DataSet 区别

面试题啊，三者的不同？三者都没有存储真实数据，真实数据存储在 block 里面。

三者共性：

1. 基础都是rdd，都有rdd五大特性，都有惰性机制；
2. DataFrame和Dataset均可使用模式匹配获取各个字段的值和类型，DataFrame 是 case Row(col1:String, col2:String)

不同点：

1. 元数据存储

   1. RDD[Person]是以Person为类型参数，但是Person类的内部结构对于RDD而言却是不可知的。
   2. DataFrame 则是Row对象的集合，提供了详细的结构信息Schema，但每行Row要通过解析才能获取各个字段的值，每行的值无法直接访问。
   3. Dataset强类型，在需要访问列中的某个字段时是非常方便的。

   ```scala
   testDF.foreach{
     line =>
       val col1 = line.getAs[String]("col1")
       val col2 = line.getAs[String]("col2")
   }
   
   val testDS: Dataset[Coltest]=rdd.map{line=>
         Coltest(line._1,line._2)
       }.toDS
   testDS.map{
         line=>
           println(line.col1)
           println(line.col2)
   }
   
   DataSet 可以这样取数据   _._1
   DataFrame 则要这样取数据    _.getString(0)
   ```

   

**RDD**

* RDD五大特性；
* **类型安全，面向对象编程风格，序列化以及反序列化开销大。**

**DataFrame**

DataFrame的数据集都是按指定列存储，即结构化数据。类似于传统数据库中的表。

**DataSet**

在Spark 2.x中，Dataset具有两个完全不同的API特征：强类型API和弱类型API。

* DataFrame是特殊的Dataset，其每行是一个弱类型JVM object，只要指定字段名。Dataset是强类型JVM object的集合，封装Scala的case class或者Java class，指定 字段名 和 类型。
* 数据以编码的二进制形式存储，将对象的schema映射为SparkSQL类型，不需要反序列化就可以进行shuffle等操作，每条记录存储的则是强类型值。

dataframe  和  dataset 相同点

- 都是基于RDD的，所以都有RDD的特性，如懒加载，分布式，不可修改，分区等等。但执行sql性能比RDD高，因为spark自动会使用优化策略执行。
- 均支持 sparksql 的操作，还能注册临时表，进行sql语句操作
- `DataFrame`和`Dataset`均可使用模式匹配获取各个字段的值和类型
- `DataFrame`也叫`Dataset[Row]`，每一行的类型是Row

dataframe  和  dataset 不同点

因为`DataFrame`也叫`Dataset[Row]`，所以我们理解了**Row**和**普通对象**的区别就好办了

- **Row**的数据结构类似一个**数组**，只有顺序，切记。**普通对象**的数据结构也就是对象。
- 因此，访问**Row**只能通过如：`getInt(i: Int)`解析数据 或者 通过模式匹配得到数据；而**普通对象**可以通过 `.`号 直接访问对象中成员变量。
- 同理，**Row**中数据没类型，没办法在编译的时候检查是否有类型错误（弱类型的概念）；相反**普通对象**可以（强类型）。




### 三者转换

![](https://raw.githubusercontent.com/stillcoolme/mypic/master/2020/202003/df-convert.png)

![dataframe2dataset.png](https://i.loli.net/2020/01/16/PkpdJSz4EYGv9sR.png)

**注意**

- 原始RDD类型是 `RDD[(Int, String)]`
- DataFrame -> RDD 时，变成了`RDD[Row]`
- DataSet -> RDD时，变成了`RDD[User]`
- DataSet<Row> -> RDD时，变成`RDD[Row]`

```scala
// RDD转DataFrame，用元组把一行的数据写在一起，然后在toDF中指定字段名
import spark.implicits._
val testDF = rdd.map {line=>
      (line._1,line._2)
    }.toDF("col1","col2")

// RDD转Dataset：
import spark.implicits._
case class Coltest(col1:String,col2:Int)extends Serializable //定义字段名和类型
val testDS = rdd.map {line=>
   Coltest(line._1,line._2)
}.toDS

// DataFrame 转  Dataset
df.as(Encoders.String())
df.as(Encoders.bean())
```





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



### 窄依赖宽依赖

依据 Shuffle 为界来进行 Stage 的划分，也是 宽依赖窄依赖的划分。

**1. Shuffle**: Spark job 中 shuffle个数决定着stage个数。

**2. 分区**: Spark 算子中 RDD的分区数 决定着 stage任务的并行度。

Spark数据分区（partitionBy分区、partitioner获取分区方式、自定义分区）

https://blog.csdn.net/u012369535/article/details/90114003

将 partitionBy 讲的很好。。。

**3.** **分区传递**: 假设 rdd 有6个分区，map到fliter的分区数不变，filter到redcuebykey分区就变了，reducebykey的分区有个默认计算公式，假设我们在使用reducebykey的时候传入了一个分区数12。reducebykey后面的map就是12。map这类转换完全继承了父RDD的分区器和分区数，默认无法人为设置并行度，只有在shuffle的时候，我们才可以传入并行度。

```java
// 根据父rdd的 partitioner 来
override val partitioner = if (preservesPartitioning) firstParent[T].partitioner else None

override def getPartitions: Array[Partition] = firstParent[T].partitions
```

对于由窄依赖变换（例如map和filter）返回的RDD，会延续父RDD的分区信息，以pipeline的形式计算。每个对象仅依赖于父RDD中的单个对象。

> 两种算子

Transformation

1. 针对已有的RDD创建一个新的RDD（RDD是只读不可变的）
2. lazy特性。没有Action的话，不会触发spark程序的执行的，它们只是记录了元数据信息，对RDD所做的操作。Spark通过这种lazy特性，**来进行底层的spark应用执行的优化，避免产生过多中间结果。**

Action

1. 对RDD进行最后的操作，比如遍历、reduce、保存到文件等，并可以返回结果给Driver程序。
2. action操作执行，会触发一个spark job的运行，让最开始的Transformation读取数据之类的开始执行。



### DAG的生成，划分，task的调度执行

划分成一个Stage (pipeline流水线，每个task计算一个分区）



## Shuffle

读过源码？可以说说 SparkShuffle 啊。

**在Spark中Task的类型分为2种：ShuffleMapTask和ResultTask。**

**ShuffleMapTask的输出是shuffle所需数据**，ResultTask的输出是最后结果result。





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

spark上yarn有两种管理模式，**YARN-Client**和**YARN-Cluster**。

主要区别是：SparkContext初始化位置不同，也就是了Driver所在位置的不同。

|           client           |       master        |
| :------------------------: | :-----------------: |
|      driver在Client上      | driver在AppMaster上 |
| 日志可以直接在Client上看到 |  日志在某个节点上   |
|     Client连接不能断开     | Client连接可以断开  |
|       适合交互和调试       |    适合生产环境     |





## 内存分配相关

spark的大部分内存分为**执行内存和存储内存**。他们共享一个存储空间M。同时，存储内存在执行内存空闲的时候可以占用执行内存空间，执行内存也可以在存储内存大于一个阈值R的时候占用存储内存。R代表缓存不可被清除的最小存储内存。 

相关参数：

*  spark.memory.fraction = 0.6      设置 存储内存 和 执行内存 占用堆内存的比例， 若值越低，则 存储内存越低，则发生spill和evict的频率就越高。 
* spark.memory.storageFraction = 0.5   分配 存储内存，比如  `spark.memory.offHeap.size` 分配了 1G，然后 offheap 的 500M 就会用于缓存；



spark官网说了这么段话：在gc的统计信息中，如果老年代接近满了，减少用于缓存的内存(通过减小spark.memory.Fraction)。缓存较少的对象比降低运行速度对我们来说更有好处。
另外，可以考虑减少年轻代。可以通过减小-Xmn参数设置的值，假如使用的话。

为啥不直接设置：spark.memory.storageFraction

 假如我们单纯的减少 spark.memory.storageFraction 是行不通的，因为**存储内存可以占用执行内存进行缓存**，缓解不了老年代被吃满的状况，所以只能调整spark.memory.fraction。 

spark最骚的操作是，没有加内存解决不了的问题，假如有那是没加够。



## 面试具体问题

1. RDD种类，宽依赖窄依赖的区别，依据什么划分的？（shuffle）

2. 说说shuffle的过程

3. rdd，dataframe 和 dataset 的区别

4. cache 和 persist 区别

   dataset 调用 cache() ，缓存级别为仅在内存中，实际上调用了无参数的persist方法。
   
   而 persisit() 可以指定 缓存级别。
   
   项目中调用的是缓存表，spark2.+采用：
   
   sparkserssion.catalog.cacheTable("tableName")缓存表，
   sparkserssion.catalog.uncacheTable("tableName")解除缓存。
   
   
   
   StorageLevel 这个类里面设置了RDD的各种缓存级别，总共有12种，其实是它的多个构造参数的组合形成的，先看一下它的相关构造参数，源码如下：
   
   ```java
   class StorageLevel private(
       private var _useDisk: Boolean,    //是否使用磁盘
       private var _useMemory: Boolean,  //是否使用内存
       private var _useOffHeap: Boolean, //是否使用堆外内存
       private var _deserialized: Boolean, //是否反序列化
       private var _replication: Int = 1)  //备份因子，默认为1
     extends Externalizable {
   ```
   
   指定    val OFF_HEAP = new StorageLevel(true, true, true, false, 1) 
   
   
   
2. 4399的校招笔试题：海量日志数据，找出最近一周登录游戏最多的ip，讲一下解决方案。大佬们谈谈想法？

   经典TOPN问题， 1 日志按天分区; 2 刷选7天内的数据; 3 根据ip计数和group by; 4 同一分区里排序，取最大那条; 5 综合所有分区排序取第最大的那条。








## 参考

翻一下书好了，看来看去又看回最基础的，最基础的应该联想的起来啊，还是在山脚下徘徊，没有长进。