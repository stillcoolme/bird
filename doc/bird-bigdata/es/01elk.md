## 概念

人家问es是什么？结合自己项目里面使用来说呗

[如何高逼格的给同事做 Elasticsearch 技术分享](https://mp.weixin.qq.com/s?__biz=MzA3MTUzOTcxOQ==&mid=2452966988&idx=1&sn=2b7227769be57b08d6f190134f7bea1f&scene=21#wechat_redirect)

![](https://raw.githubusercontent.com/janehzhang/mypic/master/2020/202003/es-mysql-compare.png)

![](https://img-blog.csdn.net/20160717132008382)

索引打开关闭，打开才缓存到内存才可以查数据。



## 初识搜索引擎

### 索引、分片

Elasticsearch 中的**数据会整理为索引**。

每个索引又由一个或多个分片组成。每个分片都是一个 Lucene 索引实例，可以将其视作一个独立的搜索引擎。

数据写到分片上之后，会定期发布到磁盘上**不可更改的新 Lucene 段**中，此时，数据便可用于查询了。这称为刷新。

分片是 Elasticsearch 在集群内分发数据的单位。Elasticsearch 在对数据进行再平衡（例如发生故障后）时移动分片的速度取决于分片的大小和数量，以及网络和磁盘性能。

[我应该设计多少分片](https://www.elastic.co/cn/blog/how-many-shards-should-i-have-in-my-elasticsearch-cluster?nsukey=EB%2BtCgHd6my4k5BptSe45EzhKue86h5teaT6mHtRUTv9wMEzkEMkYtrOz5LCy3poLIJY2aPhd3DQvv09BY8KPdwfuscVJ1y5mRBUmGYHJ6Pordh4o8EBejeH7ARChzH8BrlPTxUWvLxd0kVS1YqXHHC%2FfItNpexWZKnQFeTStrcVgFgdOtaP3Ei%2FxZ%2B2JbDb )

### 倒排索引

建立倒排索引的时候，会执行一个 normalization 操作，也就是说对拆分出的各个单词进行相应的处理，以提升后面搜索的时候能够搜索到相关联的文档的概率：时态的转换，单复数的转换，同义词的转换，大小写的转换

### 易忘记的概念

text 和 keyword：ElasticSearch 5.0以后，string类型有重大变更，移除了string类型，被拆分成两种新的数据类型: text 会分词，用于全文搜索的，而 keyword 不会被分词，用于关键词搜索。

match 和 termterm：查询的时候，match是模糊查询，有个评分过程会增加耗时，只要有的所查字段的文档都会被查出来。



## 索引管理

### 索引查询

```shell
# 查看所有索引
GET /_cat/indices
# 查询
GET /索引名/_search

# 创建索引，不指定映射类型
PUT /website/article/1
{
  "post_date": "2017-01-01",
  "title": "my first article",
  "content": "this is my first article in this website",
  "author_id": 11400
}
PUT /website/article/2
{
  "post_date": "2017-01-02",
  "title": "my second article",
  "content": "this is my second article in this website",
  "author_id": 11402
}
PUT /website/article/3
{
  "post_date": "2017-01-03",
  "title": "my third article",
  "content": "this is my third article in this website",
  "author_id": 11403
}

# 创建索引，自己指定映射类型
PUT /my_index
{
    "settings": { ... any settings ... },
    "mappings": {
        "type_one": { ... any mappings ... },
        "type_two": { ... any mappings ... },
        ...
    }
}

PUT /my_index
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "my_type": {
      "properties": {
        "my_field": {
          "type": "text"
        }
      }
    }
  }
}

# 修改索引
PUT /my_index/_settings
{
    "number_of_replicas": 1
}
# 删除索引
DELETE /my_index
DELETE /index_one,index_two
DELETE /index_*

# 查询索引
POST /website/_search
GET /website/article/_search?q=2017			3条结果             
GET /website/article/_search?q=2017-01-01        	3条结果
GET /website/article/_search?q=post_date:2017-01-01   	1条结果
GET /website/article/_search?q=post_date:2017         	1条结果

POST /_search
{
  "from":0,
  "size":10
}

POST /index名/_search
{
	"sort" : [{"field": "desc"}],
	// 只查某个字段
	"_source" : ["field"],
	"query" : {
		"match" : {
			"field" : "xxx"
		}
	}
}
```

### mapping

映射（mapping）机制用于进行字段类型确认，将每个字段匹配为一种确定的数据类型（number，booleans，date等）。

分析（analysis）机制用于进行全文文本（Full Text）的分词，以建立供搜索用的反向索引。

mapping不仅告诉ES一个field中是什么类型的值， 它还告诉ES如何索引数据以及数据是否能被搜索到。当你的查询没有返回相应的数据， 你的mapping很有可能有问题。当你拿不准的时候， 直接检查你的mapping。

**剖析mapping**

一个mapping由一个或多个analyzer组成， 一个analyzer又由一个或多个filter组成的。当ES索引文档的时候，它把字段中的内容传递给相应的analyzer，analyzer再传递给各自的filters。

filter的功能很容易理解：一个filter就是一个转换数据的方法， 输入一个字符串，这个方法返回另一个字符串，比如一个将字符串转为小写的方法就是一个filter很好的例子。

一个analyzer由一组顺序排列的filter组成，执行分析的过程就是按顺序一个filter一个filter依次调用， ES存储和索引最后得到的结果。默认的analyzer是标准analyzer, 这个标准analyzer有三个filter：token filter, lowercase filter和 stop token filter。

**dynamic mapping**

自动在建立index时创建对应的mapping，mapping中包含了每个field对应的数据类型，以及如何分词等设置；
因为 es 自动建立mapping的时候，设置了不同的field不同的data type。所以分词、搜索等行为是不一样的导致搜索结果为什么不一致。

* **不同的数据类型，可能有的是exact value，有的是full text**
* exact value，在建立倒排索引的时候，分词的时候，是将整个值一起作为一个关键词建立到倒排索引中的（要搜索2017-10-1 则需要完整输入2017-10-1）；full text，会经历各种各样的处理，分词，normaliztion 才会建立到倒排索引中

ElasticSearch 5.0以后，string类型有重大变更，移除了string类型，string字段被拆分成两种新的数据类型: text用于全文搜索的,而keyword用于关键词搜索。字符串将默认被同时映射成 text 和 keyword 类型，将会自动创建下面的动态映射(dynamic mappings):

```
"title" : {
	"type" : "text",
	"fields" : {
        "keyword" : {					// 自动生成一个与之对应的 .keyword 字段
            "type" : "keyword",
            "ignore_above" : 256
        }
	}
}
Text：会分词，然后进行索引，支持模糊、精确查询，不支持聚合
keyword：不进行分词，直接索引，全都支持
```



```shell
# 查看mapping
GET /website/_mapping

GET /_analyze
{
  "analyzer": "standard",
  "text": "Text to analyze"
}


# 创建时手动建立 mapping，或者新增field mapping，但是不能update field mapping
PUT /website
{
  "mappings": {
    "article": {
      "properties": {
        "author_id": {
          "type": "long"
        },
        "title": {
          "type": "text",
          "analyzer": "english"
        },
        "content": {
          "type": "text"
        },
        "post_date": {
          "type": "date"
        },
        "publisher_id": {
          "type": "text",
          "index": "not_analyzed"
        }
      }
    }
  }
}
=====================

// 添加新的字段
PUT /website/_mapping/article
{
  "properties" : {
    "new_field" : {
      "type" :    "string",
      "index":    "not_analyzed"
    }
  }
}
​```

# 测试mapping
​```
GET /website/_analyze
{
  "field": "content",
  "text": "my-dogs" 
}

GET website/_analyze
{
  "field": "new_field",
  "text": "my dogs"
}

{
  "error": {
    "root_cause": [
      {
        "type": "remote_transport_exception",
        "reason": "[4onsTYV][127.0.0.1:9300][indices:admin/analyze[s]]"
      }
    ],
    "type": "illegal_argument_exception",
    "reason": "Can't process field [new_field], Analysis requests are only supported on tokenized fields"
  },
  "status": 400
}
​```

```



**mapping复杂数据类型**

1、multivalue field

```
{ "tags": [ "tag1", "tag2" ]}
```

建立索引时与string是一样的，数据类型不能混

2、empty field

```
null，[]，[null]
```

3、object field

```
PUT /company/employee/1
{
  "address": {
    "country": "china",
    "province": "guangdong",
    "city": "guangzhou"
  },
  "name": "jack",
  "age": 27,
  "join_date": "2017-01-01"
}

address：就是object类型！
在es底层数据结构变成这样：
{
    "name":            [jack],
    "age":          [27],
    "join_date":      [2017-01-01],
    "address.country":         [china],
    "address.province":   [guangdong],
    "address.city":  [guangzhou]
}


{
    "authors": [
        { "age": 26, "name": "Jack White"},
        { "age": 55, "name": "Tom Jones"},
        { "age": 39, "name": "Kitty Smith"}
    ]
}
就变成这样列式的存储：
{
    "authors.age":    [26, 55, 39],
    "authors.name":   [jack, white, tom, jones, kitty, smith]
}
```



### query DSL搜索语法

2、Query DSL的基本语法

```shell
{
    QUERY_NAME: {
        ARGUMENT: VALUE,
        ARGUMENT: VALUE,...
    }
}

{
    QUERY_NAME: {
        FIELD_NAME: {
            ARGUMENT: VALUE,
            ARGUMENT: VALUE,...
       }
    }
}

# 例子
GET /test_index/_search 
{
  "query": {
    "match": {
      "test_field": "test"
    }
  }
}
```

3、如何组合多个搜索条件
bool，must，must_not，should，filter每个子查询都会计算一个document针对它的相关度分数，
然后bool综合所有分数合并为一个分数，当然filter是不会计算分数的

搜索需求：title必须包含elasticsearch，content可以包含elasticsearch也可以不包含，author_id必须不为111

```
GET /website/article/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": "elasticsearch"
          }
        }
      ],
      "should": [
        {
          "match": {
            "content": "elasticsearch"
          }
        }
      ],
      "must_not": [
        {
          "match": {
            "author_id": 111
          }
        }
      ]
    }
  }
}


