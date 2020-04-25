这些调优经验在美团2016年的文章就说的和清楚了！！

[Spark性能优化指南——基础篇](https://tech.meituan.com/2016/04/29/spark-tuning-basic.html)

[Spark性能优化指南——高级篇](https://tech.meituan.com/2016/05/12/spark-tuning-pro.html)



调优前首先要对spark的作业流程清楚：

* Driver到Executor的结构；
```
Master: Driver
    |-- Worker: Executor
            |-- job
                 |-- stage
                       |-- Task Task 
```
* 一个Stage内，**最终的RDD有多少个partition，就会产生多少个task**，一个task处理一个partition的数据；
* 作业划分为task分到Executor上，然后一个cpu core执行一个task；
* BlockManager负责Executor，task的数据管理，task来它这里拿数据；



## 开发调优

### RDD相关

* 避免创建重复的RDD
* 尽可能复用同一个RDD
* 对多次使用的RDD进行持久化
* 尽量避免使用shuffle类算子，例如 rdd1.map(Broadcast数据) 进行join
* 如果一定要 shuffle，使用 map-side 预聚合的shuffle操作
* 使用Kryo优化序列化性能
* 优化数据结构， 尽量不要使用上述三种数据结构 ：对象，字符串，集合类；其实很难做到；



## 资源分配调优

性能调优的王道：分配更多资源。
* 分配哪些资源？ executor、cpu per executor、memory per executor、driver memory、并行度。
* 为什么调了就能更好？提高并行执行能力，增加内存减少cache、shuffle 溢写，减少GC
```
/usr/local/spark/bin/spark-submit \
--class cn.spark.sparktest.core.WordCountCluster \
--driver-memory 1000m \     #driver的内存，影响不大，只要不出driver oom
--num-executors 3 \         #executor的数量
--executor-memory 100m \    #每个executor的内存大小
--executor-cores 3 \        #每个executor的cpu core数量
/usr/local/SparkTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar \

--conf spark.default.parallelism=1000 \   # 设置每个stage的并行度，默认task数量，线程数
--conf spark.storage.memoryFraction=0.5 \  # RDD持久化数据在Executor内存中能占的比例
--conf spark.shuffle.memoryFraction=0.3 \  # 进行聚合操作使用的Executor内存的比例，默认0.2
```





### 如何调节资源分配

* 第一种，Spark Standalone 模式下资源分配。

假如说一共20台机器，每台机器能使用4G内存，2个cpu core。
```
executor = 20，对应worker节点数量；
executor-memory = 4G，对应每个worker能用的最大内存；
executor-cores = 2，对应每个worker给每个executor能用的最多个cpu core。
```

* 第二种，Yarn模式资源队列下资源调度。

应该去查看spark作业，要提交到的资源队列，大概有多少资源？
一个原则，你能使用的资源有多大，就尽量去调节到最大的大小（executor的数量，几十个到上百个不等；

### 提高并行度

**2. 如何设置一个Spark Application的并行度？**

**spark.defalut.parallelism默认是没有值的，如果设置了值比如说10，是在shuffle的过程才会起作用**（val rdd2 = rdd1.reduceByKey(_+_) //rdd2的分区数就是10，rdd1的分区数不受这个参数的影响）

**new SparkConf().set(“spark.defalut.parallelism”,”“500)**

 **3、如果读取的数据在HDFS上，增加block数**，默认情况下split与block是一对一的，而split又与RDD中的partition对应，所以增加了block数，也就提高了并行度。

**4、RDD.repartition**，给RDD重新设置partition的数量

**5、reduceByKey的算子指定partition的数量**

​         val rdd2 = rdd1.reduceByKey(_+_,10)  val rdd3 = rdd2.map.filter.reduceByKey(_+_)

 **6、**val rdd3 = rdd1.**join**（rdd2）**rdd3里面partiiton的数量是由父RDD中最多的partition数量来决定，因此使用join算子的时候，增加父RDD中partition的数量。**




## 本地化等待时间时长

所谓任务跟着数据跑，Driver  的 TaskScheduler  对每一个stage的task进行分配之前，首先应该拿到 rdd1 数据所在的位置（rdd1 封装了这个文件所对应的 block 的位置），

DAGScheduler 通过调用  getPreferrdeLocations() 拿到 partition 所对应的数据的位置，TaskScheduler 根据这些位置来发送相应的 task。

TaskScheduler 接收到了 TaskSet 后，TaskSchedulerImpl 会为每个 TaskSet 创建一个 TaskSetManager 对象，该对象包含taskSet 所有 tasks，并管理这些 tasks 的执行，其中就包括计算 TaskSetManager 中的 tasks 都有哪些 locality levels，以便在调度和延迟调度 tasks 时发挥作用。

总的来说，Spark 中的数据本地化是由 DAGScheduler 和 TaskScheduler 共同负责的。

===============

分配算法会希望每个task正好分配到它要计算的数据所在的节点。

但是可能某task要分配过去的那个节点的计算资源和计算能力都满了，Spark会等待一段时间，默认情况下是3s。
超过时间了会选择一个比较差的本地化级别，将task分配到离要计算的数据所在节点比较近的一个节点，然后进行计算。

task会通过其所在节点的BlockManager来获取数据，BlockManager发现自己本地没有数据，
会通过一个getRemote()方法，通过TransferService（网络数据传输组件）从数据所在节点的BlockManager中，获取数据，通过网络传输回task所在节点。

所以我们可以调节等待时长就是让spark再等待多一下，不要到低一级的本地化级别。
```
spark.locality.wait.process
spark.locality.wait.node
spark.locality.wait.rack

new SparkConf()
  .set("spark.locality.wait", "10")
```

观察日志，spark作业的运行日志显示，观察大部分task的数据本地化级别。
如果是发现，好多的级别都是NODE_LOCAL、ANY，那么最好就去调节一下数据本地化的等待时长。
看看大部分的task的本地化级别有没有提升，spark作业的运行时间有没有缩短。

PROCESS_LOCAL 进程本地化，表示 task 要计算的数据在同一个 Executor 中。

NODE_LOCAL 节点本地化，速度稍慢，因为数据需要在不同的进程之间传递或从文件中读取。分为两种情况，第一种：task 要计算的数据是在同一个 worker 的不同 Executor 进程中。第二种：task 要计算的数据是在同一个 worker 的磁盘上，或在 HDFS 上恰好有 block 在同一个节点上。如果 Spark 要计算的数据来源于 HDFSD 上，那么最好的本地化级别就是 NODE_LOCAL。

NO_PREF 没有最佳位置，数据从哪访问都一样快，不需要位置优先。比如 Spark SQL 从 Mysql 中读取数据。






## 1.5 JVM调优

### 首先估计GC的影响

GC调优的第一步就是去统计GC发生的频率和GC消耗时间。
通过添加：
```
./bin/spark-submit \
--name "My app" \
--master local[4] \
--conf spark.eventLog.enabled=false \
--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps" \
myApp.jar
```
在作业运行的时候能够看到worker的日志里面在每次GC的时候就打印出GC信息。

**使用jvisualvm来监控Spark应用程序**

可以看到Spark应用程序堆，线程的使用情况，看不到GC回收的次数时间什么的，从而根据这些数据去优化您的程序。
* 在$SPARK_HOME/conf目录下配置spark-default.conf文件，加入如下配置：
   ```
   spark.driver.extraJavaOptions   -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
   ```
   
* 启动Spark应用程序

* 打开jvisualvm.exe监控
    在JDK安装的bin目录下有个jvisualvm.exe，进行配置，依次选择 文件-> 添加JMX连接，在连接文本框填上Driver机器的地址和端口




### 了解GC相关原理
* GC调优的目标
    1. 保证在Old generation只存储有长期存活的RDD；
    2. Young generation有足够的空间存储短期的对象，**避免full GC将作业执行时创建的短期的对象也回收掉**。
    3. 避免Young generation频繁minor GC

* GC调优做法：
    1. 通过收集GC状态检查是否有大量的垃圾回收，如果在作业完成前有多次 full GC，意味着没有足够的内存给执行的task。
    2. 如果进行了多次 minorGC，分配更多的内存给Eden也许会有帮助。只要将Eden的内存E设置成大于每个task需要的内存大小，Young generation 则设置成 -Xmn=4 / 3 * E。
    3. 如果OldGen将要完全占满，可以减少spark.memory.fraction。改变JVM的NewRatio参数，默认设置是2，表示Old generation占用了 2/3的 heap内存，应该设置得足够大超过并超过spark.memory.fraction。另外可以考虑减少Young generation的大小通过调低 -Xmn。
    4. 尝试使用G1GC 收集器。如果executor需要大的堆内存，那么通过配置-XX:G1HeapRegionSize来提高G1 region size 是很重要的。
    5. 如果作业从HDFS中读取数据，可通过作业使用的block大小推测使用的内存大小，读取出来的block通常是存储大小的2-3倍。如果有4个作业去使用一个HDFS的128MB的block，我们预估Eden需要4 * 3 * 128MB。

spark-submit脚本里面，去用--conf的方式，去添加配置：
```
--conf spark.memory.fraction，0.6 -> 0.5 -> 0.4 -> 0.2  # exeuctor占用的堆内存
--conf spark.shuffle.memoryFraction=0.3
```



### 调节 executor 堆外内存

有时候，如果你的spark作业处理的数据量特别特别大；然后spark作业一运行，时不时的报错，shuffle file cannot find，executor、task lost，out of memory（内存溢出）；

可能是说executor的堆外内存不太够用，导致executor在运行的过程中会内存溢出；

然后导致后续的stage的task在运行的时候，可能要从一些executor中去拉取shuffle map output文件，但是executor可能已经挂掉了，关联的Block manager也没有了；spark作业彻底崩溃。

上述情况下，就可以去考虑调节一下executor的堆外内存。避免掉某些JVM OOM的异常问题。
此外，堆外内存调节的比较大的时候，对于性能来说，也会带来一定的提升。


spark-submit脚本里面，去用--conf的方式，去添加基于yarn的提交模式配置；
```
--conf spark.yarn.executor.memoryOverhead=2048
```
默认情况下，这个堆外内存上限大概是300多M；真正处理大数据的时候，这里都会出现问题，导致spark作业反复崩溃，无法运行；此时就会去调节这个参数到至少1G（1024M），甚至说2G、4G。



### 调节连接等待时长

我们知道 Executor，优先从自己本地关联的 BlockManager 中获取某份数据。
如果本地block manager没有的话，那么会通过TransferService，去远程连接其他节点上executor的block manager去获取。
正好碰到那个 exeuctor 的 JVM 在垃圾回收，就会没有响应，无法建立网络连接；
spark默认的网络连接的超时时长，是60s；

就会出现某某file。一串file  id。uuid（dsfsfd-2342vs--sdf--sdfsd）。not found。file lost。
报错几次，几次都拉取不到数据的话，可能会导致spark作业的崩溃。

也可能会导致DAGScheduler，反复提交几次stage。TaskScheduler，反复提交几次task。大大延长我们的spark作业的运行时间。

可以考虑在spark-submit脚本里面，调节连接的超时时长：
```
--conf spark.core.connection.ack.wait.timeout=300
```



## OOM相关

Spark中的OOM问题不外乎两种情况：
* map执行中产生了大量的对象导致内存溢出；针对这种问题，**在不增加内存的情况下，可以通过减少每个Task的大小，让Executor的内存也能够装得下。**具体做法可以在会产生大量对象的map操作之前调用repartition方法，分区成更小的块传入map。
* shuffle后内存溢出，包括join，reduceByKey，repartition等操作。

可以先理一下Spark内存模型，再总结各种OOM的情况相对应的解决办法和性能优化方面的总结。

* 数据不平衡导致内存溢出

也可如上面进行repartition

* coalesce减少分区导致内存溢出

  例子：Spark计算后如果产生的文件太小，会调用coalesce合并文件再存入hdfs中。这会导致一个问题：有100个文件，现在调用coalesce(10)，意味着能够有100个Task，最后只产生10个文件。
  **因为coalesce并不是shuffle操作**，意味着从头到位只有10个Task在执行，每个Task同时一次读取10个文件，使用的内存是原来的10倍，这导致了OOM。

  

  可以通过repartition解决，调用repartition(10)，
  因为这就有一个shuffle的过程，shuffle前后是两个Stage，一个100个分区，一个是10个分区，就能按照我们的想法执行。

* shuffle后内存溢出：

shuffle内存溢出的情况可以说都是shuffle后，单个文件过大导致的。在shuffle的使用，需要传入一个partitioner，默认的partitioner都是HashPatitioner，默认值是父RDD中最大的分区数，这个参数通过spark.default.parallelism控制 (只对HashPartitioner有效，在spark-sql中用spark.sql.shuffle.partitions) 。如果是别的partitioner导致的shuffle内存溢出，就需要从partitioner的代码增加partitions的数量。



## Suffle调优

大多数Spark作业的性能主要就是消耗在了shuffle环节，因为该环节包含了大量的磁盘IO、序列化、网络数据传输等操作。


### 调节map端内存缓冲与reduce端内存占比


shuffle的map task：
输出到磁盘文件的时候，统一都会先写入每个task自己关联的一个内存缓冲区。
这个缓冲区大小，默认是32kb。当**内存缓冲区满溢之后，会进行spill溢写操作到磁盘文件中去**。数据量比较大的情况下可能导致多次溢写。

shuffle的reduce端task：
在拉取到数据之后，会用hashmap的数据格式，来对各个key对应的values进行汇聚的时候。
使用的就是自己对应的executor的内存，executor（jvm进程，堆），默认executor内存中划分给reduce task进行聚合的比例，是0.2。
要是拉取过来的数据很多，那么在内存中，放不下；就将在内存放不下的数据，都spill（溢写）到磁盘文件中去。
数据大的时候磁盘上溢写的数据量越大，后面在进行聚合操作的时候，很可能会多次读取磁盘中的数据，进行聚合。



## shuffle相关参数调优

以下是Shffule过程中的一些主要参数，这里详细讲解了各个参数的功能、默认值以及基于实践经验给出的调优建议。

### spark.shuffle.file.buffer

- 默认值：32k
- 参数说明：该参数用于设置shuffle write task的BufferedOutputStream的buffer缓冲大小。将数据写到磁盘文件之前，会先写入buffer缓冲中，待缓冲写满之后，才会溢写到磁盘。
- 调优建议：如果作业可用的内存资源较为充足的话，可以适当增加这个参数的大小（比如64k），从而减少shuffle write过程中溢写磁盘文件的次数，也就可以减少磁盘IO次数，进而提升性能。在实践中发现，合理调节该参数，性能会有1%~5%的提升。

### spark.reducer.maxSizeInFlight

- 默认值：48m
- 参数说明：该参数用于设置shuffle read task的buffer缓冲大小，而这个buffer缓冲决定了每次能够拉取多少数据。
- 调优建议：如果作业可用的内存资源较为充足的话，可以适当增加这个参数的大小（比如96m），从而减少拉取数据的次数，也就可以减少网络传输的次数，进而提升性能。在实践中发现，合理调节该参数，性能会有1%~5%的提升。

### spark.shuffle.io.maxRetries

- 默认值：3
- 参数说明：shuffle read task从shuffle write task所在节点拉取属于自己的数据时，如果因为网络异常导致拉取失败，是会自动进行重试的。该参数就代表了可以重试的最大次数。如果在指定次数之内拉取还是没有成功，就可能会导致作业执行失败。
- 调优建议：对于那些包含了特别耗时的shuffle操作的作业，建议增加重试最大次数（比如60次），以避免由于JVM的full gc或者网络不稳定等因素导致的数据拉取失败。在实践中发现，对于针对超大数据量（数十亿~上百亿）的shuffle过程，调节该参数可以大幅度提升稳定性。

### spark.shuffle.io.retryWait

- 默认值：5s
- 参数说明：具体解释同上，该参数代表了每次重试拉取数据的等待间隔，默认是5s。
- 调优建议：建议加大间隔时长（比如60s），以增加shuffle操作的稳定性。



**怎么调节？**
看Spark UI，shuffle的磁盘读写的数据量很大，就意味着最好调节一些shuffle的参数。进行调优。

```
set(spark.shuffle.file.buffer，64)      // 默认32k  每次扩大一倍，看看效果。
set(spark.shuffle.memoryFraction，0.2)  // 每次提高0.1，看看效果。
```

很多资料都会说这两个参数，是调节shuffle性能的不二选择，实际上不是这样的。
以实际的生产经验来说，这两个参数没有那么重要，shuffle的性能不是因为这方面的原因导致的。



## 数据倾斜调优

主要参考美团文章！！！

数据倾斜只会发生在shuffle过程中。shuffle操作是按照key，来进行values的数据的输出、拉取和聚合的。所以**同一个key的values，一定是分配到一个reduce task进行处理的**。当其中一个key的数据量很大就会发生数据倾斜。

触发shuffle操作的算子：distinct、 groupByKey、reduceByKey、aggregateByKey、join、cogroup、repartition等 的   key值 分布不均匀。

现象：1. 个别Task运行慢；2. 原本能够运行的作业OOM；

### 生产环境怎么定位

**#某个task执行特别慢的情况**

首先要看的，就是数据倾斜发生在第几个stage中，找到那个 shuffle 的 stage。

如果是用yarn-client模式提交，那么本地是直接可以看到 log 的，可以在 log 中找到当前运行到了第几个stage；如果是用yarn- cluster模式提交，则可以通过Spark Web UI来查看当前运行到了第几个stage。此外，无论是使用yarn-client模式还是yarn-cluster模式，我们都可以在Spark Web UI上深入看一下**当前这个stage各个task分配的数据量，从而进一步确定是不是task分配的数据不均匀导致了数据倾斜**。

比如下图中，倒数第三列显示了每个task的运行时间。明显可以看到，有的task运行特别 慢，需要几分钟才能运行完，此时单从运行时间上看就已经能够确定发生数据倾斜了。此外，倒数第一列显示了每个task处理的数据量，明显可以看到，运行时 间特别短的task只需要处理几百KB的数据即可，而运行时间特别长的task需要处理几千KB的数据，处理的数据量差了10倍。此时更加能够确定是发生 了数据倾斜。

![img](https:////upload-images.jianshu.io/upload_images/2160494-2950f0847eb168a8.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

知道数据倾斜发生在哪一个stage之后，接着我们就需要根据stage划分原理，推算出来发生倾斜的那个stage对应代码中的哪一部分，这部分 代码中肯定会有一个shuffle类算子。精准推算stage与代码的对应关系，需要对Spark的源码有深入的理解，这里我们可以介绍一个相对简单实用 的推算方法：

只要看到Spark代码中出现了一个shuffle类算子或者是Spark SQL 的 SQL语句中出现了会导致shuffle的语句（比如group by语句），那么就可以判定，以那个地方为界限划分出了前后两个stage。

然后我们就知道如何快速定位出发生数据倾斜的stage对应代码的哪一个部分了。比如我们在Spark Web UI或者本地log中发现，stage1的某几个task执行得特别慢，判定stage1出现了数据倾斜，那么就可以回到代码中定位出stage1主要包 括了reduceByKey这个shuffle类算子，此时基本就可以确定是由reduceByKey算子导致的数据倾斜问题。比如某个单词出现了100万 次，其他单词才出现10次，那么stage1的某个task就要处理100万数据，整个stage的速度就会被这个task拖慢。

**#某个task莫名其妙内存溢出的情况**

这种情况下去定位出问题的代码就比较容易了。我们建议直接看yarn-client模式下本地log的异常栈，或者是通过YARN查看yarn- cluster模式下的log中的异常栈。一般来说，通过异常栈信息就可以定位到你的代码中哪一行发生了内存溢出。然后在那行代码附近找找，一般也会有 shuffle类算子，此时很可能就是这个算子导致了数据倾斜。

但是大家要注意的是，不能单纯靠偶然的内存溢出就判定发生了数据倾斜。因为自己编写的代码的bug，以及偶然出现的数据异常，也可能会导致内存溢 出。因此还是要按照上面所讲的方法，通过Spark Web UI查看报错的**那个stage的各个task的运行时间以及分配的数据量**，才能确定是否是由于数据倾斜才导致了这次内存溢出。



**#此时根据你执行操作的情况不同，可以有很多种查看 倾斜key分布的方式：**

如果是Spark SQL中的group by、join语句导致的数据倾斜，那么就查询一下SQL中使用的表的key分布情况。

如果是对Spark RDD执行shuffle算子导致的数据倾斜，那么可以在Spark作业中加入查看key分布的代码，比如RDD.countByKey()。然后对统计 出来的各个key出现的次数，collect/take到客户端打印一下，就可以看到key的分布情况。

举例来说，我们可以先对倾斜RDD采样10%的样本数据，然后使用countByKey算子统计出每个key出现的次数，最后在客户端遍历和打印 样本数据中各个key的出现次数。

val sampledPairs = pairs.sample(false, 0.1)
val sampledWordCounts = sampledPairs.countByKey()
sampledWordCounts.foreach(println(_))



### 初步方案

1. 聚合数据源
2. 如果机器无限多，可以reptition的无限小
   谁oom都给谁repartition，repartition不行就count然后filter
   如果repartition不管用就是严重数据倾斜，那么首先count找到引发倾斜的key，然后看到底是什么原因引起的，然后单独处理；
3. 如果对计算指标没影响，就过滤少数导致倾斜的key。业务其实更多的是非法值，加key打散也是浪费资源，非法值就是裹一层Option，模式匹配一把梭。不能过滤的话该干啥干啥，网上搜一下很全了。

4. 聚合源数据。数据源按key来分组，将key对应的所有的values，全部用一种特殊的格式，拼接到一个字符串里面去，每个key就只对应一条数据。比如

```
key=sessionid, value: action_seq=1|user_id=1|search_keyword=火锅|category_id=001;action_seq=2|user_id=1|search_keyword=涮肉|category_id=001”。

然后在spark中，拿到key=sessionid，values<Iterable>。

```

 

Spark应用程序怎么监控？
1)、webUI：4040端口：包括任务调度状态，RDD大小和内存使用的统计信息；正在运行executor信息；
2)、辅助监控工具，如：ganglia；



### 提高shuffle read task并行度

 **方案适用场景：**如果我们必须要对数据倾斜迎难而上，那么建议优先使用这种方案，因为这是处理数据倾斜最简单的一种方案。 

**方案实现思路：**在对RDD执行shuffle算子时，给shuffle算子传入一个参数，比如reduceByKey(1000)，该参数就设置了这个shuffle算子执行时shuffle read task的数量。对于Spark SQL中的shuffle类语句，比如group by、join等，需要设置一个参数，即 spark.sql.shuffle.partitions，该参数代表了shuffle read task的并行度，该值默认是200，对于很多场景来说都有点过小。

**方案实现原理：**增加shuffle read task的数量，可以让原本分配给一个task的多个key分配给多个task，从而让每个task处理比原来更少的数据。举例来说，如果原本有5个key，每个key对应10条数据，这5个key都是分配给一个task的，那么这个task就要处理50条数据。而增加了shuffle read task以后，每个task就分配到一个key，即每个task就处理10条数据，那么自然每个task的执行时间都会变短了。

**方案优点：**实现起来比较简单，可以有效缓解和减轻数据倾斜的影响。

**方案缺点：**只是缓解了数据倾斜而已，没有彻底根除问题，根据实践经验来看，其效果有限。（对于某个key有超大数据量的情况无能为力）

**方案实践经验：**该方案通常无法彻底解决数据倾斜，因为如果出现一些极端情况，比如某个key对应的数据量有100万，那么无论你的task数量增加到多少，这个对应着100万数据的key肯定还是会分配到一个task中去处理，因此注定还是会发生数据倾斜的。所以这种方案只能说是在发现数据倾斜时尝试使用的第一种手段，尝试去用嘴简单的方法缓解数据倾斜而已，或者是和其他方案结合起来使用。



### 随机key实现二次聚合

**方案适用场景：**对RDD执行reduceByKey等聚合类shuffle算子或者在Spark SQL中使用group by语句进行分组聚合时，比较适用这种方案。

**方案实现思路：**这个方案的核心实现思路就是进行两阶段聚合。

1. 第一次是局部聚合，先给每个key都打上一个随机数，比如10以内的随机数，此时原先一样的key就变成不一样的了，比如(hello, 1) (hello, 1) (hello, 1) (hello, 1)，就会变成(1_hello, 1) (1_hello, 1) (2_hello, 1) (2_hello, 1)。
2. 接着对打上随机数后的数据，执行reduceByKey等聚合操作，进行局部聚合，就会变成了(1_hello, 2) (2_hello, 2)。
3. 然后将各个key的前缀给去掉，就会变成(hello,2)(hello,2)，
4. 再次进行全局聚合操作，就可以得到最终结果了，比如(hello, 4)。

**方案实现原理：**将原本相同的key通过附加随机前缀的方式，变成多个不同的key，就可以让原本被一个task处理的数据分散到多个task上去做局部聚合，进而解决单个task处理数据量过多的问题。接着去除掉随机前缀，再次进行全局聚合，就可以得到最终的结果。具体原理见下图。

**方案优点：**对于聚合类的shuffle操作导致的数据倾斜，效果是非常不错的。通常都可以解决掉数据倾斜，或者至少是大幅度缓解数据倾斜，将Spark作业的性能提升数倍以上。

**方案缺点：**仅仅适用于聚合类的shuffle操作，适用范围相对较窄。如果是join类的shuffle操作，还得用其他的解决方案。



### reduce join 转为 map join

将小的RDD全量广播，就可以map join，因为 map 是 窄依赖，所以无shuffle

只适用有小 RDD 的情况。



### 采样倾斜key并分拆join操作

**方案适用场景：**两个RDD/Hive表进行join的时候，如果数据量都比较大，无法采用“解决方案五”，那么此时可以看一下两个RDD/Hive表中的key分布情况。如果出现数据倾斜，是因为其中某一个RDD/Hive表中的少数几个key的数据量过大，而另一个RDD/Hive表中的所有key都分布比较均匀，那么采用这个解决方案是比较合适的。

**方案实现思路：** 

* 对包含少数几个数据量过大的key的那个RDD，通过sample算子采样出一份样本来，然后统计一下每个key的数量，计算出来数据量最大的是哪几个key。 
* 然后将这几个key对应的数据从原来的RDD中拆分出来，形成一个单独的RDD，并给每个key都打上n以内的随机数作为前缀，而不会导致倾斜的大部分key形成另外一个RDD。 
* 接着将需要join的另一个RDD，也过滤出来那几个倾斜key对应的数据并形成一个单独的RDD，**将每条数据膨胀成n条数据**，这n条数据都按顺序附加一个0~n的前缀，不会导致倾斜的大部分key也形成另外一个RDD。 
* 再将附加了随机前缀的独立RDD与另一个膨胀n倍的独立RDD进行join，此时就可以将原先相同的key打散成n份，分散到多个task中去进行join了。 
* 而另外两个普通的RDD就照常join即可。 
* 最后将两次join的结果使用union算子合并起来即可，就是最终的join结果。

**方案实现原理：**对于join导致的数据倾斜，如果只是某几个key导致了倾斜，可以将少数几个key分拆成独立RDD，并附加随机前缀打散成n份去进行join，此时这几个key对应的数据就不会集中在少数几个task上，而是分散到多个task进行join了。具体原理见下图。

**方案优点：**对于join导致的数据倾斜，如果只是**某几个key导致了倾斜**，采用该方式可以用最有效的方式打散key进行join。而且只需要针对少数倾斜key对应的数据进行扩容n倍，不需要对全量数据进行扩容。避免了占用过多内存。

**方案缺点：**如果导致倾斜的key特别多的话，比如成千上万个key都导致数据倾斜，那么这种方式也不适合。

![img](https://awps-assets.meituan.net/mit-x/blog-images-bundle-2016/61c7dabc.png)



### 使用随机前缀和扩容RDD进行join

**方案适用场景：**如果在进行join操作时，RDD中有大量的key导致数据倾斜，那么进行分拆key也没什么意义，此时就只能使用最后一种方案来解决问题了。

**方案实现思路：** 

* 该方案的实现思路基本和 上一方案 类似，首先查看RDD/Hive表中的数据分布情况，找到那个造成数据倾斜的RDD/Hive表，比如有多个key都对应了超过1万条数据。 
* 然后将该RDD的每条数据都打上一个n以内的随机前缀。 
* 同时对另外一个正常的RDD进行扩容，将每条数据都扩容成n条数据，扩容出来的每条数据都依次打上一个0~n的前缀。 
* 最后将两个处理后的RDD进行join即可。

**方案实现原理：**将原先一样的key通过附加随机前缀变成不一样的key，然后就可以将这些处理后的“不同key”分散到多个task中去处理，而不是让一个task处理大量的相同key。该方案与“解决方案六”的不同之处就在于，上一种方案是尽量只对少数倾斜key对应的数据进行特殊处理，由于处理过程需要扩容RDD，因此上一种方案扩容RDD后对内存的占用并不大；而这一种方案是针对有大量倾斜key的情况，没法将部分key拆分出来进行单独处理，因此只能对整个RDD进行数据扩容，对内存资源要求很高。

**方案优点：**对join类型的数据倾斜基本都可以处理，而且效果也相对比较显著，性能提升效果非常不错。

**方案缺点：**该方案更多的是缓解数据倾斜，而不是彻底避免数据倾斜。而且需要对整个RDD进行扩容，对内存资源要求很高。

**方案实践经验：**曾经开发一个数据需求的时候，发现一个join导致了数据倾斜。优化之前，作业的执行时间大约是60分钟左右；使用该方案优化之后，执行时间缩短到10分钟左右，性能提升了6倍。



### 多种方案组合使用

在实践中发现，很多情况下，如果只是处理较为简单的数据倾斜场景，那么使用上述方案中的某一种基本就可以解决。但是如果要处理一个较为复杂的数据倾斜场景，那么可能需要将多种方案组合起来使用。

比如说，我们针对出现了多个数据倾斜环节的Spark作业，

* 可以先运用解决方案一和二，预处理一部分数据，并过滤一部分数据来缓解；
* 其次可以对某些shuffle操作提升并行度，优化其性能；
* 最后还可以针对不同的聚合或join操作，选择一种方案来优化其性能。
* 大家需要对这些方案的思路和原理都透彻理解之后，在实践中根据各种不同的情况，灵活运用多种方案，来解决自己的数据倾斜问题。




## 1.7 算子调优

### 1.7.1 MapPartitions提升Map类操作性能

这里需要稍微讲一下RDD和DataFrame的区别。
RDD强调的是不可变对象，每个RDD都是不可变的，当调用RDD的map类型操作的时候，都是产生一个新的对象。
这就导致如果对一个RDD调用大量的map类型操作的话，每个map操作会产生一个到多个RDD对象，这虽然不一定会导致内存溢出，但是会产生大量的中间数据，增加了gc操作。

另外RDD在调用action操作的时候，会出发Stage的划分，但是在每个Stage内部可优化的部分是不会进行优化的，
例如rdd.map(_+1).map(_+1)，这个操作在数值型RDD中是等价于rdd.map(_+2)的，但是RDD内部不会对这个过程进行优化。

DataFrame则不同，DataFrame由于有类型信息所以是可变的，并且在可以使用sql的程序中，都会有一个sql优化器Catalyst。

上面说到的这些RDD的弊端，有一部分就可以使用mapPartitions进行优化，mapPartitions可以同时替代rdd.map, rdd.filter, rdd.flatMap的作用，所以在长操作中，可以在mapPartitons中将RDD大量的操作写在一起，避免产生大量的中间rdd对象，
另外是mapPartitions在一个partition中可以复用可变类型，这也能够避免频繁的创建新对象。

普通的mapToPair，当一个partition中有1万条数据，function要执行和计算1万次。

MapPartitions类操作，一个task仅仅会执行一次function，function一次接收一个 partition 的所有数据。只要执行一次就可以了，性能比较高。

但是有的时候，使用mapPartitions会出现OOM（内存溢出）的问题。因为单次函数调用就要处理掉一个partition所有的数据，如果内存不够，垃圾回收时是无法回收掉太多对象的，很可能出现OOM异常。所以使用这类操作时要慎重！

在项目中，自己先去估算一下RDD的数据量，以及每个partition的量，还有自己分配给每个executor的内存资源，看看一下子内存容纳所有的partition数据。


### 1.7.2 使用coalesce减少分区数量

从源码中可以看出 repartition()，其实就是调用了coalesce() 的 shuffle为 true 的情况. 

现在假设 RDD 有X个分区,需要重新划分成Y个分区.

1.如果x<y，说明x个分区里有数据分布不均匀的情况，利用HashPartitioner把x个分区重新划分成了y个分区，此时，需要把shuffle设置成true才行,因为如果设置成false,不会进行shuffle操作,此时父RDD和子RDD之间是窄依赖,这时并不会增加RDD的分区.

2.如果x>y，需要先把x分区中的某些个分区合并成一个新的分区，然后最终合并成y个分区,此时,需要把coalesce方法的shuffle设置成false.

总结：如果想要增加分区的时候，可以用repartition或者coalesce+true。但是一定要有shuffle操作,分区数量才会增加。



RDD这种filter之后，RDD中的每个partition的数据量，可能都不太一样了。
问题：
1、每个partition数据量变少了，但是在后面进行处理的时候，还跟partition数量一样数量的task，来进行处理；有点浪费task计算资源。
2、每个partition的数据量不一样，会导致后面的每个task处理每个partition的时候，每个task要处理的数据量就不同，处理速度相差大，导致数据倾斜。。。。

针对上述的两个问题，能够怎么解决呢？

1、针对第一个问题，希望可以进行partition的压缩吧，因为数据量变少了，那么partition其实也完全可以对应的变少。
比如原来是4个partition，现在完全可以变成2个partition。
那么就只要用后面的2个task来处理即可。就不会造成task计算资源的浪费。

2、针对第二个问题，其实解决方案跟第一个问题是一样的；也是去压缩partition，尽量让每个partition的数据量差不多。
那么这样的话，后面的task分配到的partition的数据量也就差不多。
不会造成有的task运行速度特别慢，有的task运行速度特别快。避免了数据倾斜的问题。

主要就是用于在filter操作之后，添加coalesce算子，针对每个partition的数据量各不相同的情况，来压缩partition的数量。
减少partition的数量，而且让每个partition的数据量都尽量均匀紧凑。


### 1.7.3 foreachPartition优化写数据库性能
默认的foreach的性能缺陷在哪里？
1. 对于每条数据，都要单独去调用一次function，task为每个数据都要去执行一次function函数。如果100万条数据，（一个partition），调用100万次。性能比较差。
2. 浪费数据库连接资源。

在生产环境中，都使用foreachPartition来写数据库
1、对于我们写的function函数，就调用一次，一次传入一个partition所有的数据，但是太多也容易OOM。
2、主要创建或者获取一个数据库连接就可以
3、只要向数据库发送一次SQL语句和多组参数即可

**local模式跑的时候foreachPartition批量入库会卡住**，可能资源不足，因为用standalone集群跑的时候不会出现。



### repartition 解决Spark SQL低并行度的性能问题

并行度：可以这样调节：
1、spark.default.parallelism   指定为全部executor的cpu core总数的2~3倍
2、textFile()，传入第二个参数，指定partition数量（比较少用）
**但是通过spark.default.parallelism参数指定的并行度，只会在没有Spark SQL的stage中生效**。
Spark SQL自己会默认根据hive表对应的hdfs文件的block，自动设置Spark SQL查询所在的那个stage的并行度。

比如第一个stage，用了Spark SQL从hive表中查询出了一些数据，然后做了一些transformation操作，接着做了一个shuffle操作（groupByKey），这些都不会应用指定的并行度可能会有非常复杂的业务逻辑和算法，就会导致第一个stage的速度，特别慢；下一个stage，在shuffle操作之后，做了一些transformation操作才会变成你自己设置的那个并行度。

解决上述Spark SQL无法设置并行度和task数量的办法，是什么呢？

repartition算子，可以将你用Spark SQL查询出来的RDD，使用repartition算子时，去重新进行分区，此时可以分区成多个partition，比如从20个partition分区成100个。
就可以避免跟Spark SQL绑定在一个stage中的算子，只能使用少量的task去处理大量数据以及复杂的算法逻辑。



###  reduceByKey本地聚合介绍

reduceByKey，相较于普通的shuffle操作（比如groupByKey），它有一个特点：底层基于CombineByKey，会进行map端的本地聚合。对map端给下个stage每个task创建的输出文件中，写数据之前，就会进行本地的combiner操作，也就是说对每一个key，对应的values，都会执行你的算子函数。

用reduceByKey对性能的提升：
1、在本地进行聚合以后，在map端的数据量就变少了，减少磁盘IO。而且可以减少磁盘空间的占用。
2、下一个stage，拉取数据的量，也就变少了。减少网络的数据传输的性能消耗。
3、在reduce端进行数据缓存的内存占用变少了，要进行聚合的数据量也变少了。

reduceByKey在什么情况下使用呢？
1、简单的wordcount程序。
2、对于一些类似于要对每个key进行一些字符串拼接的这种较为复杂的操作，可以自己衡量一下，其实有时，也是可以使用reduceByKey来实现的。
但是不太好实现。如果真能够实现出来，对性能绝对是有帮助的。





### **使用reduceByKey/aggregateByKey替代groupByKey**

 reduceByKey/aggregateByKey底层使用combinerByKey实现，会在map端进行局部聚合；groupByKey不会。

使用map-side预聚合的shuffle操作,尽量使用有combiner的shuffle类算子。

​       combiner概念：

​         在map端，每一个map task计算完毕后进行的局部聚合

​       combiner好处：

​       a)   降低shuffle write写磁盘的数据量。

​       b)   降低shuffle read拉取数据量的大小。

