## 1.List

### 1.1 ArrayList

* 由数组实现，所以是一块连续的内存，可以随机访问（因为可以指针加偏移量定位到元素）
    * 按数组下标访问元素－get（i）、set（i,e） 的性能很高，这是数组的基本优势。
    * 如果按下标插入元素、删除元素－add（i,e）、 remove（i）、remove（e），则要用System.arraycopy（）来复制移动部分受影响的元素，性能就变差了。
* 超出限制时会增加50%容量，用System.arraycopy（）复制到新开辟的1.5倍的数组，比较耗时。因此最好能给出数组大小的预估值。默认第一次插入元素时创建大小为10的数组。
* contains方法，对数组进行for循环遍历，效率低。可以的话应该用Set来代替。


### 1.2 LinkedList
LinkedList也实现了List接口，也可以按索引访问元素，表面上用起来感觉差不多，但是其底层却有天壤之别。


* 由双向链表实现。链表无容量限制，但双向链表本身使用了更多空间，每插入一个元素都要构造一个额外的Node对象（分配一块位置各异的内存空间），也需要额外的链表指针操作。
    * 按下标访问元素－get（i）、set（i,e） 遍历链表将指针移动到位 （如果i>数组大小的一半，会从末尾移起）。
    * 插入、删除元素时修改前后节点的指针即可，不再需要复制移动。但还是要部分遍历链表的指针才能移动到下标所指的位置。

Apache Commons 有个TreeNodeList，里面是棵二叉树，可以快速移动指针到位。


所以数组更像是康庄大道、四平八稳。链表更像是曲径通幽、人迹罕至。一个像探险，步步为营。一个像回家，轻车熟路。 


### 1.3 CopyOnWriteArrayList

CopyOnWriteArrayList是一个基于 ReentrantLock  的**线程安全**的ArrayList ，通过写入时复制思想设计（程序设计领域中的一种优化策略），在修改时先复制出一个数组快照来修改，改好了，再让内部指针指向新数组：

因为对快照的修改对读操作来说不可见，所以读读之间不互斥，读写之间也不互斥，只有写写之间要加锁互斥。

CopyOnWrite的缺点，主要存在两个问题，即内存占用问题和数据一致性问题。

1. 内存占用问题。因为CopyOnWrite的写时复制机制，所以在进行写操作的时候，内存里会同时驻扎两个对象的内存。可能造成频繁的Yong GC和Full GC。
   1. 之前我们系统中使用了一个服务由于每晚使用CopyOnWrite机制更新大对象，造成了每晚15秒的Full GC，应用响应时间也随之变长。
   2. 针对内存占用问题，可以通过压缩容器中的元素的方法来减少大对象的内存消耗，比如，如果元素全是10进制的数字，可以考虑把它压缩成36进制或64进制。或者不使用CopyOnWrite容器，而使用其他的并发容器，如ConcurrentHashMap。
2. 数据一致性问题。只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。

但复制快照的成本昂贵，典型的适合读多写少的场景。

虽然增加了addIfAbsent（e）方法，会遍历数组来检查元素是否已存在，性能可想像的不会太好。

### 1.4 遗憾

无论哪种实现，按值返回下标contains（e）, indexOf（e）, remove（e） 都需遍历所有元素进行比较，性能可想像的不会太好。

没有按元素值排序的SortedList。

除了CopyOnWriteArrayList，再没有其他线程安全又并发优化的实现如ConcurrentLinkedList。凑合着用Set与Queue中的等价类时，会缺少一些List特有的方法如get（i）。如果更新频率较高，或数组较大时，还是得用Collections.synchronizedList（list），对所有操作用同一把锁来保证线程安全。

------

## 2.Map

在讨论哈希表之前，我们先大概了解下其他数据结构在新增，查找等基础操作执行性能：

　　**数组**：采用一段连续的存储单元来存储数据。对于指定下标的查找，时间复杂度为O(1)；通过给定值进行查找，需要遍历数组，逐一比对给定关键字和数组元素，时间复杂度为O(n)，当然，对于有序数组，则可采用二分查找，插值查找，斐波那契查找等方式，可将查找复杂度提高为O(logn)；对于一般的插入删除操作，涉及到数组元素的移动，其平均复杂度也为O(n)

　　**线性链表**：对于链表的新增，删除等操作（在找到指定操作位置后），仅需处理结点间的引用即可，时间复杂度为O(1)，而查找操作需要遍历链表逐一进行比对，复杂度为O(n)

　　**二叉树**：对一棵相对平衡的有序二叉树，对其进行插入，查找，删除等操作，平均复杂度均为O(logn)。

