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
- **Master(总控进程)**：Spark Standalone模式下的主节点，负责管理和分配集群资源来运行Spark Application。
- **Executor(执行进程)**：在Worker节点上运行Aplication的一个进程，该进程负责从线程池中抽取空余线程运行Task，Task并行的数量取决于分配给Executor进程的CPU数量。最终Executor将结果写入内存或磁盘中，每个Application都独立拥有一批Executor。

Spark的作业和任务调度系统是其核心，能够有效地进行调度根本原因是对任务划分有向无环图（DAG）和容错，使得从底层到顶层的各个模块之间调用和处理游刃有余。

- **Task**：分发到 executor 的工作任务，Spark执行应用的最小单元，就是一个线程。
- **Job**：由 Action 组成的 一个或多个 Stage，由谁提交？
- **Stage**：每个Job会因为RDD之间的依赖关系拆分成多组任务集合（TaskSet），调度阶段的划分是由DAGScheduler来划分的，调度阶段有Shuffle Map Stage和Result Stage两种。
- **DAGScheduler：**DAGScheduler是面向调度阶段的任务调度器，负责接收**Spark应用**提交的作业，根据RDD的依赖关系划分 Stage ，并提交调度阶段给TaskScheduler。
- **TaskScheduler：**TaskScheduler是面向任务的调度器，接收DAGScheduler提交过来的调度阶段，把 Task任务 分发到worker节点执行，由Worker节点的Executor来实际运行任务。

![](https://raw.githubusercontent.com/janehzhang/mypic/master/2020/03spark-submit.png)

1、Driver程序的代码运行到action操作，触发了SparkContext的runJob方法。
2、SparkContext调用DAGScheduler的runJob函数。
3、DAGScheduler把Job划分stage，然后把stage转化为相应的Tasks，把Tasks交给TaskScheduler。
4、通过TaskScheduler把Tasks添加到任务队列当中，交给SchedulerBackend进行资源分配和任务调度。
5、调度器给Task分配执行Executor，ExecutorBackend负责执行Task。



## 参考

翻一下书好了，看来看去又看回最基础的，最基础的应该联想的起来啊，还是在山脚下徘徊，没有长进。