​       c)   降低reduce端聚合的次数。



### **foreachPartitions替代foreach**

原理类似于“使用mapPartitions替代map”，也是一次函数调用处理一个partition的所有数据，而不是一次函数调用处理一条数据。在实践中发现，foreachPartitions类的算子，对性能的提升还是很有帮助的。比如在foreach函数中，将RDD中所有数据写MySQL，那么如果是普通的foreach算子，就会一条数据一条数据地写，每次函数调用可能就会创建一个数据库连接，此时就势必会频繁地创建和销毁数据库连接，性能是非常低下；但是如果用foreachPartitions算子一次性处理一个partition的数据，那么对于每个partition，只要创建一个数据库连接即可，然后执行批量插入操作，此时性能是比较高的。实践中发现，对于1万条左右的数据量写MySQL，性能可以提升30%以上。

foreach 以一条记录为单位来遍历 RDD

foreachPartition 以分区为单位遍历 RDD

foreach 和 foreachPartition 都是 actions 算子

map 和 mapPartition 可以与它们做类比，但 map 和 mapPartitions 是 transformations 算子

### **filter之后进行coalesce操作**

通常对一个RDD执行filter算子过滤掉RDD中较多数据后（比如30%以上的数据），

建议使用coalesce算子，手动减少RDD的partition数量，

