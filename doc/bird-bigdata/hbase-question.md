* 一个RegionServer 下，有各表的多个Region。

* 一个Region下，对应一个列簇。

* 一个列簇  对应 Store（一个MemStore + 多个StoreFile（因为MemStore不断 flush 出来））。

  

1. MemStore Flush：形成有序的StoreFIle
2. StoreFIle Compact：mirror将storeFile合并而已，major合并删除的数据
3. Region Split：进行Region移动



### HBase架构

* 基于列设计的稀疏分布式存储，HBase使用 LSM-Tree

 LSM树本质上就是在读写之间取得平衡，和B+树相比，它**牺牲了部分读性能，用来大幅提高写性能**。

**它的原理是把一颗大树拆分成N棵小树，它首先写入到内存中**（内存没有寻道速度的问题，随机写的性能得到大幅提升），**在内存中构建一颗有序小树，随着小树越来越大，内存的小树会flush到磁盘上。**

**当读时，由于不知道数据在哪棵小树上，因此必须遍历所有的小树，但在每颗小树内部数据是有序的。**

* 存储架构

服务器体系结构遵循简单的主从服务器架构，它由 RegionServer 和 MasterServer 构成。Master 负责管理所有的RS，而**HBase中所有的服务器都是通过ZooKeeper来进行协调**，并处理 HBase 服务器运行期间可能遇到的错误。

Master本身不存储HBase中的任何数据，HBase逻辑上的表可能会被划分为多个Region，然后存储到RS中，Master中存储的是从数据到RS中的映射

3> HBase启动

当HBase启动的时候发生了什么？

HMaster启动的时候会连接zookeeper，将自己注册到Zookeeper，首先将自己注册到Backup Master上，因为可能会有很多的节点抢占Master，最终的Active Master要看他们抢占锁的速度。

将会把自己从Backup Master删除，成为Active Master之后，才会去实例化一些类，比如Master Filesytem，table state manager

当一个节点成为Active Master之后，他就会等待Regionserver汇报。

首先Regionserver注册Zookeeper，之后向HMaster汇报。HMaster现在手里就有一份关于Regionserver状态的清单，对各个Regionserver（包括失效的）的数据进行整理，

最后HMaster整理出了一张Meta表，这张表中记录了，所有表相关的Region，还有各个Regionserver到底负责哪些数据等等。然后将这张表，交给Zookeeper。

之后的读写请求，都只需要经过Zookeeper就行了。

Backup Master 会定期同步 Active Master信息，保证信息是最新的。



### 优点

### 缺点

HBase，可以进行高效随机读写，**却并不适用于基于SQL的数据分析方向，大批量数据获取时的性能较差**。

因为分析需要批量获取数据，而HBase本身的设计并不适合批量获取数据

1）都说HBase是列式数据库，其实从底层存储的角度来说它并不是列式的，获取指定列数据时是会读到其他列数据的。看过我之前文章的同学应该比较清楚就不多说了。相对而言Parquet格式针对分析场景就做了很多优化。

2）HBase是LSM-Tree架构的数据库，这导致了HBase读取数据路径比较长，从内存到磁盘，可能还需要读多个HFile文件做版本合并。

LSM 的中心思想就是**将随机写转换为顺序写来大幅提高写入操作的性能**，但是牺牲了部分读的性能。



### rk如何设计

* 长度控制
* 热点控制
* rk设计尽可能包含更多的经常查询信息
* rk的使用寿命



### 读写过程的缓存

整个RegionServer内存（Java进程内存）分为两部分：JVM内存和堆外内存。

* 其中JVM内存中 LRUBlockCache 和堆外内存 BucketCache 一起构成了读缓存**CombinedBlockCache**，用于缓存读到的Block数据，其中LRUBlockCache用于缓存元数据Block，BucketCache用于缓存实际用户数据Block；
* MemStore 用于写流程，缓存用户写入KeyValue数据；还有部分用于RegionServer正常运行所必须的内存；

### HBase写流程

   1) Client先访问zookeeper，从meta表获取相应region信息，**然后找到meta表的数据**

