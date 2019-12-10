# 数据结构
### 时间空间复杂度，做完每道题都应该分析一下时间空间复杂度！

### 链表
* 反转链表，两两元素反转，判断链表是否有环
    
### 堆栈
* 判断括号是否有效20，用栈实现堆232，用堆实现栈225

### 哈希表、Set：用于查询和计数
* 有效的字母异位词242，2Sum1，3Sum15

### 优先队列（正常入，按优先级出）（由小顶堆，大顶堆，二叉搜索树实现）
* 流进的第K个最大的元素703，滑动窗口内最大的数239

* add      增加一个元索，如果队列已满，则抛出一个IIIegaISlabEepeplian异常
* remove   移除并返回队列头部的元素，如果队列为空，则抛出一个NoSuchElementException异常
* offer    添加一个元素并返回true，如果队列已满，则返回false
* poll     移除并返回队列头部的元素，如果队列为空，则返回null
* peek     返回队列头部的元素，如果队列为空，则返回null

* put      添加一个元素，如果队列满，则阻塞
* take     移除并返回队列头部的元素，如果队列为空，则阻塞


### 二叉树，二叉搜索树（左子树小于根结点，右子树大于根节点）
* 验证二叉搜索树98，最小公共祖先235，

### B-tree (Balance tree)
https://mp.weixin.qq.com/s/raIWLUM1kdbYvz0lTWPTvw

https://mp.weixin.qq.com/s/wGqqSFpBnjpytw8J2Sniyw

注意：B-tree 不念 B減樹，而是直接念 Btree。一般像是 MySQL PostgreSQL 都是用 Btree 的方式建立索引。

为什麼用 Btree
用二叉树（二叉搜索树）進行查詢，最快的時間複雜度是 O(logN)，以算法邏輯上面來說，二叉树搜尋的查找速度跟比較次數都是最小的，为什麼还要用 Btree？

我們還需要考量一個現實的問題:磁盘 I/O

数据库的索引都是儲存在磁盘上，當數據量龐大時，索引的大小可能會有幾 G 甚至更多；
查询时我們不可能一次把几G的索引全部加載到內存裡，
由于索引树的每个节点对应每一個磁盘分頁，每查找一次索引节点对应一次磁盘IO
当要查找其中一个索引节点，从根节点出发，最坏情况要经过树的高度次的磁盘IO才能找到。

所以 Btree 的特點就是把「瘦高」的結構變成「矮胖」，來降低 I/O 次數。
Btree是一种多路平衡查找树，每个节点最多包含 k 个孩子（k阶Btree），k 的大小取决于磁盘页的大小。
Btree 每個节点最多可以包容兩個值，就可以在节点內定位省了一次到不同硬碟分頁的時間。

Btree结构：
![](src/main/resources/file/Btree结构.png)

### B+tree
B+ Tree 特征
每個叶子节点都帶有指向下一個节点的指針，形成有序链表，加速范围查询
只有叶子节点帶卫星数据，父节点只帶关键字与指针，单一节点更省空间，让查询 I/O 變小
所有查询都會查到叶子节点，查询性能穩定

B+ Tree 更加矮胖
B+ Tree 中的内部节点，只存放关键字与子节点的指針，不存其他的 Satellite Information，因此最大化了內部節點的分支因子，所以說以同樣大小的硬碟分頁可以容納更多的節點元素。

換句話說，以數據量相同的情況下， B+ Tree 會比 B-Tree 來的更加矮胖，有效的節省硬碟 I/O 次數。

 
### trie树（字典树）
说句实话，工程上来说，和hash比起来，trie就是没有用的东西。这个可以说很深刻地表现出了一个学生和一个软件工程师的思维差异。
* hash类的算法都有可能因为碰撞而退化，而trie是稳定的复杂度，简直太招人喜欢了。但是工程师99.9%的情况下都会用hash。
* 因为，hash函数、map、set，都有内置库，所有语言都有。这意味着一个功能用hash就是五行代码，甚至体现不出用了hash，
* 而用trie，你要么先交一份trie开源库的分析选型比较报告来，要么自己撸一个，附带着先写一千行的单元测试用例，再跑个压测。
* 写出简单可用能快速上线的代码要更重要。

### 并查集（union&find）
树型的数据结构，用于处理一些不交集的合并及查询问题。
一开始初始化所有节点为自己指向自己的节点。
Find：确定元素属于哪一个子集。它被用来确定两个元素是否属于同一子集。
Union：将两个子集合并成同一个集合。