将RDD中的数据压缩到更少的partition中去。

因为filter之后，RDD的每个partition中都会有很多数据被过滤掉，此时如果照常进行后续的计算，其实每个task处理的partition中的数据量并不是很多，有一点资源浪费，而且此时处理的task越多，可能速度反而越慢。因此用coalesce减少partition数量，将RDD中的数据压缩到更少的partition之后，只要使用更少的task即可处理完所有的partition。在某些场景下，对于性能的提升会有一定的帮助。

**使用repartitionAndSortWithinPartitions替代repartition与sort类操作**

repartitionAndSortWithinPartitions是Spark官网推荐的一个算子，官方建议，如果需要在repartition重分区之后，还要进行排序，建议直接使用repartitionAndSortWithinPartitions算子。因为该算子可以一边进行重分区的shuffle操作，一边进行排序。shuffle与sort两个操作同时进行，比先shuffle再sort来说，性能可能是要高的。

**使用broadcast使各task共享同一Executor的集合替代算子函数中各task传送一份集合**

在算子函数中使用到外部变量时，默认情况下，Spark会将该变量复制多个副本，通过网络传输到task中，此时每个task都有一个变量副本。如果变量本身比较大的话（比如100M，甚至1G），那么大量的变量副本在网络中传输的性能开销，以及在各个节点的Executor中占用过多内存导致的频繁GC，都会极大地影响性能。