GET /test_index/_search
{
    "query": {
            "bool": {
                "must": { "match":   { "name": "tom" }},
                "should": [
                    { "match":       { "hired": true }},
                    { "bool": {
                        "must":      { "match": { "personality": "good" }},
                        "must_not":  { "match": { "rude": true }}
                    }}
                ],
                "minimum_should_match": 1
            }
    }
}

{
    "bool": {
        "must":     { "match": { "title": "how to make millions" }},
        "must_not": { "match": { "tag":   "spam" }},
        "should": [
            { "match": { "tag": "starred" }}
        ],
        "filter": {
          "bool": { 
              "must": [
                  { "range": { "date": { "gte": "2014-01-01" }}},
                  { "range": { "price": { "lte": 29.99 }}}
              ],
              "must_not": [
                  { "term": { "category": "ebooks" }}
              ]
          }
        }
    }
}

3、multi match

GET /test_index/_search
{
  "query": {
    "multi_match": {
      "query": "test",
      "fields": ["test_field", "test_field1"]
    }
  }
}

4、range query

GET /company/employee/_search 
{
  "query": {
    "range": {
      "age": {
        "gte": 30
      }
    }
  }
}

5、term query
// term 形式搜索test hello，就是要匹配test hello，文档中只是test是搜不出来的。
GET /test_index/test_type/_search 
{
  "query": {
    "term": {
      "test_field": "test hello"
    }
  }
}
```



###  filter与query对比解密

1、filter与query示例

```
PUT /company/employee/2
{
  "address": {
    "country": "china",
    "province": "jiangsu",
    "city": "nanjing"
  },
  "name": "tom",
  "age": 30,
  "join_date": "2016-01-01"
}

