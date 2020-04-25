* 一个RegionServer 下，有多个Region。
* 一个Region下，对应一个列簇一个MemStore。



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

其中JVM内存中 LRUBlockCache 和堆外内存 BucketCache 一起构成了读缓存**CombinedBlockCache**，用于缓存读到的Block数据，其中LRUBlockCache用于缓存元数据Block，BucketCache用于缓存实际用户数据Block；

MemStore 用于写流程，缓存用户写入KeyValue数据；还有部分用于RegionServer正常运行所必须的内存；



### HBase写流程

   1) Client先访问zookeeper，从meta表获取相应region信息，**然后找到meta表的数据**

   2) 根据namespace、表名和rk根据meta表的数据找到写入数据对应的region信息

   3) 找到对应的 RegionServer；

   4) 向对应的Regionserver发起写请求，并将对应的数据发送到该Regionserver检查操作，看Region是不是只读状态，BlockMemorySize大小限制等。 

把数据依次写入MemoryStore 和HLog（WAL），只有这两个都写入成功，此次写入才算成功。需要获取相关的锁，而且写入MemoryStore 和HLog是原子性的，要么都成功，要么都失败。（这可不是LSM啊，别说错了。。）

   5) MemStore达到一个阈值后把数据flush成StoreFile；若MemStore中的数据有丢失，则可以从HLog上恢复

   6) 多个 StoreFile 文件达到一定的大小后，触发Compact合并操作，合并为一个大的 StoreFile，同时进行版本的合并和数据删除。

   7）Compact后，形成越来越大的Region后，触发Split操作，Region分成两个，相当于把一个大的region分割成两个region。将Region一分为二，HBase给两个Region分配相应的Regionserver进行管理，从而分担压力。



### HBase读流程

和写流程相比，HBase读数据是一个更加复杂的操作流程。

其一是因为整个HBase存储引擎基于LSM-Like树实现，因此**一次范围查询可能会涉及多个分片、多块缓存甚至多个数据存储文件**；其二是因为HBase中更新操作以及删除操作实现都很简单，更新操作并没有更新原有数据，而是使用时间戳属性实现了多版本，就需要过滤已经更新删除的数据。

   1) 客户端从zk的 /meta-region-server节点，读取元数据表所在的 RegionServer 节点信息，元数据表会缓存到本地

   2）从hbase:meta中根据rowkey确定数据所在的RegionServer

   3）将请求发送给rowkey所在的RegionServer行get/scan操作

   4）Client也会缓存 .META. 表信息，以后可以直接连到RegionServer 。如果集群发生某些变化导致hbase:meta元数据更改，客户端再根据本地元数据表请求的时候就会发生异常，此时客户端需要重新加载一份最新的元数据表到本地。

  5）然后服务器端接收到该请求之后需要进行复杂的处理， StoreFileScanner和MemstoreScanner是整个scan的最终执行者。  构建scanner体系（实际上就是做一些scan前的准备工作），在此体系基础上一行一行检索。

Scanner：分成RowScanner，TimeScanner，BloomFilter的方式来过滤数据。

**常说HBase数据读取要读Memstore、StoreFile 和 Blockcache**，为什么上面Scanner只有StoreFileScanner和MemstoreScanner两种？没有BlockcacheScanner?

HBase中数据仅仅独立地存在于Memstore和StoreFile中，Blockcache中的数据只是StoreFile中的部分数据（热点数据），即所有存在于 Blockcache 的数据必然存在于StoreFile中。因此 MemstoreScanner 和 StoreFileScanner 就可以覆盖到所有数据。实际读取时**StoreFileScanner 通过 索引 定位到待查找key所在的block之后，首先检查该block是否存在于Blockcache中，如果存在直接取出，如果不存在再到对应的 StoreFile 中读取。**

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

在HBase运行的过程当中，HLog的容量会随着数据的写入越来越大，HBase会通过HLog过期策略来进行定期清理HLog，每个RegionServer内部均有一个HLog的监控线程。

当数据从MemStore Flush到底层存储(HDFS)上后，说明该时间段的HLog已经不需要了，就会被移到“oldlogs”这个目录中，HLog监控线程监控该目录下的HLog，当该文件夹中的HLog达到“hbase.master.logcleaner.ttl”(单位是毫秒)属性所配置的阀值后，监控线程会立即删除过期的HLog数据。



### Memstore