因此对于上述情况，如果使用的外部变量比较大，建议使用Spark的广播功能，对该变量进行广播。广播后的变量，会保证每个Executor的内存中，只驻留一份变量副本，而Executor中的task执行时共享该Executor中的那份变量副本。这样的话，可以大大减少变量副本的数量，从而减少网络传输的性能开销，并减少对Executor内存的占用开销，降低GC的频率。




## 1.8 troubleshooting调优

### 1.8.1 控制shuffle reduce端缓冲大小以避免OOM
map端的task是不断的输出数据的，数据量可能是很大的。
但是，在map端写过来一点数据，reduce端task就会拉取一小部分数据，先放在buffer中，立即进行后面的聚合、算子函数的应用。
每次reduece能够拉取多少数据，就由buffer来决定。然后才用后面的executor分配的堆内存占比（0.2），hashmap，去进行后续的聚合、函数的执行。

reduce端缓冲默认是48MB（buffer），可能会出什么问题？
缓冲达到最大极限值，再加上你的reduce端执行的聚合函数的代码，可能会创建大量的对象。reduce端的内存中，就会发生内存溢出的问题。
这个时候，就应该减少reduce端task缓冲的大小。我宁愿多拉取几次，但是每次同时能够拉取到reduce端每个task的数量，比较少，就不容易发生OOM内存溢出的问题。