PUT /company/employee/3
{
  "address": {
    "country": "china",
    "province": "shanxi",
    "city": "xian"
  },
  "name": "marry",
  "age": 35,
  "join_date": "2015-01-01"
}
```

搜索请求：年龄必须大于等于30，同时join_date必须是2016-01-01

```
GET /company/employee/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "join_date": "2016-01-01"
          }
        }
      ],
      "filter": {
        "range": {
          "age": {
            "gte": 30
          }
        }
      }
    }
  }
}
```

2、filter与query对比大解密

1. filter，仅仅只是按照搜索条件过滤出需要的数据而已，不计算任何相关度分数，不需要按照相关度分数进行排序，同时还有**内置的自动cache最常使用filter的数据**
2. query，会去计算每个document相对于搜索条件的相关度，并按照相关度进行排序，无法cache结果

＊　如果你是在进行搜索，需要将最匹配搜索条件的数据先返回，那么用query；
＊　如果你只是要根据一些条件筛选出一部分数据，不关注其排序，那么用filter
＊　你希望越符合这些搜索条件的document越排在前面返回，那么这些搜索条件要放在query中；
＊　如果你不希望一些搜索条件来影响你的document排序，那么就放在filter中即可







### 相关问题

* 深度search

[ElasticSearch分页查询详解-----------深度分页(from-size)和快照分页(scroll)](https://blog.csdn.net/YYDU_666/article/details/82792800)
[如何跳过es分页这个坑？](https://my.oschina.net/u/1787735/blog/3024051)



### 技巧

如何定位不合法的搜索以及其原因

一般用在那种特别复杂庞大的搜索下，比如你一下子写了上百行的搜索，这个时候可以先用validate api去验证一下，搜索是否合法

GET /test_index/test_type/_validate/query?explain
{
  "query": {
    "math": {
      "test_field": "test"
    }
  }
}

{
  "valid": false,
  "error": "org.elasticsearch.common.ParsingException: no [query] registered for [math]"
}



### query phase、 fetch phase 过程

1、query phase

（1）搜索请求发送到某一个coordinate node，构构建一个priority queue，长度以paging操作from和size为准，默认为10条，按照_score排序
（2）coordinate node将请求转发到所有shard，每个shard本地搜索，并构建一个本地的priority queue
（3）各个shard将自己的priority queue返回给coordinate node，并构建一个全局的priority queue

* replica shard如何提升搜索吞吐量

一次请求要打到所有shard的一个replica/primary上去，如果每个shard都有多个replica，
那么同时并发过来的搜索请求可以同时打到其他的replica上去

2、fetch phase工作流程

（1）coordinate node构建完priority queue之后，就发送**mget请求**去所有shard上获取对应的document
（2）各个shard将document返回给coordinate node
（3）coordinate node将合并后的document结果返回给client客户端

**搜索相关参数梳理以及bouncing results问题解决方案**

1、 preference 参数：决定了哪些shard会被用来执行搜索操作，不同的shard执行搜索导致 bouncing results问题（跳跃结果）：两个document排序，field值相同；不同的shard上，可能排序不同；

原因：搜索的时候，是轮询将搜索请求发送到每一个replica shard（primary shard），但是在不同的shard上，可能document的排序不同

解决方案：
将preference设置为一个字符串，比如说user_id，让每个user每次搜索的时候，都使用同一个replica shard去执行，就不会看到bouncing results了

2、routing，document文档路由，默认是_id路由，或者自己指定routing=user_id，这样的话可以让同一个user对应的数据到一个shard上去

4、search_type

default：query_then_fetch
dfs_query_then_fetch，可以提升revelance sort精准度






### doc value

在建立索引的时候，

* 一方面会建立倒排索引，以供**搜索**用；
* 一方面会建立正排索引，也就是doc values，以供**排序，聚合，过滤**等操作使用

```shell
doc1: { "name": "jack", "age": 27 }
doc2: { "name": "tom", "age": 30 }

// doc  value
document	name		age
doc1		jack		27
doc2		tom		    30	

