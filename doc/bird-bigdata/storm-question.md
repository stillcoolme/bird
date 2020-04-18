storm的一些知识点，反压机制，ack机制，结合源码来看。

现在人脸项目还对数据的状态没什么要求，所以用Strom还好，要是确定状态的流那就。。。

## Strom安装

Strom启动
```
./zkServer.sh start
启动nimbus主节点： nohup bin/storm nimbus >> /dev/null & 
启动supervisor从节点： nohup bin/storm supervisor >> /dev/null &  
都启动完毕之后，启动strom ui管理界面： bin/storm ui & 
使用了drpc，要启动drpc：  nohup bin/storm drpc &
```

## Storm简介
* 低延迟。高性能。可扩展。
* 分布式。系统都是为应用场景而生的，如果你的应用场景、你的数据和计算单机就能搞定，那么不用考虑这些复杂的问题了。我们所说的是单机搞不定的情况。
* 容错。一个节点挂了不影响应用。

实现一个分布式容错实时计算系统。如果仅仅需要解决这5个问题，可能会有无数种方案，使用消息队列+分布在各个机器上的工作进程不就ok啦？
1. 容易在上面开发应用程序。设计的系统需要应用程序开发人员考虑各个处理组件的分布、消息的传递吗？那就有点麻烦啊，开发人员可能会用不好，也不会想去用。
2. 消息不丢失。用户发布的一个宝贝消息不能在实时处理的时候给丢了；更严格一点，如果是一个精确数据统计的应用，那么它处理的消息要不多不少才行。Storm保证每个消息至少能得到一次完整处理。任务失败时，它会负责从消息源重试消息。
3. 消息严格有序。有些消息之间是有强相关性的，比如同一个宝贝的更新和删除操作消息，如果处理时搞乱顺序完全是不一样的效果了。
4. 快速。系统的设计保证了消息能得到快速的处理，使用ZeroMQ作为其底层消息队列。


## Storm的架构、基本概念