2. 根据namespace、表名和rk  从 meta表的数据找到写入数据对应的region信息，找到对应的 RegionServer；

   （一些通用的细节就是校验是否可读，异常的处理，缓存元数据下次用）

 ![这里写图片描述](https://img-blog.csdn.net/20180313102723251?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvSGFpeFdhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast) 

3. 向对应的 Regionserver发起写请求，把数据依次写入 HLog（WAL）和 MemoryStore，只有这两个都写入成功，此次写入才算成功。（这可不是LSM啊，别说错了。。）
4. 当 MemStore 写入数据达到一个阈值128M后，触发 flush 操作，溢写成 StoreFile (底层是HFile）；若MemStore中的数据有丢失，则可以从HLog上恢复；
5. 溢写的 StoreFile 文件过多时（应为MemStore是小块内存），触发Compact合并操作，合并为一个大的 StoreFile，（majorCompact同时进行版本的合并和数据删除）。
6. StoreFile Compact后，形成越来越大的Region，**达到 hbase.hregion.max.filesize 后**，触发Split操作，Region分成两个，相当于把一个大的region分割成两个region，HBase给两个Region分配相应的 Regionserver 进行管理，从而分担压力。

注意：

* MemStore的最小flush单元是 Region 级别 而不是单个MemStore。可想而知，如果一个HRegion中Memstore过多，每次flush的开销必然会很大，因此我们也建议在进行表设计的时候尽量减少ColumnFamily的个数。

### HBase读流程

和写流程相比，HBase读数据是一个更加复杂的操作流程。

其一是因为整个HBase存储引擎基于LSM树实现，因此**一次范围查询可能会涉及多个分片、多块缓存甚至多个数据存储文件**；其二是因为HBase中更新操作以及删除操作实现都很简单，更新操作并没有更新原有数据，而是使用时间戳属性实现了多版本，就需要过滤已经更新删除的数据。

1. 定位 RS 和 写的流程一样；
2. Client也会缓存 .META. 表信息，以后可以直接连到 RegionServer 。如果集群发生某些变化导致hbase:meta元数据更改，客户端再根据本地元数据表请求的时候就会发生异常，此时客户端需要重新加载一份最新的元数据表到本地。
3. 然后服务器端接收到该请求之后， 通过 StoreFileScanner，先从BucketCache LRU (StoreFIle的热点数据）中找数据，如果没有，才到 MemStore 和 StoreFile磁盘文件上找。因为 StoreFile 使用了LSM树型结构，导致它需要磁盘寻道时间在可预测范围内；而上文也提到了读取HFile速度也会很快，因为节省了寻道开销。1）、hbase可以划分多个region，2）、键是排好序的；3）、按列存储；

​     还有 RowScanner，TimeScanner，BloomFilter的方式来过滤数据。

   3）将请求发送给rowkey所在的RegionServer行get/scan操作

   4）Client也会缓存 .META. 表信息，以后可以直接连到RegionServer 。如果集群发生某些变化导致hbase:meta元数据更改，客户端再根据本地元数据表请求的时候就会发生异常，此时客户端需要重新加载一份最新的元数据表到本地。

墓碑标记和数据不在一个地方，读取数据的时候怎么知道这个数据要删除呢？

如果这个数据比它的墓碑标记更早被读到，那在这个时间点是不知道这个数据会被删 除的，只有当扫描器接着往下读，读到墓碑标记的时候才知道这个数据是被标记为删除的，不需要返回给用户。

所以 HBase 的 Scan 操作在取到所需要的所有行键对应的信息之后还会继续扫描下去，直到被扫描的数据大于给出的限定条件为止，这样它才能知道哪些数据应该被返回给用户，而哪些应该被舍弃。所以**你增加过滤条件也无法减少 Scan 遍历的行数，都要扫一遍！！，只有缩小 STARTROW 和 ENDROW 之间的行键范围才可以明显地加快扫描的速度**。

​    

### HBase的RIT处理

​    1)RIT 的全称是region in transcation。每次hmaster 对region 的open或close 操作都会向Master 的RIT中插入一条记录，因为master 对region的操作要保持原子性，region 的 open 和 close 是通过Hmaster 和 rs协助来完成。为了满足这些操作的协调，回滚，和一致性。

Hmaster 采用了 RIT 机制并结合Zookeeper中Node的状态来保证操作的安全和一致性。

​    2) RIT处理套路

​    套路一：pending_open（或pending_close）状态的region通常可以使用hbck命令修复

​    套路二：failed_open （或failed_close）状态的region通常无法使用hbck命令修复

​    套路三：failed_open （或failed_close）状态的region需检查日志确认region无法打开关闭的具体原因

​    套路四：region处于RIT状态但hbck显示正常，把zk上的region-in-transaction节点相关region删除，重启master。

[RIT简介](https://github.com/mattshma/bigdata/blob/master/hbase/docs/hbase_rit.md)

### HLog

HBase在实现WAL方式时会产生日志信息，即HLog。每一个RegionServer节点上都有一个HLog，所有该RegionServer节点上的Region写入数据均会被记录到该HLog中。HLog的主要职责就是当遇到RegionServer异常时，能够尽量的恢复数据。

WAL 是一个环状的滚动日志结构，因为这种结构写入效果最高，而且可以保证空间不会持续变大。

WAL 的检查间隔由 hbase.regionserver.logroll.period 定义，默认值为 1h。检查的内容是把当前 WAL 中的操作跟实际持久化到 HDFS 上的操作比较，看哪些操作已经被持久化了，被持久化的操作就会被移动到 .oldlogs 文件夹内（这个文件夹也是在 HDFS 上的）。



### Memstore

HBase通过MemStore来缓存Region数据，大小可以通过 hbase.hregion.memstore.flush.size 128M 属性来进行设置。RegionServer在写完HLog后，数据会接着写入到Region的MemStore。由于MemStore的存在，HBase的数据写入并非是同步的，不需要立刻响应客户端。由于是异步操作，具有高性能和高资源利用率等优秀的特性。数据在写入到MemStore中的数据后都是预先按照RowKey的值来进行排序的，这样便于查询的时候查找数据。



由于数据在写入 Memstore 之前，要先被写入 WAL，所以**增加 Memstore 的大小并不能加速写入速度。Memstore 存在的意义是维持数据按照 rowkey 顺序排列，而不是做一个缓存。**

设计 MemStore 的原因有以下几点：

- 由于 HDFS 上的文件不可修改，为了让数据顺序存储从而提高读取效率，HBase 使用了 LSM 树结构来存储数据，数据会先在 Memstore 中**整理成 LSM 树**，最后再刷写到 HFile 上。
- **优化数据的存储**，比如一个数据添加后就马上删除了，这样在刷写的时候就可以直接不把这个数据写到 HDFS 上。

#### Memstore Flush条件

注意：

* MemStore的最小flush单元是 Region 级别 而不是单个MemStore。可想而知，如果一个HRegion中Memstore过多，每次flush的开销必然会很大，因此我们也建议在进行表设计的时候尽量减少ColumnFamily的个数。
* 以下各条件都可以出 Flush：
  * Memstore级别限制：当Region中任意一个MemStore的大小达到了上限（hbase.hregion.memstore.flush.size，**默认128MB**），会触发Memstore刷新。
  * Region级别限制：**当Region中所有Memstore的大小总和达到了上限**（hbase.hregion.memstore.block.multiplier * hbase.hregion.memstore.flush.size，默认 2* 128M = 256M），会触发memstore刷新。
  * Region Server级别限制：当一个Region Server中所有Memstore的大小总和达到了上限（hbase.regionserver.global.memstore.upperLimit ＊ hbase_heapsize，默认 40%的JVM内存使用量），会触发部分Memstore刷新。Flush顺序是按照Memstore由大到小执行，先Flush Memstore最大的Region，再执行次大的，直至总体Memstore内存使用量低于阈值（hbase.regionserver.global.memstore.lowerLimit ＊ hbase_heapsize，默认 38%的JVM内存使用量）。
  * 当一个Region Server中HLog数量达到上限（可通过参数hbase.regionserver.maxlogs配置）时，系统会选取最早的一个 HLog对应的一个或多个Region进行flush
  * HBase定期刷新Memstore：默认周期为1小时，确保Memstore不会长时间没有持久化。为避免所有的MemStore在同一时间都进行flush导致的问题，定期的flush操作有20000左右的随机延时。



#### Memstore Flush流程

为了减少flush过程对读写的影响，HBase采用了类似于两阶段提交的方式，将整个flush过程分为三个阶段：

1. prepare阶段：**遍历当前Region中的所有Memstore**，将Memstore中当前**数据集kvset做一个快照snapshot**，然后**再新建一个新的kvset**。**后期的所有写入操作都会写入新的kvset中**，而整个flush阶段读操作会首先分别遍历kvset和snapshot，如果查找不到再会到HFile中查找。**prepare阶段需要加一把updateLock对写请求阻塞**，结束之后会释放该锁。因为此阶段没有任何费时操作，因此持锁时间很短。
2. flush阶段：遍历所有Memstore，**将prepare阶段生成的snapshot持久化为临时文件**，临时文件会统一放到目录.tmp下。这个过程因为涉及到磁盘IO操作，因此相对比较耗时。
3. commit阶段：遍历所有的Memstore，**将flush阶段生成的临时文件移到指定的ColumnFamily目录下，针对HFile生成对应的storefile和Reader**，把storefile添加到HStore的storefiles列表中，**最后再清空prepare阶段生成的snapshot。**



**影响甚微**

正常情况下，大部分Memstore Flush操作都不会对业务读写产生太大影响，比如这几种场景：HBase定期刷新Memstore、触发Memstore级别限制、触发HLog数量限制以及触发Region级别限制等，这几种场景只会阻塞对应Region上的写请求，阻塞时间很短，毫秒级别。

**影响较大**

然而一旦触发**Region Server级别限制导致flush，就会对用户请求产生较大的影响**。

会阻塞所有落在该Region Server上的更新操作，阻塞时间很长，甚至可以达到分钟级别。

一般情况下Region Server级别限制很难触发，但在一些极端情况下也不排除有触发的可能，下面分析一种可能触发这种flush操作的场景：

相关JVM配置以及HBase配置：

maxHeap = 71

hbase.regionserver.global.memstore.upperLimit = 0.35

hbase.regionserver.global.memstore.lowerLimit = 0.30

基于上述配置，可以得到触发Region Server级别的总Memstore内存和为24.9G



### StoreFile



#### Compact

**触发时机**

HBase中可以触发compaction的因素有很多，最常见的因素有这么三种：

Memstore Flush、

后台线程周期性检查、

手动触发。

1. Memstore Flush: 应该说**compaction操作的源头就来自flush操作**，memstore flush会产生HFile文件，文件越来越多就需要compact。因此在每次执行完Flush操作之后，都会对当前Store中的文件数进行判断，一旦文件数＃ > ，就会触发compaction。需要说明的是，**compaction都是以Store为单位进行的**，而在Flush触发条件下，整个Region的所有Store都会执行compact，所以会在短时间内执行多次compaction。

2. 后台线程周期性检查：后台线程CompactionChecker定期触发检查是否需要执行compaction，检查周期为：hbase.server.thread.wakefrequency*hbase.server.compactchecker.interval.multiplier。和flush不同的是，该线程优先检查文件数＃是否大于，一旦大于就会触发compaction。如果不满足，它会接着检查是否满足major compaction条件，简单来说，如果当前store中hfile的最早更新时间早于某个值mcTime，就会触发major compaction，HBase预想通过这种机制定期删除过期数据。上文mcTime是一个浮动值，浮动区间默认为［7-7*0.2，7+7*0.2］，其中7为hbase.hregion.majorcompaction，0.2为hbase.hregion.majorcompaction.jitter，可见默认在7天左右就会执行一次major compaction。用户如果想禁用major compaction，只需要将参数hbase.hregion.majorcompaction设为0

3. 手动触发

###### 

文件数量通常取决于Compaction的执行策略，一般和两个配置参数有关：

hbase.hstore.compactionThreshold  一个store中的文件数超过多少就应该进行合并；

和hbase.hstore.compaction.max.size，参数合并的文件大小最大是多少，超过此大小的文件不能参与合并。

这两个参数不能设置太’松’（前者不能设置太大，后者不能设置太小），导致Compaction合并文件的实际效果不明显，进而很多文件得不到合并。这样就会导致HFile文件数变多。



**选择合适的HFile合并**

选择合适的文件进行合并是整个compaction的核心，

一旦触发，HBase会将该Compaction交由一个独立的线程处理，**该线程首先会从对应store中选择合适的hfile文件进行合并**，这一步是整个Compaction的核心，选取文件需要遵循很多条件，

比如文件数不能太多、不能太少、文件大小不能太大等等，

最理想的情况是，选取那些承载IO负载重、文件小的文件集，实际实现中，HBase提供了多个文件选取算法：

RatioBasedCompactionPolicy 从老到新选择文件策略、

ExploringCompactionPolicy 在上面的基础上扫描全部来找合适的 和

StripeCompactionPolicy等，

用户也可以通过特定接口实现自己的Compaction算法；

选出待合并的文件后，HBase会根据这些hfile文件总大小挑选对应的线程池处理，最后对这些文件执行具体的合并操作。



**挑选合适的线程池**

HBase实现中有一个专门的线程CompactSplitThead负责接收compact请求以及split请求，

而且为了能够独立处理这些请求，这个线程内部构造了多个线程池：

largeCompactions、

smallCompactions以及splits等，

其中splits线程池负责处理所有的split请求，

largeCompactions和smallCompaction负责处理所有的compaction请求，其中前者用来处理大规模compaction，后者处理小规模compaction。这里需要明白三点：

\1. 上述设计目的是为了能够将请求独立处理，提供系统的处理性能。

\2. 哪些compaction应该分配给largeCompactions处理，哪些应该分配给smallCompactions处理？是不是Major Compaction就应该交给largeCompactions线程池处理？不对。这里有个分配原则：待compact的文件总大小如果大于值throttlePoint（可以通过参数hbase.regionserver.thread.compaction.throttle配置，默认为2.5G），分配给largeCompactions处理，否则分配给smallCompactions处理。

\3. largeCompactions线程池和smallCompactions线程池默认都只有一个线程，用户可以通过参数hbase.regionserver.thread.compaction.large和hbase.regionserver.thread.compaction.small进行配置

**执行HFile文件合并**

选出了待合并的HFile集合，一方面也选出来了合适的处理线程，万事俱备，只欠最后真正的合并。合并流程说起来也简单，主要分为如下几步：

1. 分别读出待合并hfile文件的KV，并顺序写到位于./tmp目录下的临时文件中
2. 将临时文件移动到对应region的数据目录
3. 将compaction的输入文件路径和输出文件路径封装为KV写入WAL日志，并打上compaction标记，最后强制执行sync
4. 将对应region数据目录下的compaction输入文件全部删除

上述四个步骤看起来简单，但实际是很严谨的，具有很强的容错性和完美的幂等性：

1. 如果RS在步骤2之前发生异常，本次compaction会被认为失败，如果继续进行同样的compaction，上次异常对接下来的compaction不会有任何影响，也不会对读写有任何影响。唯一的影响就是多了一份多余的数据。
2. 如果RS在步骤2之后、步骤3之前发生异常，同样的，仅仅会多一份冗余数据。
3. 如果在步骤3之后、步骤4之前发生异常，RS在重新打开region之后首先会从WAL中看到标有compaction的日志，因为此时输入文件和输出文件已经持久化到HDFS，因此只需要根据WAL移除掉compaction输入文件即可



### Region管理

https://mp.weixin.qq.com/s?__biz=MzUwOTE3OTYwNA==&mid=2247484055&idx=1&sn=752d588ecdc342c70bd20b9c3c46c86a&chksm=f917612cce60e83adbf0b913b160efff7b058fd524d8dfe00381907dba9e90acaea0b205949e&scene=21#wechat_redirect

在HBase存储中，通过把数据分配到一定数量的Region来达到负载均衡。

一个HBase表会被分配到一个或者多个Region，这些Region会被分配到一个或者多个RegionServer中。在**自动分割策略**中，当一个Region中的数据量达到阀值就会被自动分割成两个Region。HBase的表中的Region按照RowKey来进行排序，并且一个RowKey所对应的Region只有一个，保证了HBase的一致性。其中一种可能出现问题的情况被称之为“拆分/合并风暴”：

一个Region中由一个或者多个Store组成，每个Store对应一个列族。一个Store中包含一个MemStore和多个Store Files，每个列族是分开存放以及分开访问的。

#### 分区过多影响

HBase每张表在底层存储上是由至少一个Region组成，Region实际上就是HBase表的分区。一个分区在达到一定大小时会自动Split，一分为二。

通生产环境的每个Regionserver节点上会有很多Region存在，我们一般比较关心每个节点上的Region数量，主要为了防止HBase分区过多影响到集群的稳定性：

1. 频繁刷写。Region的一个列族对应一个MemStore，通常HBase的一个MemStore默认大小为128 MB，达到yu值就会 flush 成 StoreFile。

2. 压缩风暴。   当用户的region大小以恒定的速度保持增长时，region拆分会在同一时间发生，因为同时需要压缩region中的存储文件，这个过程会重写拆分之后的region，这将会引起磁盘I/O上升。
   与其依赖HBase 自动管理拆分，用户还不如关闭这个行为然后手动调用 split 和 major.compact 命令。 

   在不同 region 上交错地运行，这样可以尽可能分散I/O 负载，并且避免拆分/合并风暴。用户可以实现一个可以，调用split()和majorCompact()方法的客户端。也可以使用Shell 交互地调用相关命令，或者使用crond定时地运行它们。 

3. MSLAB内存消耗较大

   MSLAB（MemStore-local allocation buffer）存在于每个MemStore中，主要是为了解决HBase内存碎片问题，默认会分配 2 MB 的空间用于缓存最新数据。如果Region数量过多，MSLAB总的内存占用就会比较大。
   
4. HMaster要花大量的时间来分配和移动Region，且过多Region会增加ZooKeeper的负担。



**hbase.hregion.max.filesize 达到后 Region 会进行 Split**。不宜过大（同一个region内发生多次compaction的机会增加）或过小（频繁 split），经过实战，生产高并发运行下，最佳大小5-10GB！关闭某些重要场景的hbase表的major_compact！在非高峰期的时候再去调用major_compact，这样可以减少split的同时，显著提供集群的性能，吞吐量、非常有用。

#### 如何切分

1. Region切分的触发条件是什么？
2. Region切分的切分点在哪里？
3. 如何切分才能最大的保证Region的可用性？
4. 如何做好切分过程中的异常处理？切分过程中要不要将数据移动？



- ConstantSizeRegionSplitPolicy：0.94版本前默认切分策略。这是最容易理解但也最容易产生误解的切分策略，从字面意思来看，当region大小大于某个阈值（hbase.hregion.max.filesize）之后就会触发切分，实际上并不是这样，真正实现中这个阈值是对于某个store来说的，即一个region中最大store的大小大于设置阈值之后才会触发切分。
  - 另外一个问题是这里所说的store大小应该是未压缩文件总大小。
  - 这个策略在生产线上这种切分策略却有相当大的弊端：切分策略对于大表和小表没有明显的区分。设置较大对大表比较友好，但是小表就有可能不会触发分裂，极端情况下可能就1个，这对业务来说并不是什么好事。如果设置较小则对小表友好，但一个大表就会在整个集群产生大量的region，这对于集群的管理、资源使用、failover来说都不是一件好事。
- IncreasingToUpperBoundRegionSplitPolicy: 0.94版本~2.0版本默认切分策略。这种切分策略微微有些复杂，总体来看和ConstantSizeRegionSplitPolicy思路相同，一个region中最大store大小大于设置阈值就会触发切分。
  - 但是这个阈值并不像ConstantSizeRegionSplitPolicy是一个固定的值，而是会在一定条件下不断调整，调整规则和region所属表**在当前regionserver上的region个数有关系** ：(#regions) * (#regions) * (#regions) * flush size * 2，当然阈值并不会无限增大，最大值为用户设置的MaxRegionFileSize。这种切分策略很好的弥补了ConstantSizeRegionSplitPolicy的短板，能够自适应大表和小表。而且在大集群条件下对于很多大表来说表现很优秀，但并不完美，这种策略下很多小表会在大集群中产生大量小region，分散在整个集群中。而且在发生region迁移时也可能会触发region分裂。

- SteppingSplitPolicy: 2.0版本默认切分策略。这种切分策略的切分阈值又发生了变化，相比IncreasingToUpperBoundRegionSplitPolicy简单了一些，依然和待分裂region所属表在当前regionserver上的region个数有关系，如果region个数等于1，切分阈值为flush size * 2，否则为MaxRegionFileSize。这种切分策略对于大集群中的大表、小表会比IncreasingToUpperBoundRegionSplitPolicy更加友好，小表不会再产生大量的小region，而是适可而止。



## LSM 存储模型

Buffer -> Flush ->  Merge

Flush是针对一个Region整体执行操作，而Compaction操作是针对Region上的一个Store而言，因此，从逻辑上看，Flush操作粒度较大。



### 简述hbase的flush流程

​    1）遍历当前 Region 中的所有 Memstore，将 Memstore 中当前数据集 kvset 做一个快照 snapshot，然后再新建一个新的 kvset。后期的所有写入操作都会写入新的 kvset 中，而整个flush阶段读操作会首先分别遍历kvset和snapshot，如果查找不到再会到HFile中查找。prepare阶段需要加一把updateLock对写请求阻塞，结束之后会释放该锁。因为此阶段没有任何费时操作，因此持锁时间很短。

​    2）flush阶段：遍历所有Memstore，将 prepare 阶段 生成的snapshot持久化为临时文件，临时文件会统一放到目录.tmp下。此过程涉及到磁盘IO操作，因此相对比较耗时。



### HBase Compaction

memstore flush 会生成很多小文件 StoreFile 刷到磁盘，为保证查询效率，当 小 StoreFIle 达到阈值，会触发 Compaction 操作 将它们合并成较大的 StoreFile 以减少文件数目。**Compaction是 buffer->flush->merge 的 Log-Structured Merge-Tree模型的关键操作**，主要起到如下几个作用：

1. 合并文件
2. 清除删除、过期、多余版本的数据
3. 提高读写数据的效率

这个过程一直持续到这些文件中最大的文件超过配置的HRegion大小，然后会触发split操作。

compaction分为minor和major两种。minor只是将小文件合并成一个大文件，不删除数据。major会对region下的所有storeFile执行合并操作并最终生成一个文件。



#### minor compaction

如下参数会影响minor compaction，运行机制相比 major 要复杂一些：

- hbase.hstore.compaction.min
  每个Store中需要compaction的StoreFile数目的最小值，默认值为3。调优该值的目标是避免过多太小的StoreFiles进行compact。若其值设置为2，每次一个Store有2个StoreFile都会触发一次minor compaction，这并不太合适。若该值设置太大，需要其他影响minor compaction的值也设置合理。对于大多数情况而言，默认值都能满足。
- hbase.hstore.compaction.max
  触发单个minor compaction的最大StoreFile数。
- hbase.hstore.compaction.min.size
  小于该值的StoreFile自动做为需要minor compaction的StoreFile。该值默认同`hbase.hregion.memstore.flush.size`，即128Mb。对于写压力较大的场景，该值可能需要调小。因为这种使用场景中小文件非常多，即使StoreFile做了minor compaction后，新生成文件的大小仍小于该值。
- hbase.hstore.compaction.max.size
  大于该值的StoreFile自动从minor compaction中排除。该值默认为9223372036854775807 byte，即`LONG.MAX_VALUE`。
- hbase.hstore.compaction.ratio
  对于minor compaction而言，该值能判断大于`hbase.hstore.compaction.min.size`的StoreFile是否会被compaction，将StoreFile按年龄排序，若该文件的大小小于minor compaction后生成文件的大小乘以该ratio的话，则该文件也就做minor compaction，minor compatcion总是从老文件开始选择。如ratio为1，文件从老到新大小依次为100，50, 23, 12 和 12 字节，`hbase.hstore.compaction.min.size`为10，由于`100>(50+23+12+12)*1`和`50>(23+12+12)*1`，所以100，50不会被compaction，而由于`23<(12+12)*0.1`，所以23及比其小的12（文件个数须小于hbase.hstore.compaction.max）均应被compaction。该值是浮点数，默认为1.2。
- hbase.hstore.compaction.ratio.offpeak
  低峰期时可设置不同的ratio用以让大尺寸的StoreFile进行compaction，方法同`hbase.hstore.compaction.ratio`，该值仅当`hbase.offpeak.start.hour`和`hbase.offpeak.end.hour`开启时才生效。

#### major compaction

触发major compaction的条件有：major_compact、majorCompact() API、RegionServer自动运行。影响RegionServer自动运行的相关参数如下：

- hbase.hregion.majorcompaction
  每次执行major compaction的时间间隔，默认为7天。若设置为0，即禁止基于时间的major compaction。若major compaction对影响较大，可将该值设为0，通过crontab或其他文件来执行major compaction。
- hbase.hregion.majorcompaction.jitter
  为防止同一时间运行多个major compaction，该属性对`hbase.hregion.majorcompaction`规定的时间进行了浮动，该值默认值为0.5，即默认情况下major compaction时间在[7-7*0.5天, 7+7+0.5天]这个范围内。

#### 参考

- [hbase-default.xml](https://github.com/apache/hbase/blob/master/hbase-common/src/main/resources/hbase-default.xml)
- [Regions](http://hbase.apache.org/0.94/book/regions.arch.html)



### Region拆分

通常 HBase 是自动处理 Region 拆分的：一旦它们达到了既定的阈值，Region 将被拆分成两个，之后它们可以接收新的数据并继续增长。这个默认行为能够满足大多数用例的需求。

其中一种可能出现问题称之为 “拆分/合并风暴” ：当用户的region大小以恒定的速度保持增长时，region拆分会在同一时间发生，因为同时需要压缩region中的存储文件，这个过程会重写拆分之后的region,这将会引起磁盘I/O上升。 所以最好能用户控制 split 和 major compact。

Region热点问题

Region预拆分



## 参数优化

框架的各式参数就像飞机里面的各个控制按钮！！

**Region大小**

*hbase.hregion.max.filesize：*默认10G，简单理解为Region中任意HStore所有文件大小总和大于该值 Region 就会进行分裂。

解读：实际生产环境中该值不建议太大，也不能太小。太大会导致系统后台执行compaction消耗大量系统资源，一定程度上影响业务响应；太小会导致Region分裂比较频繁（分裂本身其实对业务读写会有一定影响），另外单个RegionServer中必然存在大量Region，太多Region会消耗大量维护资源，并且在rs下线迁移时比较费劲。综合考虑，建议线上设置为50G～80G左右。

**BlockCache**

该模块主要介绍BlockCache相关的参数，这个模块参数非常多，而且比较容易混淆。不同BlockCache策略对应不同的参数，而且这里参数配置会影响到Memstore相关参数的配置。笔者对BlockCache策略一直持有这样的观点：RS内存在20G以内的就选择LRUBlockCache，大于20G的就选择BucketCache中的Offheap模式。接下来所有的相关配置都基于BucketCache的offheap模型进行说明。

*file.block.cache.size：*默认0.4，该值用来设置LRUBlockCache的内存大小，0.4表示JVM内存的40%。

解读：当前HBase系统默认采用LRUBlockCache策略，BlockCache大小和Memstore大小均为JVM的40%。但对于BucketCache策略来讲，Cache分为了两层，L1采用LRUBlockCache，主要存储HFile中的元数据Block，L2采用BucketCache，主要存储业务数据Block。因为只用来存储元数据Block，所以只需要设置很小的Cache即可。建议线上设置为0.05～0.1左右。

*hbase.bucketcache.ioengine：*offheap

*hbase.bucketcache.size：*堆外存大小，设置多大就看自己的物理内存大小喽

**Memstore**

*hbase.hregion.memstore.flush.size：*默认128M（134217728），memstore大于该阈值就会触发flush。如果当前系统flush比较频繁，并且内存资源比较充足，可以适当将该值调整为256M。

*hbase.hregion.memstore.block.multiplier：*默认4，表示一旦某region中所有写入memstore的数据大小总和达到或超过阈值hbase.hregion.memstore.block.multiplier * hbase.hregion.memstore.flush.size，就会执行flush操作，并抛出RegionTooBusyException异常。

解读：该值在1.x版本默认值为2，比较小，为了保险起见会将其修改为5。而当前1.x版本默认值已经为4，通常不会有任何问题。如果日志中出现类似”Above memstore limit, regionName = ****, server = ****, memstoreSizse = ****, blockingMemstoreSize  = ****”，就需要考虑修改该参数了。

*hbase.regionserver.global.memstore.size：*默认0.4，表示整个RegionServer上所有写入memstore的数据大小总和不能超过该阈值，否则会阻塞所有写入请求并强制执行flush操作，直至总memstore数据大小降到hbase.regionserver.global.memstore.lowerLimit以下。

解读：该值在offheap模式下需要配置为0.6～0.65。一旦写入出现阻塞，立马查看日志定位“Blocking update on ***: the global memstore size *** is >= than blocking *** size”。一般情况下不会出现这类异常，如果出现需要明确是不是region数目太多、单表列族设计太多。

*hbase.regionserver.global.memstore.lowerLimit：*默认0.95。不需要修改。

*hbase.regionserver.optionalcacheflushinterval：*默认1h（3600000），hbase会起一个线程定期flush所有memstore，时间间隔就是该值配置。

解读：生产线上该值建议设大，比如10h。因为很多场景下1小时flush一次会导致产生很多小文件，一方面导致flush比较频繁，一方面导致小文件很多，影响随机读性能。因此建议设置较大值。

**HLog**

*hbase.regionserver.maxlogs：*默认为32，region flush的触发条件之一，wal日志文件总数超过该阈值就会强制执行flush操作。该默认值对于很多集群来说太小，生产线上具体设置参考HBASE-14951

*hbase.regionserver.hlog.splitlog.writer.threads：*默认为3，regionserver恢复数据时日志按照region切分之后写入buffer，重新写入hdfs的线程数。生产环境因为region个数普遍较多，为了加速数据恢复，建议设置为10。



#### 58同城的优化

* 资源相关
  * 增加工作线程数(hbase.regionserver.handler.count)
  * 调整gc方式，cms => g1  ，优化大内存gc回收带来的毛刺  。

#### 写优化

* 调大memstore内存占比，是为了减少溢写，而不是因为内存大就加快写入速度；默认128M就溢写。
* 批量写入；
* Phoenix做预分区，分区过多Region过多也不好，保证写入请求均衡；
* 关闭定期major compact；

#### 读优化

* BlockCache作为读缓存，对于读性能来说至关重要。BucketCache的offheap模式；
* Major Compact 一周一次就好了，应为车辆大数据基本都是新增的数据，很少删除过期的。 
* Short-Circuit Local Read功能。Short Circuit策略允许客户端绕过DataNode直接读取本地数据。开启 HDFS 的短路读模式。该特性由 HDFS-2246 引入。我们集群的 RegionServer 与 DataNode 混布，这样的好处是数据有本地化率的保证，数据第一个副本会优先写本地的 Datanode。在不开启短路读的时候，即使读取本地的 DataNode 节点上的数据，也需要发送RPC请求，经过层层处理最后返回数据，而短路读的实现原理是客户端向 DataNode 请求数据时，DataNode 会打开文件和校验和文件，将两个文件的描述符直接传递给客户端，而不是将路径传递给客户端。客户端收到两个文件的描述符之后，直接打开文件读取数据，该特性是通过 UNIX Domain Socket 进程间通信方式实现





无非就这些问题：

Full GC异常导致宕机问题、

RIT问题、

写吞吐量太低以及读延迟较大。



## HBase框架自己的优化 

**异步化**

- 后台线程将memstore写入Hfile；
- 后台线程完成Hfile合并；
- wal异步写入(数据有丢失的风险)。

**数据就近**

- blockcache，缓存常用数据块：读请求先到memstore中查数据，查不到就到blockcache中查，再查不到就会到磁盘上读，把最近读的信息放入blockcache，基于LRU淘汰，可以减少磁盘读写，提高性能；
- 本地化，如果Region Server恰好是HDFS的data node，Hfile会将其中一个副本放在本地；
- 就近原则，如果数据没在本地，Region Server会取最近的data node中数据。

**快速检索**

基于bloomfilter过滤：

- 正常检索，RegionServer会遍历所有Hfile查询所需数据。其中，需要遍历Hfile的索引块才能判断Hfile中是否有所需数据；
- BloomFiler存储HFile的摘要，可以通过极少磁盘IO，快速判断当前HFile是否有所需数据：

行缓存：快速判断Hflie是否有所需要的行，粒度较粗，存储占用少，磁盘IO少，数据较快；

列缓存：快速判断Hfile是否有所需的列，粒度较细，但存储占用较多。

基于timestamp过滤：

- HFile基于日志追加、合并，维护了版本信息；

- 当查询1小时内提交的信息时，可以跳过只包含1小时前数据的文件。

  