// 如果 sort by age，用倒排索引就先根据age找到所有的doc，再进行排序就慢了。。
```



### 相关度评分TF&IDF算法


relevance score算法，能计算出一个索引中的文本与搜索文本，他们之间的关联匹配程度。
ES 使用的是 term frequency/inverse document frequency算法，简称为TF/IDF算法

* Term frequency：搜索文本中的各个词条在**field文本中出现了多少次，出现次数越多，就越相关**

* Inverse document frequency：搜索文本中的各个词条在**整个索引的所有文档中出现了多少次**，出现的**次数越多，就越不相关**

搜索请求：hello world

doc1：hello, today is very good
doc2：hi world, how are you

比如说，在index中有1万条document，hello这个单词在所有的document中，一共出现了1000次；world这个单词在所有的document中，一共出现了100次

doc2更相关

* Field-length norm：field长度，field越长，相关度越弱



## 内核原理

### 68 深度图解剖析document写入原理（buffer，segment，commit）
1. 数据先写入内存buffer
2. commit point
3. buffer中的数据写入新的index segment；
4. 等待在os cache中的index segment被fsync强制刷到磁盘上；
5. 新的index sgement被打开，供search使用；
6. buffer被清空。````

删除操作：
每次commit point时，会有一个.del文件，标记了哪些segment中的哪些document被标记为deleted了。
搜索的时候，会依次查询所有的segment，从旧的到新的，比如被修改过的document，在旧的segment中，会标记为deleted，在新的segment中会有其新的数据

==================

现有流程的问题：
* 每次都必须等待fsync将segment刷入磁盘，才能将segment打开供search使用，
* 这样从一个document写入，到它可以被搜索，可能会超过1分钟！！
* 主要瓶颈在于fsync实际发生磁盘IO写数据进磁盘，是很耗时的。

**refresh，写入流程别改进**
1. 数据写入buffer；
2. 每隔一定时间，buffer中的数据被写入segment文件，但是先写入os cache；
3. 只要segment写入os cache，那就直接打开供search使用，不立即执行commit。

* 数据写入os cache，并被打开供搜索的过程，叫做refresh，默认是每隔1秒refresh一次。
* 所以，es是近实时的，数据写入到可以被搜索，默认是1秒。

手动refresh，一般不需要手动执行，没必要，让es自己搞就可以了
```
POST /my_index/_refresh
```
如果我们时效性要求比较低，只要求一条数据写入es，一分钟以后才让我们搜索到就可以了，那么就可以调整refresh interval
```
PUT /my_index
{
  "settings": {
    "refresh_interval": "30s" 
  }
}
```


====================

**flush，再次优化的写入流程**

1. 数据写入buffer缓冲和translog日志文件；
2. 每隔一秒钟，buffer中的数据被写入新的segment file，并进入os cache，此时segment被打开并供search使用；
3. buffer被清空；
4. 重复1~3，新的segment不断添加，buffer不断被清空，而translog中的数据不断累加；
5. 当translog长度达到一定程度的时候，**commit操作发生**， **进行flush操作。**
  1. buffer中的所有数据写入一个新的segment，并写入os cache，打开供使用
  2. buffer被清空
  3. 一个commit ponit被写入磁盘，标明了所有的index segment
  4. filesystem cache中的所有index segment file缓存数据，被fsync强行刷到磁盘上
  5. 现有的translog被清空，创建一个新的translog

基于translog和commit point，如何进行数据恢复？

fsync+清空translog，就是flush，默认每隔30分钟flush一次，或者当translog过大的时候，也会flush

POST /my_index/_flush，一般来说别手动flush，让它自动执行就可以了

translog，每隔5秒被fsync一次到磁盘上。
在一次增删改操作之后，当fsync在primary shard和replica shard都成功之后，那次增删改操作才会成功

但是这种在一次增删改时强行fsync translog可能会导致部分操作比较耗时，也可以允许部分数据丢失，设置异步fsync translog
```
PUT /my_index/_settings
{
    "index.translog.durability": "async",
    "index.translog.sync_interval": "5s"
}
```

**segment merge，最后优化**

每秒一个segment file，文件过多，而且每次search都要搜索所有的segment，很耗时

默认会在后台执行segment merge操作，在merge的时候，被标记为deleted的document也会被彻底物理删除

每次merge操作的执行流程:
1. 选择一些有相似大小的segment，merge成一个大的segment
2. 将新的segment flush到磁盘上去
3. 写一个新的commit point，包括了新的segment，并且排除旧的那些segment
4. 将新的segment打开供搜索
5. 将旧的segment删除
尽量不要手动执行，让它自动默认执行就可以了
```
POST /my_index/_optimize?max_num_segments=1
```


### 67 倒排索引组成结构以及其索引可变原因揭秘
```
// 其实不像下面这样这么简单
word		doc1		doc2
dog		     *		       *
hello		 *
you				           *
```
* 倒排索引的结构
1. 包含这个关键词的document list
2. 包含这个关键词的所有document的数量：IDF（inverse document frequency）
3. 这个关键词在每个document中出现的次数：TF（term frequency）
4. 这个关键词在这个document中的次序
5. 每个document的长度：length norm
6. 包含这个关键词的所有document的平均长度


* 倒排索引不可变的好处
1. 不需要锁，提升并发能力，避免锁的问题
2. 数据不变，一直保存在os cache中，只要cache内存足够
3. filter cache一直驻留在内存，因为数据不变
4. 可以压缩，节省cpu和io开销

倒排索引不可变的坏处：每次都要重新构建整个索引






## 索引管理

### 66 基于scoll+bulk+索引别名实现零停机重建索引

1、重建索引

* 一个field的设置是不能被修改的，如果要修改一个Field
* 应该重新按照新的mapping，建立一个index，然后将数据批量查询出来，重新用bulk api写入index中。

批量查询的时候，建议采用scroll api，并且采用多线程并发的方式来reindex数据，每次scoll就查询指定日期的一段数据，交给一个线程即可

（1）一开始，依靠dynamic mapping，插入数据，但是不小心有些数据是2017-01-01这种日期格式的，所以title这种field被自动映射为了date类型，实际上它应该是string类型的
```
PUT /my_index/my_type/3
{
  "title": "2017-01-03"
}

{
  "my_index": {
    "mappings": {
      "my_type": {
        "properties": {
          "title": {
            "type": "date"
          }
        }
      }
    }
  }
}
```
（2）当后期向索引中加入string类型的title值的时候，就会报错
```
PUT /my_index/my_type/4
{
  "title": "my first article"
}

{
  "error": {
    "root_cause": [
      {
        "type": "mapper_parsing_exception",
        "reason": "failed to parse [title]"
      }
    ],
    "type": "mapper_parsing_exception",
    "reason": "failed to parse [title]",
    "caused_by": {
      "type": "illegal_argument_exception",
      "reason": "Invalid format: \"my first article\""
    }
  },
  "status": 400
}
```
（3）如果此时想修改title的类型，是不可能的
```
PUT /my_index/_mapping/my_type
{
  "properties": {
    "title": {
      "type": "text"
    }
  }
}