# 算法
### 递归，分治
递归：盗梦空间。
1. 先给定一个条件让你知道是否在梦境里，在第几层梦境（是否在递归里，同时让你知道在第几层，怎么返回）；
2. 然后是梦境里要做的事（业务逻辑）；
3. 然后是进下一层梦境（再调函数自己）。

分治：利用递归分而治之，子问题没有中间结果重复计算的，没有相关性，就可以同时在cpu多个核跑更快点。
中间结果重复计算就要动态规划或者保留中间结果。

* 众数169, 生成有效括号22

### 广度优先遍历BFS，深度优先遍历DFS
* 二叉树层次遍历102，二叉树的最小最大深度104 111

### 剪枝（搜索中常用的优化方法）

### 回溯（递归（重复计算））
包含问题所有解的解空间树中，将可能的结果搜索一遍，从而获得满足条件的解。

搜索过程采用深度遍历策略，并随时判定结点是否满足条件要求，满足要求就继续向下搜索，若不满足要求则回溯到上一层，这种解决问题的方法称为回溯法。

* N皇后51 52

### 动态规划（dp的p是递推）
1. 递归 + 记忆化(备忘数组) -> 递推
2. 状态的定义：opt[n], 
3. 状态得到 **状态** **选择** 方程：opt[n] = best_of(opt[n-1], opt[n-2],...)
4. 最优子结构

* 有些问题没有最优子结构，那你就得递归去算，就得重复去算，就退化成回溯。
* 走棋盘：从最后往前面推
* 股票买卖：多个维度的信息需要记录


### 缓存替换算法 LRU cache 最近最少使用（双向链表实现）
### 缓存替换算法 LFU cache 最近最不频繁使用的，要记录使用次数（双向链表实现）


### BloomFilter
用于检索一个元素是否在一个集合中的算法，和缓存是互补的关系把集合中不在的数据挡掉（判断不在的话一定不在，判断在的话可能不在）。
空间效率和查询时间远远超过一般的算法，缺点是有一定的误识别率和删除困难。

* 原理：用一个映射函数做映射，一个很长的二进制向量来表示0和1，然后将经过映射的元素对应的向量位置为1。

* 实践
    1. 经过BloomFilter后，后面应该要接一个全表数据库来查数据；
    2. 避免Hash冲突，可以设置多个hash函数对数据进行定位，将多个结果对应的向量位置为1，这样计算出来的结果相同的概率就非常低了；

* 应用
    1. 防止数据库穿库：查询操作是磁盘I/O，代价高昂，如果大量的查询不存在的数据，就会严重影响数据库性能。使用布隆过滤器可以提前判断不存在的数据，避免不必要的磁盘操作。（HBase，Cassandra）
    2. 防止缓存穿透：查询时一般会先判断是否在缓存中，如果没有，就读DB，并放入缓存。如果有恶意请求，一直查询不存在的数据，例如查询用户abc的详细信息，而abc根本不存在。这时就可以使用布隆过滤器，例如请求用户abc的时候，先判断此用户是否存在，不存在就直接返回了，避免了数据库查询压力。
    3. 爬虫URL去重：避免爬取相同URL地址。
    4. 反垃圾邮件：从数十亿垃圾邮件列表中判断某邮箱是否为垃圾邮箱。





# 技巧

* 双指针
* 参数传值，而不用返回值来传（98）

## 模板
![面试四件套](src/main/resources/file/面试四件套.jpg)

![递归](src/main/resources/file/recursion.jpg)

![dfs](src/main/resources/file/dfs.jpg)

![bfs](src/main/resources/file/bfs.jpg)

![dp](src/main/resources/file/dp.jpg)

![binary](src/main/resources/file/binary.jpg)


# 二刷首选
* 无重复字符的最长子串（leetcode3）
* 递归  N皇后51 52 152
* 动态规划 322 72 

* 最大子序和（leetcode53）
* 三数之和（leetcode15，这道题当时字节跳动三面时被问到了，题目忘记了最优解法导致最后挂）
* 合并两个有序链表（leetcode21）
* 全排列（leetcode46，47）
* 二叉树中序后序的非递归
* 股票买卖的最佳时机（leetcode121，122）
* LRU缓存（leetcode146）
* k个一组反转链表（leetcode25）
* 用队列实现栈（leetcode225，232）
* 岛屿数量（leetcode200）