　　**哈希表**：相比上述几种数据结构，在哈希表中进行添加，删除，查找等操作，性能十分之高，不考虑哈希冲突的情况下，仅需一次定位即可完成，时间复杂度为O(1)，接下来我们就来看看哈希表是如何实现达到惊艳的常数阶O(1)的。

　　我们知道，数据结构的物理存储结构只有两种：**顺序存储结构**和**链式存储结构**（像栈，队列，树，图等是从逻辑结构去抽象的，映射到内存中，属于链式），而在上面我们提到过，在数组中根据下标查找某个元素，一次定位就可以达到，哈希表利用了这种特性，**哈希表的主干就是数组**。

　　比如我们要新增或查找某个元素，我们通过把当前元素的关键字 通过某个函数映射到数组中的某个位置，通过数组下标一次定位就可完成操作。

　　　　　　　　**存储位置 = f(关键字)**

　　其中，这个函数f一般称为**哈希函数**，这个函数的设计好坏会直接影响到哈希表的优劣。



### 2.1 HashMap

以Entry[]数组实现的哈希桶数组，用Key的哈希值取模桶数组的大小可得到数组下标。

插入元素时，如果两条Key落在同一个桶（比如哈希值1和17取模16后都属于第一个哈希桶），我们称之为哈希冲突。

JDK的做法是链表法，Entry用一个next属性实现多个Entry以单向链表存放。查找哈希值为17的key时，先定位到哈希桶，然后链表遍历桶里所有元素，逐个比较其Hash值然后key值。

在JDK8里，新增默认为8的阈值，当一个桶里的Entry超过閥值，就不以单向链表而以红黑树来存放以加快Key的查找速度。

当然，最好还是桶里只有一个元素，不用去比较。所以默认当Entry数量达到桶数量的75%时，哈希冲突已比较严重，就会成倍扩容桶数组，并重新分配所有原来的Entry。扩容成本不低，所以也最好有个预估值。

取模用与操作（hash & （arrayLength-1））会比较快，所以数组的大小永远是2的N次方， 你随便给一个初始值比如17会转为32。默认第一次放入元素时的初始值是16。

iterator（）时顺着哈希桶数组来遍历，看起来是个乱序。

 

### 2.2 LinkedHashMap

扩展HashMap，每个Entry增加双向链表，号称是最占内存的数据结构。

它虽然增加了时间和空间上的开销，但是通过维护一个运行于所有条目的双向链表，记录插入数据的先后顺序，LinkedHashMap保证了元素迭代的顺序。 

*  按照访问顺序输出，就是被访问的entry会被链表链接到尾部。所以可以用来实现 LRUCache；



### 2.3 TreeMap

以红黑树实现，红黑树又叫自平衡二叉树：

```
对于任一节点而言，其到叶节点的每一条路径都包含相同数目的黑结点。
```

上面的规定，使得树的层数不会差的太远，使得所有操作的复杂度不超过 O（lgn），但也使得插入，修改时要复杂的左旋右旋来保持树的平衡。

支持iterator（）时按Key值排序，可按实现了Comparable接口的Key的升序排序，或由传入的Comparator控制。在树上插入/删除元素的代价一定比HashMap的大。

支持SortedMap接口，如firstKey（），lastKey（）取得最大最小的key，或sub（fromKey, toKey）, tailMap（fromKey）剪取Map的某一段。

 

### 2.4 EnumMap

EnumMap的原理是，在构造函数里要传入枚举类，那它就构建一个与枚举的所有值等大的数组，按Enum. ordinal（）下标来访问数组。性能与内存占用俱佳。

美中不足的是，因为要实现Map接口，而 V get（Object key）中key是Object而不是泛型K，所以安全起见，EnumMap每次访问都要先对Key进行类型判断，在JMC里录得不低的采样命中频率。

 

### 2.5 ConcurrentHashMap

并发优化的HashMap。

在JDK5里的经典设计，默认16把写锁（可以设置更多），有效分散了阻塞的概率。数据结构为Segment[]，每个Segment一把锁。Segment里面才是哈希桶数组。Key先算出它在哪个Segment里，再去算它在哪个哈希桶里。

也没有读锁，因为put/remove动作是个原子动作（比如put的整个过程是一个对数组元素/Entry 指针的赋值操作），读操作不会看到一个更新动作的中间状态。

但在JDK8里，Segment[]的设计被抛弃了，改为只在需要锁的时候加锁。

支持ConcurrentMap接口，如putIfAbsent（key，value）与相反的replace（key，value）与以及实现CAS的replace（key, oldValue, newValue）。

 