{
  "error": {
    "root_cause": [
      {
        "type": "illegal_argument_exception",
        "reason": "mapper [title] of different type, current_type [date], merged_type [text]"
      }
    ],
    "type": "illegal_argument_exception",
    "reason": "mapper [title] of different type, current_type [date], merged_type [text]"
  },
  "status": 400
}
```
（4）此时，唯一的办法，就是进行reindex，也就是说，重新建立一个索引，将旧索引的数据查询出来，再导入新索引

（5）如果说旧索引的名字，是old_index，新索引的名字是new_index，终端java应用，已经在使用old_index在操作了，难道还要去停止java应用，修改使用的index为new_index，才重新启动java应用吗？
这个过程中，就会导致java应用停机，可用性降低

（6）所以说，给java应用一个别名，这个别名是指向旧索引的，java应用先用着，java应用先用goods_index alias来操作，此时实际指向的是旧的my_index
```
PUT /my_index/_alias/goods_index
```
（7）新建一个index，**手动mapping，调整其title的类型为string**
```
PUT /my_index_new
{
  "mappings": {
    "my_type": {
      "properties": {
        "title": {
          "type": "text"
        }
      }
    }
  }
}
```
（8）使用scroll api将数据批量查询出来
```
GET /my_index/_search?scroll=1m
{
    "query": {
        "match_all": {}
    },
    "sort": ["_doc"],
    "size":  1
}

{
  "_scroll_id": "DnF1ZXJ5VGhlbkZldGNoBQAAAAAAADpAFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAA6QRY0b25zVFlWWlRqR3ZJajlfc3BXejJ3AAAAAAAAOkIWNG9uc1RZVlpUakd2SWo5X3NwV3oydwAAAAAAADpDFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAA6RBY0b25zVFlWWlRqR3ZJajlfc3BXejJ3",
  "took": 1,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": null,
    "hits": [
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "2",
        "_score": null,
        "_source": {
          "title": "2017-01-02"
        },
        "sort": [
          0
        ]
      }
    ]
  }
}
```
（9）采用bulk api将scoll查出来的一批数据，批量写入新索引
```
POST /_bulk
{ "index":  { "_index": "my_index_new", "_type": "my_type", "_id": "2" }}
{ "title":    "2017-01-02" }
```
（10）反复循环8~9，查询一批又一批的数据出来，采取bulk api将每一批数据批量写入新索引

（11）将goods_index alias切换到my_index_new上去，java应用会直接通过index别名使用新的索引中的数据，java应用程序不需要停机，零提交，高可用
```
POST /_aliases
{
    "actions": [
        { "remove": { "index": "my_index", "alias": "goods_index" }},
        { "add":    { "index": "my_index_new", "alias": "goods_index" }}
    ]
}
```
（12）直接通过goods_index别名来查询，是否ok
```
GET /goods_index/my_type/_search
```
2、基于alias对client透明切换index

PUT /my_index_v1/_alias/my_index

client对my_index进行操作

reindex操作，完成之后，切换v1到v2

POST /_aliases
{
    "actions": [
        { "remove": { "index": "my_index_v1", "alias": "my_index" }},
        { "add":    { "index": "my_index_v2", "alias": "my_index" }}
    ]
}



### 65 定制化自己的dynamic mapping策略
1、定制dynamic策略
* true：遇到陌生字段，就进行dynamic mapping
* false：遇到陌生字段，就忽略
* strict：遇到陌生字段，就报错

```
PUT /my_index
{
  "mappings": {
    "my_type": {
      "dynamic": "strict",
      "properties": {
        "title": {
          "type": "text"
        },
        "address": {
          "type": "object",
          "dynamic": "true"
        }
      }
    }
  }
}

PUT /my_index/my_type/1
{
  "title": "my article",
  "content": "this is my article",
  "address": {
    "province": "guangdong",
    "city": "guangzhou"
  }
}

{
  "error": {
    "root_cause": [
      {
        "type": "strict_dynamic_mapping_exception",
        "reason": "mapping set to strict, dynamic introduction of [content] within [my_type] is not allowed"
      }
    ],
    "type": "strict_dynamic_mapping_exception",
    "reason": "mapping set to strict, dynamic introduction of [content] within [my_type] is not allowed"
  },
  "status": 400
}

PUT /my_index/my_type/1
{
  "title": "my article",
  "address": {
    "province": "guangdong",
    "city": "guangzhou"
  }
}

GET /my_index/_mapping/my_type