另外，如果你的Map端输出的数据量也不是特别大，然后你的**整个application的资源也特别充足，就可以尝试去增加这个reduce端缓冲大小的，比如从48M，变成96M**。
这样每次reduce task能够拉取的数据量就很大。需要拉取的次数也就变少了。
最终达到的效果，就应该是性能上的一定程度上的提升。
设置
spark.reducer.maxSizeInFlight


### 1.8.2 解决JVM GC导致的shuffle文件拉取失败
比如，executor的JVM进程，内存不够了，发生GC，导致BlockManger，netty通信都停了。
下一个stage的executor，可能是还没有停止掉的，task想要去上一个stage的task所在的exeuctor，去拉取属于自己的数据，结果由于对方正在GC，就导致拉取了半天没有拉取到。
可能会报错shuffle file not found。
但是，可能下一个stage又重新提交了stage或task以后，再执行就没有问题了，因为可能第二次就没有碰到JVM在gc了。
有的时候，出现这种情况以后，会重新去提交stage、task。重新执行一遍，发现就好了。没有这种错误了。


spark.shuffle.io.maxRetries 3
spark.shuffle.io.retryWait 5s

针对这种情况，我们完全可以进行预备性的参数调节。
增大上述两个参数的值，达到比较大的一个值，尽量保证第二个stage的task，一定能够拉取到上一个stage的输出文件。
尽量避免因为gc导致的shuffle file not found，无法拉取到的问题。