HBase通过MemStore来缓存Region数据，大小可以通过“hbase.hregion.memstore.flush.size”(单位byte)属性来进行设置。RegionServer在写完HLog后，数据会接着写入到Region的MemStore。由于MemStore的存在，HBase的数据写入并非是同步的，不需要立刻响应客户端。由于是异步操作，具有高性能和高资源利用率等优秀的特性。数据在写入到MemStore中的数据后都是预先按照RowKey的值来进行排序的，这样便于查询的时候查找数据。



### Region管理

https://mp.weixin.qq.com/s?__biz=MzUwOTE3OTYwNA==&mid=2247484055&idx=1&sn=752d588ecdc342c70bd20b9c3c46c86a&chksm=f917612cce60e83adbf0b913b160efff7b058fd524d8dfe00381907dba9e90acaea0b205949e&scene=21#wechat_redirect

在HBase存储中，通过把数据分配到一定数量的Region来达到负载均衡。

一个HBase表会被分配到一个或者多个Region，这些Region会被分配到一个或者多个RegionServer中。在**自动分割策略**中，当一个Region中的数据量达到阀值就会被自动分割成两个Region。HBase的表中的Region按照RowKey来进行排序，并且一个RowKey所对应的Region只有一个，保证了HBase的一致性。其中一种可能出现问题的情况被称之为“拆分/合并风暴”：

一个Region中由一个或者多个Store组成，每个Store对应一个列族。一个Store中包含一个MemStore和多个Store Files，每个列族是分开存放以及分开访问的。

#### 分区过多影响

HBase每张表在底层存储上是由至少一个Region组成，Region实际上就是HBase表的分区。一个分区在达到一定大小时会自动Split，一分为二。

通生产环境的每个Regionserver节点上会有很多Region存在，我们一般比较关心每个节点上的Region数量，主要为了防止HBase分区过多影响到集群的稳定性：

1. 频繁刷写。Region的一个列族对应一个MemStore，假设HBase表都有统一的1个列族配置，则每个Region只包含一个MemStore。通常HBase的一个MemStore默认大小为128 MB，见参数*hbase.hregion.memstore.flush.size*。当可用内存足够时，每个MemStore可以分配128 MB空间。当可用内存紧张时，假设每个Region写入压力相同，则理论上每个MemStore会平均分配可用内存空间。

2. 压缩风暴。   当用户的region大小以恒定的速度保持增长时，region拆分会在同一时间发生，因为同时需要压缩region中的存储文件，这个过程会重写拆分之后的region,这将会引起磁盘I/O上升。
   与其依赖HBase 自动管理拆分，用户还不如关闭这个行为然后手动调用 split 和 major.compact 命令。 

   在不同 region 上交错地运行，这样可以尽可能分散I/O 负载，并且避免拆分/合并风暴。用户可以实现一个可以，调用split()和majorCompact()方法的客户端。也可以使用Shell 交互地调用相关命令，或者使用crond定时地运行它们。 

3. MSLAB内存消耗较大

   MSLAB（MemStore-local allocation buffer）存在于每个MemStore中，主要是为了解决HBase内存碎片问题，默认会分配 2 MB 的空间用于缓存最新数据。如果Region数量过多，MSLAB总的空间占用就会比较大。比如当前节点有1000个包含1个列族的Region，MSLAB就会使用1.95GB的堆内存，即使没有数据写入也会消耗这么多内存。



生产环境中，如果一个regionserver节点的Region数量在 20~200 我们认为是比较正常的，但是我们也要重点参考理论合理计算值。如果每个Region的负载比较均衡，分区数量在2~3倍的理论合理计算值通常认为也是比较正常的。

假设我们集群单节点Region数量比2~3倍计算值还要多，因为实际存在单节点分区数达到1000+/2000+的集群，遇到这种情况我们就要密切观察Region的刷写压缩情况了，主要从日志上分析，因为Region越多HBase集群的风险越大。经验告诉我们，如果单节点Region数量过千，集群可能存在较大风险。



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

**Region**

*hbase.hregion.max.filesize：*默认10G，简单理解为Region中任意HStore所有文件大小总和大于该值就会进行分裂。

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

* 提高IO能力，抬高系统瓶颈：
  * 磁盘升级， SATA => SSD （hadoop2.6.0以后支持异构存储，如果成本有限，可以考虑**仅支持一个wal目录的ssd**  ）
  * 网卡升级， KM => 10KM  
* 参数
  * 减少HFile数量同时增加compaction对应资源数，避免堆积。
    compaction文件数阈值 (hbase.hstore.compactionThreshold 10=>3)
    compaction线程数量 (hbase.regionserver.thread.compaction.large/small)
  * 增加工作线程数(hbase.regionserver.handler.count)
  * 调整gc方式，cms => g1  ，优化大内存gc回收带来的毛刺  。



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

  