{
  "my_index": {
    "mappings": {
      "my_type": {
        "dynamic": "strict",
        "properties": {
          "address": {
            "dynamic": "true",
            "properties": {
              "city": {
                "type": "text",
                "fields": {
                  "keyword": {
                    "type": "keyword",
                    "ignore_above": 256
                  }
                }
              },
              "province": {
                "type": "text",
                "fields": {
                  "keyword": {
                    "type": "keyword",
                    "ignore_above": 256
                  }
                }
              }
            }
          },
          "title": {
            "type": "text"
          }
        }
      }
    }
  }
}
```
2、定制dynamic mapping策略

（1）date_detection

默认会按照一定格式识别date，比如yyyy-MM-dd。

但是如果某个field先过来一个2017-01-01的值，就会被自动dynamic mapping成date，

后面如果再来一个"hello world"之类的值，就会报错。

可以手动关闭某个type的date_detection，如果有需要，自己手动指定某个field为date类型。

PUT /my_index/_mapping/my_type
{
    "date_detection": false
}

（2）定制自己的dynamic mapping template（type level）

PUT /my_index
{
    "mappings": {
        "my_type": {
            "dynamic_templates": [
                { "en": {
                      "match":              "*_en", 
                      "match_mapping_type": "string",
                      "mapping": {
                          "type":           "string",
                          "analyzer":       "english"
                      }
                }}
            ]
}}}

PUT /my_index/my_type/1
{
  "title": "this is my first article"
}

PUT /my_index/my_type/2
{
  "title_en": "this is my first article"
}

title没有匹配到任何的dynamic模板，默认就是standard分词器，不会过滤停用词，is会进入倒排索引，用is来搜索是可以搜索到的
title_en匹配到了dynamic模板，就是english分词器，会过滤停用词，is这种停用词就会被过滤掉，用is来搜索就搜索不到了

（3）定制自己的default mapping template（index level）

PUT /my_index
{
    "mappings": {
        "_default_": {
            "_all": { "enabled":  false }
        },
        "blog": {
            "_all": { "enabled":  true  }
        }
    }
}




### 64 mapping root object深入剖析
1、root object

就是某个type对应的mapping json，包括了properties，metadata（_id，_source，_type），settings（analyzer），其他settings（比如include_in_all）

PUT /my_index
{
  "mappings": {
    "my_type": {
      "properties": {}
    }
  }
}

2、properties

type，index，analyzer

PUT /my_index/_mapping/my_type
{
  "properties": {
    "title": {
      "type": "text"
    }
  }
}

3、_source

好处

（1）查询的时候，直接可以拿到完整的document，不需要先拿document id，再发送一次请求拿document
（2）partial update基于_source实现
（3）reindex时，直接基于_source实现，不需要从数据库（或者其他外部存储）查询数据再修改
（4）可以基于_source定制返回field
（5）debug query更容易，因为可以直接看到_source

如果不需要上述好处，可以禁用_source

PUT /my_index/_mapping/my_type2
{
  "_source": {"enabled": false}
}


4、_all

将所有field打包在一起，作为一个_all field，建立索引。没指定任何field进行搜索时，就是使用_all field在搜索。

PUT /my_index/_mapping/my_type3
{
  "_all": {"enabled": false}
}

也可以在field级别设置include_in_all field，设置是否要将field的值包含在_all field中

PUT /my_index/_mapping/my_type4
{
  "properties": {
    "my_field": {
      "type": "text",
      "include_in_all": false
    }
  }
}

5、标识性metadata

_index，_type，_id


### 63 深入探秘type底层数据结构
type，是一个index中用来区分类似的数据的，类似的数据但是可能有不同的fields，而且有不同的属性来控制索引建立、分词器

field的value，在底层的lucene中是没有type的概念的，lucene建立索引的时候，全部是opaque bytes类型，不区分类型的

* 在document中，实际上将type作为一个document的field来存储，即_type field，

* es通过_type来进行type的过滤和筛选

一个index中的多个type，实际上是放在一起存储的，

因此一个index下，不能有多个type重名，而类型或者其他设置不同的，因为那样是无法处理的:
```
{
   "ecommerce": {
      "mappings": {
      	 // elactronic_goods 和 fresh_goods 就是 _type
         "elactronic_goods": {
            "properties": {
               "name": {
                  "type": "string",
               },
               "price": {
                  "type": "double"
               },
	       "service_period": {
		   "type": "string"
	       }			
            }
         },
         "fresh_goods": {
            "properties": {
               "name": {
                  "type": "string",
               },
               "price": {
                  "type": "double"
               },
	       "eat_period": {
		  "type": "string"
	       }
            }
         }
      }
   }
}

{
  "name": "geli kongtiao",
  "price": 1999.0,
  "service_period": "one year"
}

{
  "name": "aozhou dalongxia",
  "price": 199.0,
  "eat_period": "one week"
}
```
在底层的存储是这样子的:
```
{
   "ecommerce": {
      "mappings": {
        "_type": {
          "type": "string",
          "index": "not_analyzed"
        },
        "name": {
          "type": "string"
        }
        "price": {
          "type": "double"
        }
        "service_period": {
          "type": "string"
        }
        "eat_period": {
          "type": "string"
        }
      }
   }
}

{
  "_type": "elactronic_goods",
  "name": "geli kongtiao",
  "price": 1999.0,
  "service_period": "one year",
  "eat_period": ""
}

{
  "_type": "fresh_goods",
  "name": "aozhou dalongxia",
  "price": 199.0,
  "service_period": "",
  "eat_period": "one week"
}
```

最佳实践，
* 将类似结构的type放在一个index下，这些type应该有多个field是相同的；
* 如果将两个type的field完全不同，放在一个index下，那么每条数据都至少有一半的field在底层的lucene中是空值，会有严重的性能问题。



### 62 自定义分词器

1、默认的分词器 standard

standard tokenizer：以单词边界进行切分
standard token filter：什么都不做
lowercase token filter：将所有字母转换为小写
stop token filer（默认被禁用）：移除停用词，比如a the it等等

2、修改分词器的设置

启用english停用词 token filter
```
PUT /my_index
{
  "settings": {
    "analysis": {
      "analyzer": {
        "es_std": {
          "type": "standard",
          "stopwords": "_english_"
        }
      }
    }
  }
}

GET /my_index/_analyze
{
  "analyzer": "standard", 
  "text": "a dog is in the house"
}

GET /my_index/_analyze
{
  "analyzer": "es_std",
  "text":"a dog is in the house"
}
```
3、定制化自己的分词器
```
PUT /my_index
{
  "settings": {
    "analysis": {
      "char_filter": {
        "&_to_and": {
          "type": "mapping",
          "mappings": ["&=> and"]
        }
      },
      "filter": {
        "my_stopwords": {
          "type": "stop",
          "stopwords": ["the", "a"]
        }
      },
      "analyzer": {
        "my_analyzer": {
          "type": "custom",
          "char_filter": ["html_strip", "&_to_and"],
          "tokenizer": "standard",
          "filter": ["lowercase", "my_stopwords"]
        }
      }
    }
  }
}

GET /my_index/_analyze
{
  "text": "tom&jerry are a friend in the house, <a>, HAHA!!",
  "analyzer": "my_analyzer"
}

PUT /my_index/_mapping/my_type
{
  "properties": {
    "content": {
      "type": "text",
      "analyzer": "my_analyzer"
    }
  }
}