### 1.8.3 yarn-cluster模式的JVM内存溢出无法执行问题
总结一下yarn-client和yarn-cluster模式的不同之处：
yarn-client模式，driver运行在本地机器上的；yarn-cluster模式，driver是运行在yarn集群上某个nodemanager节点上面的。
yarn-client的driver运行在本地，通常来说本地机器跟yarn集群都不会在一个机房的，所以说性能可能不是特别好；yarn-cluster模式下，driver是跟yarn集群运行在一个机房内，性能上来说，也会好一些。

实践经验，碰到的yarn-cluster的问题：

有的时候，运行一些包含了spark sql的spark作业，可能会碰到yarn-client模式下，可以正常提交运行；yarn-cluster模式下，可能是无法提交运行的，会报出JVM的PermGen（永久代）的内存溢出，会报出PermGen Out of Memory error log。

yarn-client模式下，driver是运行在本地机器上的，spark使用的JVM的PermGen的配置，是本地的spark-class文件（spark客户端是默认有配置的），JVM的永久代的大小是128M，这个是没有问题的；但是在yarn-cluster模式下，driver是运行在yarn集群的某个节点上的，使用的是没有经过配置的默认设置（PermGen永久代大小），82M。

spark-submit脚本中，加入以下配置即可：
--conf spark.driver.extraJavaOptions="-XX:PermSize=128M -XX:MaxPermSize=256M"

另外，sql有大量的or语句。可能就会出现一个driver端的jvm stack overflow。
基本上就是由于调用的方法层级过多，因为产生了非常深的，超出了JVM栈深度限制的，递归。spark sql内部源码中，在解析sqlor特别多的话，就会发生大量的递归。
建议不要搞那么复杂的spark sql语句。
采用替代方案：将一条sql语句，拆解成多条sql语句来执行。
每条sql语句，就只有100个or子句以内；一条一条SQL语句来执行。