![storm-conponent](https://raw.githubusercontent.com/janehzhang/mypic/master/2020/202003/storm-conponent.png)

* Nimbus：主节点，负责资源分配和任务调度。
* Supervisor：每个节点都运行了一个名为“Supervisor”的守护进程，负责接收Nimbus分配的任务，启动和停止自己管理的N个worker进程。

* Worker：运行具体处理组件逻辑的进程。一个Worker对应一个Topo，conf.setNumWorkers(2);   一个worker对应一个端口对应**一个JVM进程**！不是对应一台机器！

* Topology：storm中运行的一个实时应用程序（相当于MR），（Spout + Bolt = Topo = Component，Spout，Bolt的名字就是ComponentId）形成有向图。点是计算节点，边是数据流。

* Executor：执行的物理线程，就是setBolt制定的那个数字！！用来设置线程数（并行度）（Spark的Executor是进程，而并行度是Task数）。

* Task：代表线程内的任务实例，同一个spout/bolt的多个task会共享一个物理线程executor。用来设置要执行的task数目，处理逻辑的数目。每个spout/bolt的 prepare() 和 execute() 都是 task级别的。

-----------------

* Component: Storm中，Spout和Bolt都是Component。所以，Storm定义了一个名叫IComponent的总接口

* Spout：Topology中的数据流源头。通常情况下spout会从外部数据源中读取数据，然后转换为Topology内部的源数据，然后封装成Tuple形式，之后发送到Stream中，Bolt再接收任意多个输入stream， 作一些业务处理。**Spout是一个主动的角色**，其接口中有个nextTuple()函数，storm框架会不停地调用此函数，用户只要在其中生成源数据即可。

* Bolt：一个Topology中接受数据然后执行处理的**逻辑处理组件**。Bolt可以执行过滤、函数操作、合并、写数据库等任何操作。**Bolt是一个被动的角色**，其接口中有个execute(Tuple input)函数，在接受到消息后会调用此函数。

* Tuple：Bolt之间一次消息传递的基本单元：**有序元素的列表**。通常建模为一组逗号分隔的值，所以就是一个value list。
* Stream：tuple的序列流。

* UI页面看到 executor线程名 [10 - 12]，表示executor线程对应着3个task 10，11， 12。一个executor就运行一个bolt程序

storm 提交 topo 流程图如下：
![](https://raw.githubusercontent.com/janehzhang/mypic/master/2020/202003/storm-submit.png)

## Storm容错机制

### 任务级失败

1. Bolt任务crash引起的消息未被应答。此时，acker中所有与此Bolt任务关联的消息都会因为超时而失败，对应的Spout的fail方法将被调用。

2. acker任务失败。如果acker任务本身失败了，它在失败之前持有的所有消息都将超时而失败。Spout的fail方法将被调用。

3. Spout任务失败。在这种情况下，与Spout任务对接的外部设备(如MQ)负责消息的完整性。例如，当客户端异常时，kestrel队列会将处于pending状态的所有消息重新放回队列中。

### 进程级（任务槽(slot)）故障

1. Worker失败。Supervisor负责监控Worker中的Bolt(或Spout)任务，当Worker失败后会尝试在本机重启它，如果它在启动时连续失败了一定的次数，无法发送心跳信息到Nimbus，Nimbus将在另一台主机上重新分配worker。

2.Supervisor失败。Supervisor是无状态(所有的状态都保存在Zookeeper或者磁盘上)和快速失败(每当遇到任何意外的情况，进程自动毁灭)的，因此Supervisor的失败不会影响当前正在运行的任务，只要及时将他们重新启动即可。

3.Nimbus失败。Nimbus也是无状态和快速失败的，因此Nimbus的失败不会影响当前正在运行的任务，但是当Nimbus失败时，无法提交新的任务，只要及时将它重新启动即可。


### 集群节点(机器)故障
1.Storm集群中的节点故障。此时Nimbus会将此机器上所有正在运行的任务转移到其他可用的机器上运行。

2.Zookeeper集群中的节点故障。Zookeeper保证少于半数的机器宕机系统仍可正常运行，及时修复故障机器即可。



Nimbus是否是“单点故障”的
如果失去了Nimbus节点，Worker也会继续执行。另外，如果worker死亡，Supervisor也会继续重启他们。但是，没有Nimbus，Worker不会在必要时(例如，失去一个Worker的主机)被安排到其他主机，客户端也无法提交任务。

所以Nimbus“在某种程度上”是单点故障。在实践中，这不是一个大问题，因为Nimbus守护进程死亡，不会发生灾难性问题。



## Storm分组机制

Stream Grouping 定义了一个流在Bolt 的Task实例间该**如何被切分进行接收**，**由下游接收数据的Bolt来设置Grouping方式**。
Bolt在多线程下有7种类型的stream grouping ，单线程下都是All Grouping:

1. Shuffle Grouping(随机分组)： 随机派发stream里面的tuple， 保证Bolt的每个Task接收到的tuple数目相同平均分配。（Spout 100条，Bolt的两个Task每个获得50条Tuple）

2. Fields Grouping（按字段分组）： 比如按userid来分组， 具有同样userid的tuple会被分到相同的Bolts， 而不同的userid则会被分配到不同的Bolts。
作用：（1）过滤多输出Field中选择某些Field；去重操作，Join。
       （2）相同的tuple会分给同一个Task处理，比如WordCount，相同的单词给同一个Task统计才能准确，而使用Shuffle Grouping就不行！！
3. Non Grouping  （不分组）：不关心到底谁会收到它的tuple。这种分组和Shuffle grouping是一样的效果，不平均分配。

4. LocalOrShuffleGrouping：如果目标Bolt中的一个或者多个Task和当前产生数据的Task在同一个Worker进程里面，那么就走内部的线程间通信，将Tuple直接发给在当前Worker进程的目的Task。**否则，同shuffleGrouping**。该方式数据传输性能优于shuffleGrouping，因为在Worker内部传输，只需要通过Disruptor队列就可以完成，没有网络开销和序列化开销。**因此在数据处理的复杂度不高，而网络开销和序列化开销占主要地位的情况下，可以优先使用localOrShuffleGrouping来代替shuffleGrouping**。

5. DirectGrouping ：发送者需要使用 OutputCollector 的 emitDirect 方法指定下游的TaskId（哪个Task）接收这个元组。Bolt 可以通过 TopologyContext 来获取它的下游消费者的任务 id，也可以通过跟踪 OutputCollector 的 emit 方法（该方法会返回它所发送元组的目标任务的 id）的数据来获取任务 id。

6. All Grouping （广播发送）： 对于每一个tuple， 所有的Bolts都会收到。（Spout 100条，两个Bolt每个获得100条Tuple，一共200条）

7. Global Grouping  （全局分组）： tuple被分配到一个bolt的其中一个task。再具体一点就是分配给id值最低的那个task。



另外，上游的Stream是可以分流的，下游接收数据的Bolt来设置Grouping方式的时候，通过指定StreamId来接收特定的Stream。

## 并发度相关概念

Executor数代表实际并发数（worker进程中的线程数），这样设置一个并发数：```setBolt(xx, xx, 1);```。

Task数代表最大并发度，是具体的处理逻辑实例，这样设置2个task：```setBolt(xx, xx, 1).setTask(2);```

这样就两个task共享一个executor线程，互相抢executor线程来执行bolt的execute方法。

通过storm rebalance命令：一个component的task数是不会改变的, 但是一个componet的**executer数目是会发生变化的**。

看看下面的例子：
```
Config conf = new Config();
// 2个进程
conf.setNumWorkers(2);

topologyBuilder.setSpout("blue-spout", new BlueSpout(), 2);

topologyBuilder.setBolt("green-bolt", new GreenBolt(), 2)
               .setNumTasks(4)
               .shuffleGrouping("blue-spout");
               
topologyBuilder.setBolt("yellow-bolt", new YellowBolt(), 6)
               .shuffleGrouping("green-bolt");
               
StormSubmitter.submitTopology("mytopology", conf, topologyBuilder.createTopology());
```

通过setBolt和setSpout一共定义 2 + 2 + 6 = 10个 executor threads；

前面 setNumWorkers 设置2个workers, 所以storm会**平均在每个worker上run 5个executors ！！！！**

而对于green-bolt, 定义了4个tasks, 所以每个executor中有2个tasks。

![并行度设置图解](https://img2018.cnblogs.com/blog/659358/201907/659358-20190726152306946-1413403434.png)



## DRPC
分流合流   https://blog.csdn.net/congcong68/article/details/73017445
DRPC用普通的拓扑构造  https://www.cnblogs.com/LHWorldBlog/p/8353706.html
LinearDRPC流程：
https://www.liangzl.com/get-article-detail-27067.html
https://www.cnblogs.com/panfeng412/p/storm-drpc-implementation-mechanism-analysis.html

DRPC Topology由1个DRPCSpout、1个Prepare-Request Bolt、若干个User Bolts（即用户通过LinearDRPCTopologyBuilder添加的Bolts）、1个JoinResult Bolt和1个ReturnResults Bolt组成。除了User Bolts以外，其他的都是由LinearDRPCTopologyBuilder内置添加到Topology中的。接下来，我们从数据流的流动关系来 看，这些Spout和Bolts是如何工作的：

1. DRPCSpout中维护了若干个DRPCInvocationsClient，通过fetchRequest方法从DRPC Server读取需要提交到Topology中计算的RPC请求，然后发射一条数据流给Prepare-Request Bolt：<”args”, ‘”return-info”>，其中args表示RPC请求的参数，而return-info中则包含了发起这次RPC请求的RPC Server信息（host、port、request id），用于后续在ReturnResults Bolt中返回计算结果时使用。

2. Prepare-Request Bolt接收到数据流后，会新生成三条数据流：
<”request”, ”args”>：发给用户定义的User Bolts，提取args后进行DRPC的实际计算过程；
<”request”, ”return-info”>：发给JoinResult Bolt，用于和User Bolts的计算结果做join以后将结果返回给客户端；
<”request”>：在用户自定义Bolts实现了FinishedCallback接口的情况下，作为ID流发给用户定义的最后一级Bolt，用于判断batch是否处理完成。

3. User Bolts按照用户定义的计算逻辑，以及RPC调用的参数args，进行业务计算，并最终输出一条数据流给JoinResult Bolt：<”request”, ”result”>。

4. JoinResult Bolt将上游发来的<”request”, ”return-info”>和<”request”, ”result”>两条数据流做join，然后输出一条新的数据流给ReturnResults Bolt： <”result”, ”return-info”>。

5. ReturnResults Bolt接收到数据流后，从return-info中提取出host、port、request id，根据host和port生成DRPCInvocationsClient对象，并调用result方法将request id及result返回给DRPC Server，如果result方法调用成功，则对tuple进行ack，否则对tuple进行fail，并最终在DRPCSpout中检测到tuple 失败后，调用failRequest方法通知DRPC Server该RPC请求执行失败。

## Storm使用经验分享

1. 使用组件的并行度代替线程池或额外的线程

Storm自身是一个分布式、多线程的框架，对每个Spout和Bolt，我们都可以设置其并发度;

也支持通过**rebalance命令**来动态调整并发度，把负载分摊到多个Worker上。

如果自己在组件内部采用线程池做一些计算密集型的任务，有可能使得某些组件的资源消耗特别高，导致Worker之间资源消耗不均衡，这种情况在组件并行度比较低的时候更明显。

比如某个Bolt设置了1个并行度，但在Bolt中又启动了线程池，这样导致集群中分配了这个Bolt的Worker进程可能会把机器的资源都给消耗光了，影响到其他Topology在这台机器上的任务的运行。

* 不在组件内部启动线程池或Timer做逻辑计算，Storm并不保证额外的线程中处理数据的线程安全。
* 如果真有计算密集型的任务，我们可以把组件的并发度设大，Worker的数量也相应提高，让计算分配到多个节点上。也可以通过CGroup控制每个Worker的资源使用量。



2. 不要用DRPC批量处理大数据

RPC提供了应用程序和Storm Topology之间交互的接口，可供其他应用直接调用，使用Storm的并发性来处理数据，然后将结果返回给调用的客户端。当需要处理批量大数据的时候，问题就比较明显了。

* 处理数据的Topology在超时之前可能无法返回计算的结果。

* 批量处理数据，可能使得集群的负载短暂偏高，处理完毕后，又降低回来，负载均衡性差。

* 批量处理大数据不是Storm设计的初衷，Storm考虑的更多的是时效性。


3. 不在Spout中处理耗时的操作

Spout中nextTuple方法会发射数据流，在启用Ack的情况下，fail方法和ack方法会被触发。

在Storm中Spout是单线程(JStorm的Spout分了3个线程，分别执行nextTuple方法、fail方法和ack方法)。

如果nextTuple方法非常耗时，某个消息被成功执行完毕后，Acker会给Spout发送消息，Spout若无法及时消费，可能造成ACK消息超时后被丢弃，然后Spout反而认为这个消息执行失败了，造成逻辑错误。

反之若fail方法或者ack方法的操作耗时较多，则会影响Spout发射数据的量，造成Topology吞吐量降低。


4. 注意fieldsGrouping的数据均衡性

fieldsGrouping是根据一个或者多个Field对数据进行分组，**不同的目标Task收到不同的数据，而同一个Task收到的数据会相同**。

假设某个Bolt根据用户ID对数据进行fieldsGrouping，如果某一些用户的数据特别多，而另一些用户的数据比较少，就可能使得下一级处理Bolt收到的数据不均衡，整个处理的性能就会受制于某些数据量大的节点。

可以加入更多的分组条件或者更换分组策略，使得数据具有均衡性。

6. 设置合理的MaxSpoutPending值

在启用Ack的情况下，Spout中有个RotatingMap用来保存Spout已经发送出去，但还没有等到Ack结果的消息。RotatingMap的最大个数是有限制的，为p*num-tasks。其中p是topology.max.spout.pending值，也就是MaxSpoutPending(也可以由TopologyBuilder在setSpout通过setMaxSpoutPending方法来设定)，num-tasks是Spout的Task数。如果不设置MaxSpoutPending的大小或者设置得太大，可能消耗掉过多的内存导致内存溢出，设置太小则会影响Spout发射Tuple的速度。

7. 设置合理的Worker数
* 每新增加一个Worker进程，都会将一些原本线程间的内存通信变为进程间的网络通信，这些进程间的网络通信还需要进行序列化与反序列化操作，这些降低了吞吐率。

* 每新增加一个Worker进程，都会额外地增加多个线程（Netty发送和接收线程、心跳线程、System Bolt线程以及其他系统组件对应的线程等），这些线程切换消耗了不少CPU，sys 系统CPU消耗占比增加，在CPU总使用率受限的情况下，降低了业务线程的使用效率。

8. 平衡吞吐量和时效性

Storm的数据传输默认使用Netty。在数据传输性能方面，有如下的参数可以调整：
```
storm.messaging.netty.server_worker_threads和storm.messaging.netty.client_worker_threads  
//接收消息线程和发送消息线程的数量。

netty.transfer.batch.size
// 每次 Netty Client向 Netty Server发送的数据的大小，
// 如果需要发送的Tuple消息大于netty.transfer.batch.size，则Tuple消息会按照netty.transfer.batch.size进行切分，
// 然后多次发送。

storm.messaging.netty.buffer_size
// 每次批量发送的Tuple序列化之后的TaskMessage消息的大小。

storm.messaging.netty.flush.check.interval.ms
// 当有TaskMessage需要发送的时候， Netty Client检查可以发送数据的频率。
```

降低storm.messaging.netty.flush.check.interval.ms的值，可以提高时效性。
增加netty.transfer.batch.size和storm.messaging.netty.buffer_size的值，可以提升网络传输的吐吞量，使得网络的有效载荷提升(减少TCP包的数量，并且TCP包中的有效数据量增加)，通常时效性就会降低一些。
因此需要根据自身的业务情况，合理在吞吐量和时效性直接的平衡。


9. UI中找性能的瓶颈
在Storm的UI中，有3个参数对性能来说参考意义比较明显：Execute latency、Process latency和Capacity。

分别看一下这3个参数的含义和作用。

(1)Execute latency：消息的平均处理时间，单位为毫秒。

(2)Process latency：消息从收到到被ack掉所花的时间，单位为毫秒。如果没有启用Acker机制，那么Process latency的值为0。

(3)Capacity：计算公式为Capacity = Bolt或者Executor调用execute方法处理的消息数量 * 消息平均执行时间 / 时间区间。这个值越接近1，说明Bolt或者Executor基本一直在调用execute方法，因此并行度不够，需要扩展这个组件的Executor数量。


为了在Storm中达到高性能，我们在设计和开发Topology的时候，需要注意以下原则：
* 优化的前提是测量，而不是主观臆测。收集相关数据，再动手，事半功倍。
* 模块和模块之间解耦，模块之间的层次清晰，每个模块可以独立扩展，并且符合流水线的原则。
* 无状态设计，无锁设计，水平扩展支持。
* 为了达到高的吞吐量，延迟会加大;为了低延迟，吞吐量可能降低，需要在二者之间平衡。
* 性能的瓶颈永远在热点，解决热点问题。




## 开发相关

#### ack

Bolt 继承 BaseRichBolt 需要自己ack，emit的时候需要加上 anker tuple： 
```
collector.emit(tuple, new Values(tuple.getValue(0), response));
```
从接口的名字就能看出这接口的意图：BaseBasicBolt说明是对基本功能进行了实现；BaseRichBolt说明只是提供简单的功能，能让你去自定义更丰富的实现。


### 
思考：如何 在多线程高并发下计算  所有Word总数和 去重Word个数？ 
wordcount bolt 后面再加一个 sum bolt 来 单线程来做 全局汇总。。。不能直接在 wordcount bolt 中。


类似企业场景：网站PV和UV
PV: count (session_id)
多线程下做全局汇总，可行的两个方案：
1、shuffleGrouping下，pv * Executer并发数
2、bolt1进行多并发局部汇总，bolt2单线程进行全局汇总

UV:    count(distinct session_id)

（类似WordCount的计算去重word总数）：
bolt1通过fieldGrouping 进行多线程局部汇总，下一级blot2进行单线程保存session_id和count数到Map，下一级blot3进行Map遍历，可以得到：
Pv、UV、访问深度（每个session_id 的浏览数）



场景分析：
单线程下(设置一个executor)：
加减乘除，和任何处理类Operate，(全局)汇总 !!!!!

多线程下：
1、局部加减乘除
2、做处理类Operate，如split
3、持久化，如入DB


打包使用

bin/storm  jar  jar包路径

BasicBolt或者BasicSpout 就会自动调 ack方法。。。。