```

### 60 scoll技术
背景：如果一次性要查出来比如10万条数据性能会很差，一般会采取用scoll滚动查询，一批一批的查，直到所有数据都查询完处理完

scoll搜索
* 在第一次搜索的时候，保存一个当时的视图快照，
* 之后只会基于该旧的视图快照提供数据搜索，如果这个期间数据变更，是不会让用户看到的
* 采用基于_doc进行排序的方式，性能较高
* 每次发送scroll请求，我们还需要指定一个scoll参数，指定一个时间窗口，每次搜索请求只要在这个时间窗口内能完成就可以了

```
GET /test_index/test_type/_search?scroll=1m
{
  "query": {
    "match_all": {}
  },
  "sort": [ "_doc" ],
  "size": 3
}

{
  "_scroll_id": "DnF1ZXJ5VGhlbkZldGNoBQAAAAAAACxeFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAAsYBY0b25zVFlWWlRqR3ZJajlfc3BXejJ3AAAAAAAALF8WNG9uc1RZVlpUakd2SWo5X3NwV3oydwAAAAAAACxhFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAAsYhY0b25zVFlWWlRqR3ZJajlfc3BXejJ3",
  "took": 5,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 10,
    "max_score": null,
    "hits": [
      {
        "_index": "test_index",
        "_type": "test_type",
        "_id": "8",
        "_score": null,
        "_source": {
          "test_field": "test client 2"
        },
        "sort": [
          0
        ]
      },
      {
        "_index": "test_index",
        "_type": "test_type",
        "_id": "6",
        "_score": null,
        "_source": {
          "test_field": "tes test"
        },
        "sort": [
          0
        ]
      },
      {
        "_index": "test_index",
        "_type": "test_type",
        "_id": "AVp4RN0bhjxldOOnBxaE",
        "_score": null,
        "_source": {
          "test_content": "my test"
        },
        "sort": [
          0
        ]
      }
    ]
  }
}
```
获得的结果会有一个scoll_id，下一次再发送scoll请求的时候，必须带上这个scoll_id
```
GET /_search/scroll
{
    "scroll": "1m", 
    "scroll_id" : "DnF1ZXJ5VGhlbkZldGNoBQAAAAAAACxeFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAAsYBY0b25zVFlWWlRqR3ZJajlfc3BXejJ3AAAAAAAALF8WNG9uc1RZVlpUakd2SWo5X3NwV3oydwAAAAAAACxhFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAAsYhY0b25zVFlWWlRqR3ZJajlfc3BXejJ3"
}