### 2.6 ConcurrentSkipListMap

JDK6新增的并发优化的SortedMap，以SkipList结构实现。Concurrent包选用它是因为它支持基于CAS的无锁算法，而红黑树则没有好的无锁算法。

原理上，可以想象为多个链表组成的N层楼，其中的元素从稀疏到密集，每个元素有往右与往下的指针。从第一层楼开始遍历，如果右端的值比期望的大，那就往下走一层，继续往前走。

 

[![72995dcc07ad698ab8d8d&690](http://files.blogcn.com/wp06/M00/00/F3/CpCCFVg5ovIAAAAAAABECgHv5n894.jpeg)](http://files.blogcn.com/wp06/M00/00/F3/CpCCFVg5ovIAAAAAAABECgHv5n894.jpeg)

典型的空间换时间。每次插入，都要决定在哪几层插入，同时，要决定要不要多盖一层楼。

它的size（）同样不能随便调，会遍历来统计。

 

------

## 3.Set

 

所有Set几乎都是内部用一个Map来实现, 因为Map里的KeySet就是一个Set，而value是假值，全部使用同一个Object即可。

Set的特征也继承了那些内部的Map实现的特征。

**HashSet**：内部是HashMap。

**LinkedHashSet**：内部是LinkedHashMap。

**TreeSet**：内部是TreeMap的SortedSet。

**ConcurrentSkipListSet**：内部是ConcurrentSkipListMap的并发优化的SortedSet。

**CopyOnWriteArraySet**：内部是CopyOnWriteArrayList的并发优化的Set，利用其addIfAbsent（）方法实现元素去重，如前所述该方法的性能很一般。

好像少了个**ConcurrentHashSet**，本来也该有一个内部用ConcurrentHashMap的简单实现，但JDK偏偏没提供。Jetty就自己简单封了一个，Guava则直接用java.util.Collections.newSetFromMap（new ConcurrentHashMap（）） 实现。

 

------

 

## 4.Queue

Queue是在两端出入的List，所以也可以用数组或链表来实现。

[队列相关](https://www.cnblogs.com/lemon-flm/p/7877898.html)

![](pic/queue.png)

### 4.1 普通队列

#### 4.1.1 LinkedList

class LinkedList<E>  extends AbstractSequentialList<E> implements List<E>, Deque<E>

以双向链表实现的LinkedList既是 List ，也是 Deque。

#### 4.1.2 ArrayDeque

以循环数组实现的双向Queue。大小是2的倍数，默认是16。

为了支持FIFO，即从数组尾压入元素（快），从数组头取出元素（超慢），就不能再使用普通ArrayList的实现了，改为使用循环数组。

有队头队尾两个下标：弹出元素时，队头下标递增；加入元素时，队尾下标递增。如果加入元素时已到数组空间的末尾，则将元素赋值到数组[0]，同时队尾下标指向0，再插入下一个元素则赋值到数组[1]，队尾下标指向1。**如果队尾的下标追上队头，说明数组所有空间已用完，进行双倍的数组扩容。**

#### 4.1.3 PriorityQueue

用平衡最小二叉堆实现的优先级队列，不再是FIFO，而是按元素实现的Comparable接口或传入Comparator的比较结果来出队，数值越小，优先级越高，越先出队。但是注意其iterator()的返回不会排序。

平衡最小二叉堆，用一个简单的数组即可表达，可以快速寻址，没有指针什么的。最小的在queue[0] ，比如queue[4]的两个孩子，会在queue[2*4+1] 和 queue[2*（4+1）]，即queue[9]和queue[10]。

入队时，插入queue[size]，然后二叉地往上比较调整堆。

出队时，弹出queue[0]，然后把queque[size]拿出来二叉地往下比较调整堆。

初始大小为11，空间不够时自动50%扩容。

 

### **4.2 线程安全  并发队列**

线程池实现，就涉及到各种 BlockingQueue

#### 4.2.1 ConcurrentLinkedQueue/Deque

无界的并发优化的Queue，基于链表，实现了依赖于CAS的无锁算法。

ConcurrentLinkedQueue的结构是单向链表和head/tail两个指针，因为入队时需要修改队尾元素的next指针，以及修改tail指向新入队的元素两个CAS动作无法原子，所以需要的特殊的算法。

#### 

===================================================

#### **BlockingQueue**

BlockingQueue很好的解决了多线程中，如何高效安全“传输”数据的问题。通过这些高效并且线程安全的队列类，为我们快速搭建高质量的多线程程序带来极大的便利。

一来如果队列已空不用重复的查看是否有新数据而会阻塞在那里，二来队列的长度受限，用以保证生产者与消费者的速度不会相差太远。

- `PriorityBlockingQueue`：
- `SynchronousQueue`：特殊的`BlockingQueue`，对其的操作必须是放和取交替完成的。

当入队时队列已满，或出队时队列已空，不同函数的效果见下表：

|      | 立刻报异常  | 立刻返回布尔（offer poll可设等待时间) | 阻塞等待 |
| ---- | ----------- | ------------------------------------- | -------- |
| 入队 | add（e）    | offer（e）                            | put（e） |
| 出队 | remove（）  | poll（）                              | take（） |
| 查看 | element（） | peek（）                              | 无       |



#### 4.3.1 ArrayBlockingQueue

FIFO队列，规定长度的`BlockingQueue`，其构造函数必须带一个int参数来指明其大小。

​	也是基于循环数组实现。有一把公共的锁与notFull、notEmpty两个Condition管理队列满或空时的阻塞状态。

　基于数组的阻塞队列实现，在ArrayBlockingQueue内部，维护了一个定长数组，以便缓存队列中的数据对象，这是一个常用的阻塞队列，除了一个定长数组外，ArrayBlockingQueue内部还保存着两个整形变量，分别标识着队列的头部和尾部在数组中的位置。

　　ArrayBlockingQueue在生产者放入数据和消费者获取数据，都是共用同一个锁对象，由此也意味着两者无法真正并行运行，这点尤其不同于LinkedBlockingQueue；按照实现原理来分析，ArrayBlockingQueue完全可以采用分离锁，从而实现生产者和消费者操作的完全并行运行。

​	Doug Lea之所以没这样去做，也许是因为ArrayBlockingQueue的数据写入和获取操作已经足够轻巧，以至于引入独立的锁机制，除了给代码带来额外的复杂性外，其在性能上完全占不到任何便宜。 ArrayBlockingQueue和LinkedBlockingQueue间还有一个明显的不同之处在于，前者在插入或删除元素时不会产生或销毁任何额外的对象实例，而后者则会生成一个额外的Node对象。这在长时间内需要高效并发地处理大批量数据的系统中，其对于GC的影响还是存在一定的区别。而在创建ArrayBlockingQueue时，我们还可以控制对象的内部锁是否采用公平锁，默认采用非公平锁。


#### 4.3.2 LinkedBlockingQueue/Deque

FIFO队列，长度不定的`BlockingQueue`，若其构造函数带一个规定大小的参数，生成的BlockingQueue有大小限制，若不带大小参数，所生成的`BlockingQueue`的大小由`Integer.MAX_VALUE`来决定。

基于链表实现，所以可以把长度设为Integer.MAX_VALUE成为无界无等待的。

利用链表的特征，分离了takeLock与putLock两把锁，继续用notEmpty、notFull管理队列满或空时的阻塞状态。



#### 4.3.3 PriorityBlockingQueue

无界的PriorityQueue，也是基于数组存储的二叉堆（见前）。一把公共的锁实现线程安全。因为无界，空间不够时会自动扩容，所以入列时不会锁，出列为空时才会锁。

 类似于`LinkedBlockingQueue`，但其所含对象的排序不是FIFO，而是依据对象的自然排序顺序或者是构造函数所带的`Comparator`决定的顺序。



#### 4.3.4 DelayQueue

内部包含一个PriorityQueue，同样是无界的，同样是出列时才会锁。一把公共的锁实现线程安全。元素需实现Delayed接口，每次调用时需返回当前离触发时间还有多久，小于0表示该触发了。

pull（）时会用peek（）查看队头的元素，检查是否到达触发时间。ScheduledThreadPoolExecutor用了类似的结构。



### **4.4 同步队列**

特殊的`BlockingQueue`，对其的操作必须是放和取交替完成的。

SynchronousQueue同步队列本身无容量，放入元素时，比如等待元素被另一条线程的消费者取走再返回。JDK线程池里用它。

JDK7还有个LinkedTransferQueue，在普通线程安全的BlockingQueue的基础上，增加一个transfer（e） 函数，效果与SynchronousQueue一样。

------

 

## 5. 参考文档

- **红黑树：** https://github.com/julycoding/The-Art-Of-Programming-By-July/blob/master/ebook/zh/03.01.md

- **跳表：**http://blog.sina.com.cn/s/blog_72995dcc01017w1t.html

- **二叉堆：**http://blog.csdn.net/lcore/article/details/9100073

- **ConcurrentLinkedQueue**：http://www.ibm.com/developerworks/cn/java/j-jtp04186/

- [队列大全](https://blog.csdn.net/qq_39291929/article/details/81155201)