11,4,7
3,2,1
20
```
scoll，看起来挺像分页的，但是其实使用场景不一样。

分页主要是用来一页一页搜索，给用户看的；

scoll主要是用来一批一批检索数据，让系统进行处理的

## 系统优化

### 堆内存

ES 可用的堆越多，可用于缓存的内存就越多。但太多的堆内存可能会长时间垃圾收集暂停。

将Xmx设置为不超过物理内存的50％，以确保有足够的物理内存留给内核文件系统缓存。同时不要将Xmx设置为JVM超过32GB。宿主机内存大小的一半和31GB，取最小值。

同时要关闭交换空间  swapoff -a

**堆内存为什么不能超过物理机内存的一半？**

因为Lucene 也是重要的内存使用者。Lucene旨在利用底层操作系统来缓存内存中的数据结构。 Lucene段(segment)存储在单个文件中。因为段是一成不变的，所以这些文件永远不会改变。这使得它们非常容易缓存，并且底层操作系统将愉快地将热段（hot segments）保留在内存中以便更快地访问。

**堆内存为什么不能超过32GB？**

JVM 在内存小于 32 GB 的时候会采用一个内存对象指针压缩技术。

对于 32 位的系统，意味着堆内存大小最大为 4 GB。对于 64 位的系统， 可以使用更大的内存，但是 64 位的指针意味着更大的浪费，因为你的指针本身大了。更糟糕的是， 更大的指针在主内存和各级缓存（例如 LLC，L1 等）之间移动数据的时候，会占用更多的带宽。

Java 使用一个叫作 内存指针压缩（compressed oops）的技术来解决这个问题。它的指针不再表示对象在内存中的精确位置，而是表示 偏移量 。这意味着 32 位的指针可以引用 40 亿个 对象 ， 而不是 40 亿个字节。最终， 也就是说堆内存增长到 32 GB 的物理内存，也可以用 32 位的指针表示。

一旦你越过那个神奇的 ~32 GB 的边界，指针就会切回普通对象的指针。每个对象的指针都变长了，就会使用更多的 CPU 内存带宽，也就是说你实际上失去了更多的内存。事实上，当内存到达 40–50 GB 的时候，有效内存才相当于使用内存对象指针压缩技术时候的 32 GB 内存。

这段描述的意思就是说：即便你有足够的内存，也尽量不要 超过 32 GB。因为它浪费了内存，降低了 CPU 的性能，还要让 GC 应对大内存。



## 查询优化

归根结底，你要让 es 性能要好，最佳的情况下，就是你的机器的内存，至少可以容纳你的总数据量的一半。

根据我们自己的生产环境实践经验，最佳的情况下，是仅仅在 es 中就存少量的数据，就是你要**用来搜索的那些索引**，如果内存留给 `filesystem cache` 的是 100G，那么你就将索引数据控制在 `100G` 以内，这样的话，你的数据几乎全部走内存来搜索，性能非常之高，一般可以在 1 秒以内。

比如说你现在有一行数据。`id,name,age ....` 30 个字段。但是你现在搜索，只需要根据 `id,name,age` 三个字段来搜索。如果你傻乎乎往 es 里写入一行数据所有的字段，结果硬是占据了 es 机器上的 `filesystem cache` 的空间。其实，仅仅写入 es 中要用来检索的**少数几个字段**就可以了，比如说就写入 es `id,name,age` 三个字段，然后你可以把其他的字段数据存在 mysql/hbase 里，我们一般是建议用 `es + hbase` 这么一个架构。

hbase 的特点是**适用于海量数据的在线存储**，就是对 hbase 可以写入海量数据，但是不要做复杂的搜索，做很简单的一些根据 id 或者范围进行查询的这么一个操作就可以了。从 es 中根据 name 和 age 去搜索，拿到的结果可能就 20 个 `doc id`，然后根据 `doc id` 到 hbase 里去查询每个 `doc id` 对应的**完整的数据**，给查出来，再返回给前端。

### 数据预热

假如说，哪怕是你就按照上述的方案去做了，es 集群中每个机器写入的数据量还是超过了 `filesystem cache` 一倍，比如说你写入一台机器 60G 数据，结果 `filesystem cache` 就 30G，还是有 30G 数据留在了磁盘上。

其实可以做**数据预热**。

举个例子，拿微博来说，你可以把一些大V，平时看的人很多的数据，你自己提前后台搞个系统，每隔一会儿，自己的后台系统去搜索一下热数据，刷到 `filesystem cache` 里去，后面用户实际上来看这个热数据的时候，他们就是直接从内存里搜索了，很快。

或者是电商，你可以将平时查看最多的一些商品，比如说 iphone 8，热数据提前后台搞个程序，每隔 1 分钟自己主动访问一次，刷到 `filesystem cache` 里去。

对于那些你觉得比较热的、经常会有人访问的数据，最好**做一个专门的缓存预热子系统**，就是对热数据每隔一段时间，就提前访问一下，让数据进入 `filesystem cache` 里面去。这样下次别人访问的时候，性能一定会好很多。

### 冷热分离

es 可以做类似于 mysql 的水平拆分，就是说将大量的访问很少、频率很低的数据，单独写一个索引，然后将访问很频繁的热数据单独写一个索引。最好是将**冷数据写入一个索引中，然后热数据写入另外一个索引中**，这样可以确保热数据在被预热之后，尽量都让他们留在 `filesystem os cache` 里，**别让冷数据给冲刷掉**。

你看，假设你有 6 台机器，2 个索引，一个放冷数据，一个放热数据，每个索引 3 个 shard。3 台机器放热数据 index，另外 3 台机器放冷数据 index。然后这样的话，你大量的时间是在访问热数据 index，热数据可能就占总数据量的 10%，此时数据量很少，几乎全都保留在 `filesystem cache` 里面了，就可以确保热数据的访问性能是很高的。但是对于冷数据而言，是在别的 index 里的，跟热数据 index 不在相同的机器上，大家互相之间都没什么联系了。如果有人访问冷数据，可能大量数据是在磁盘上的，此时性能差点，就 10% 的人去访问冷数据，90% 的人在访问热数据，也无所谓了。

### document 模型设计

对于 MySQL，我们经常有一些复杂的关联查询。在 es 里该怎么玩儿，es 里面的复杂的关联查询尽量别用，一旦用了性能一般都不太好。最好是先在 Java 系统里就完成关联，将关联好的数据直接写入 es 中。搜索的时候，就不需要利用 es 的搜索语法来完成 join 之类的关联搜索了。

document 模型设计是非常重要的。es 能支持的操作就那么多，不要考虑用 es 做一些它不好操作的事情。如果真的有那种操作，尽量在 document 模型设计的时候，写入的时候就完成。另外对于一些太复杂的操作，比如 join/nested/parent-child 搜索都要尽量避免，性能都很差的。

### 分页性能优化

你要查第 100 页的 10 条数据，不可能说从 5 个 shard，每个 shard 就查 2 条数据，最后到协调节点合并成 10 条数据吧？你**必须**得从每个 shard 都查 1000 条数据过来，然后根据你的需求进行排序、筛选等等操作，最后再次分页，拿到里面第 100 页的数据。你翻页的时候，翻的越深，每个 shard 返回的数据就越多，而且协调节点处理的时间越长，非常坑爹。所以用 es 做分页的时候，你会发现越翻到后面，就越是慢。

**类似于 app 里的推荐商品不断下拉出来一页一页的**

类似于微博中，下拉刷微博，刷出来一页一页的，你可以用 `scroll api`。

scroll 会一次性给你生成**所有数据的一个快照**，然后每次滑动向后翻页就是通过**游标** `scroll_id` 移动，获取下一页下一页这样子，性能会比上面说的那种分页性能要高很多很多，基本上都是毫秒级的。

但是，这个适合于那种类似微博下拉翻页的，**不能随意跳到任何一页的场景**。也就是说，你不能先进入第 10 页，然后去第 120 页，然后又回到第 58 页，不能随意乱跳页。所以现在很多产品，都是不允许你随意翻页的，app，也有一些网站，做的就是你只能往下拉，一页一页的翻。

初始化时必须指定 `scroll` 参数，告诉 es 要保存此次搜索的上下文多长时间。你需要确保用户不会持续不断翻页翻几个小时，否则可能因为超时而失败。

除了用 `scroll api`，你也可以用 `search_after` 来做，适合并发，`search_after` 的思想是使用前一页的结果来帮助检索下一页的数据，显然，这种方式也不允许你随意翻页，你只能一页页往后翻。初始化时，需要使用一个唯一值的字段作为 sort 字段。

 https://toutiao.io/posts/yzh0h7/preview 





### 索引别名

1. 用于作为几个拆分的索引的总查询名
2. 方便应对新需求对索引的修改，迁移索引只要将别名指向新索引就行了

https://www.cnblogs.com/jajian/p/10152681.html



## 异常

ElasticSearch 7.x 默认不再支持指定索引类型，建索引的时候只要指定索引名，不然会报错：`Root mapping definition has unsupported parameters`

```json
put /store_index
{
    "settings":{    
    "number_of_shards" : 3,   
    "number_of_replicas" : 0    
    },    
     "mappings":{    
      "books":{                 //  7.x 不用再指定这个了！
        "properties":{        
            "title":{"type":"text"},
            "name":{"type":"text","index":false},
            "publish_date":{"type":"date","index":false},           
            "price":{"type":"double"},           
            "number":{
                "type":"object",
                "dynamic":true
            }
        }
      }
     }
 }
```

