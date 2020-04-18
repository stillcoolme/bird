[toc]

## 实战IT技术论坛

IT技术论坛中相关的数据，会在es中建立数据的索引

### 2 实战使用term filter来搜索数据（动态mapping）

#### 调整动态mapping

1、根据用户ID、是否隐藏、帖子ID、发帖日期来搜索帖子

（1）插入一些测试帖子数据， 动态mapping
```
POST /forum/article/_bulk
{ "index": { "_id": 1 }}
{ "articleID" : "XHDK-A-1293-#fJ3", "userID" : 1, "hidden": false, "postDate": "2017-01-01" }
{ "index": { "_id": 2 }}
{ "articleID" : "KDKE-B-9947-#kL5", "userID" : 1, "hidden": false, "postDate": "2017-01-02" }
{ "index": { "_id": 3 }}
{ "articleID" : "JODL-X-1937-#pV7", "userID" : 2, "hidden": false, "postDate": "2017-01-01" }
{ "index": { "_id": 4 }}
{ "articleID" : "QQPX-R-3956-#aD8", "userID" : 2, "hidden": true, "postDate": "2017-01-02" }

// 查看mapping类型！！！！！！！！！！！！！！

GET /forum/_mapping/article

{
  "forum": {
    "mappings": {
      "article": {
        "properties": {
          "articleID": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "hidden": {
            "type": "boolean"
          },
          "postDate": {
            "type": "date"
          },
          "userID": {
            "type": "long"
          }
        }
      }
    }
  }
}
```

正常添加数据，
* ES会自动识别所添加数据的类型，并 **动态mapping** 为它分配类型;
* 并为每个特殊类型分别建索引，以用来进行之后的搜索。

ElasticSearch 5.0以后，string类型有重大变更，移除了string类型，string字段被拆分成两种新的数据类型: 

* 全文文本类型text：用于全文搜索的。这个的检索不是直接给出是否匹配，而是检索出相似度，并按照相似度由高到低返回结果。
* 准确数据类型keyword：用于关键词搜索。直接被存储为了二进制，检索时我们直接匹配，不匹配就返回false

动态映射(dynamic mappings)，可以看到 articleID，type=text，默认会设置两个field，
* 一个是field本身，比如articleID，就是分词的；
* 另一个的话，就是keyword字段：articleID.keyword，默认不分词，会最多保留256个字符。


（2）根据用户ID搜索帖子
```
GET /forum/article/_search
{
    "query" : {
        // 相关度分数都是 1，不关心
        "constant_score" : { 
            "filter" : {
                // 对搜索文本不分词，直接拿去倒排索引中匹配，你输入的是什么，就去匹配什么
                "term" : { 
                    "userID" : 1
                }
            }
        }
    }
}
```

（5）根据帖子ID搜索帖子
```
GET /forum/article/_search
{
    "query" : {
        "constant_score" : { 
            "filter" : {
                "term" : { 
                    // articleID字段被分词了，直接搜搜不出来的，要articleID.keyword
                    "articleID" : "XHDK-A-1293-#fJ3"
                }
            }
        }
    }
}

{
  "took": 1,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 0,
    "max_score": null,
    "hits": []
  }
}

GET /forum/article/_search
{
    "query" : {
        "constant_score" : { 
            "filter" : {
                "term" : { 
                    "articleID.keyword" : "XHDK-A-1293-#fJ3"
                }
            }
        }
    }
}

{
  "took": 2,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 1,
    "max_score": 1,
    "hits": [
      {
        "_index": "forum",
        "_type": "article",
        "_id": "1",
        "_score": 1,
        "_source": {
          "articleID": "XHDK-A-1293-#fJ3",
          "userID": 1,
          "hidden": false,
          "postDate": "2017-01-01"
        }
      }
    ]
  }
}
```

=================
因为动态mapping。
* articleID 是分词的，所以搜不到。
* articleID.keyword，是不分词的，保留256个字符最多，直接一个字符串放入倒排索引中。

**所以term filter，对text过滤，考虑使用内置的field.keyword来进行全匹配才准确。**

所以尽可能还是自己去手动建立索引，指定not_analyzed。
在最新版本的es中，不需要指定not_analyzed也可以，将type=keyword即可。


（6）查看分词
```
GET /forum/_analyze
{
  "field": "articleID",
  "text": "XHDK-A-1293-#fJ3"
}
```
默认是analyzed的text类型的field，建立倒排索引的时候，就会对所有的articleID分词，分词以后，原本的articleID就没有了，只有分词后的各个word存在于倒排索引中。

term，是不对搜索文本分词的，XHDK-A-1293-#fJ3 --> XHDK-A-1293-#fJ3；

但是articleID建立索引的时候，XHDK-A-1293-#fJ3 --> xhdk，a，1293，fj3


（7）重建索引，articleID不分词
```
DELETE /forum

// 5.x版本的
PUT /forum
{
  "mappings": {
    "article": {
      "properties": {
        "articleID": {
          // 指定keyword就不进行分词了。。
          "type": "keyword"
        }
      }
    }
  }
}

// 现在7.x版本移除了 type，指定article的话会报错。。
// 后面都默认不要 article这个type了。。。。。。。。。
PUT /forum/
{
  "mappings": {
      "properties": {
        "articleID": {
          "type": "keyword"
        }
      }
  }
}

// 查看分词会发现 articleId是不分词的了
GET /forum/_analyze
{
  "field": "articleID",
  "text": "XHDK-A-1293-#fJ3"
}

// 查看mapping类型可以看到 articleId是keyword的
GET /forum/_mapping/

// 添加数据
POST /forum/article/_bulk
{ "index": { "_id": 1 }}
{ "articleID" : "XHDK-A-1293-#fJ3", "userID" : 1, "hidden": false, "postDate": "2017-01-01" }
{ "index": { "_id": 2 }}
{ "articleID" : "KDKE-B-9947-#kL5", "userID" : 1, "hidden": false, "postDate": "2017-01-02" }
{ "index": { "_id": 3 }}
{ "articleID" : "JODL-X-1937-#pV7", "userID" : 2, "hidden": false, "postDate": "2017-01-01" }
{ "index": { "_id": 4 }}
{ "articleID" : "QQPX-R-3956-#aD8", "userID" : 2, "hidden": true, "postDate": "2017-01-02" }
```

（8）重新根据帖子ID和发帖日期进行搜索，就能直接通过articleID字段搜索出来了。XHDK-A-1293-#fJ3是完整存储的没有进行分词
```
GET /forum/article/_search
{
    "query" : {
        "constant_score" : { 
            "filter" : {
                "term" : { 
                    "articleID" : "XHDK-A-1293-#fJ3"
                }
            }
        }
    }
}
```

#### 多条件查询

1、搜索发帖日期为2017-01-01，或者帖子ID为XHDK-A-1293-#fJ3的帖子，同时要求帖子的发帖日期绝对不为2017-01-02

select *
from forum.article
where (post_date='2017-01-01' or article_id='XHDK-A-1293-#fJ3')
and post_date!='2017-01-02'

GET /forum/article/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "bool": {
          "should": [
            {"term": { "postDate": "2017-01-01" }},
            {"term": {"articleID": "XHDK-A-1293-#fJ3"}}
          ],
          "must_not": {
            "term": {
              "postDate": "2017-01-02"
            }
          }
        }
      }
    }
  }
}

must，should，must_not，filter：必须匹配，可以匹配其中任意一个即可，必须不匹配

2、搜索帖子ID为XHDK-A-1293-#fJ3，或者是帖子ID为JODL-X-1937-#pV7而且发帖日期为2017-01-01的帖子

select *
from forum.article
where article_id='XHDK-A-1293-#fJ3'
or (article_id='JODL-X-1937-#pV7' and post_date='2017-01-01')

GET /forum/article/_search 
{
  "query": {
    "constant_score": {
      "filter": {
        "bool": {
          "should": [
            {
              "term": {
                "articleID": "XHDK-A-1293-#fJ3"
              }
            },
            {
              "bool": {
                "must": [
                  {
                    "term":{
                      "articleID": "JODL-X-1937-#pV7"
                    }
                  },
                  {
                    "term": {
                      "postDate": "2017-01-01"
                    }
                  }
                ]
              }
            }
          ]
        }
      }
    }
  }
}


2、梳理学到的知识点
（1）term filter：根据exact value进行搜索，数字、boolean、date天然支持；
（2）text需要建索引时指定为not_analyzed或者指定mapping为keyword，才能用term query，不然会被分词。

（1）bool：must，must_not，should，组合多个过滤条件
（2）bool可以嵌套
（3）相当于SQL中的多个and条件：当你把搜索语法学好了以后，基本可以实现部分常用的sql语法对应的功能

#### 基于range filter来进行范围过滤
1、为帖子数据增加浏览量的字段
```
POST /forum/article/_bulk
{ "update": { "_id": "1"} }
{ "doc" : {"view_cnt" : 30} }
{ "update": { "_id": "2"} }
{ "doc" : {"view_cnt" : 50} }
{ "update": { "_id": "3"} }
{ "doc" : {"view_cnt" : 100} }
{ "update": { "_id": "4"} }
{ "doc" : {"view_cnt" : 80} }
```
2、搜索浏览量在30~60之间的帖子
```
GET /forum/article/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "range": {
          "view_cnt": {
            "gt": 30,
            "lt": 60
          }
        }
      }
    }
  }
}
```

3、搜索发帖日期在最近1个月的帖子
```
POST /forum/article/_bulk
{ "index": { "_id": 5 }}
{ "articleID" : "DHJK-B-1395-#Ky5", "userID" : 3, "hidden": false, "postDate": "2017-03-01", "tag": ["elasticsearch"], "tag_cnt": 1, "view_cnt": 10 }

GET /forum/article/_search 
{
  "query": {
    "constant_score": {
      "filter": {
        "range": {
          "postDate": {
            "gt": "2017-03-10||-30d"
          }
        }
      }
    }
  }
}

GET /forum/article/_search 
{
  "query": {
    "constant_score": {
      "filter": {
        "range": {
          "postDate": {
            "gt": "now-30d"
          }
        }
      }
    }
  }
}
```
4、梳理一下学到的知识点

（1）range，sql中的between，或者是>=1，<=1
（2）range做范围过滤



### 3 filter执行原理深度剖析（bitset机制与caching机制）

（1）在倒排索引中查找搜索串，获取document list

date来举例，filter要过滤：2017-02-02
```
word		doc1		doc2		doc3

2017-01-01	*		*
2017-02-02			*		*
2017-03-03	*		*		*

到倒排索引中一找，发现2017-02-02对应的document list是 doc2, doc3
```

（2）为每个在倒排索引中搜索到的结果，构建一个bitset
* 使用找到的doc list，构建一个bitset，就是一个二进制的数组。
* 数组每个元素都是0或1，用来标识一个doc对一个filter条件是否匹配，如果匹配就是1，不匹配就是0
```
[0, 1, 1]
doc1：不匹配这个filter的
doc2和do3：是匹配这个filter的
```

（3）遍历每个filter条件对应的bitset，优先从最稀疏的开始搜索，查找满足所有条件的document

先遍历比较稀疏的bitset，就可以先过滤掉尽可能多的数据
```
请求：filter，postDate=2017-01-01，userID=1

postDate: [0, 0, 1, 1, 0, 0]
userID:   [0, 1, 0, 1, 0, 1]

遍历完两个bitset之后，找到的匹配所有条件的doc，就是doc4
```
就可以将document作为结果返回给client了

（4）caching bitset，在最近256个query中超过一定次数的过滤条件，缓存其bitset。

就不用重新扫描倒排索引，反复生成bitset，可以大幅度提升性能。

对于小segment（<1000，或<3%index总数），不缓存bitset：
* segment数据量很小，扫描也很快；
* 小segment会在后台自动合并合并成大segment，此时就缓存也没有什么意义，segment很快就消失了

filter比query的好处就在于会caching，缓存的是bitmap，不是整个过滤数据。

query：是会计算doc对搜索条件的relevance score，还会根据这个score去排序
filter：只是简单过滤出想要的数据，不计算relevance score，也不排序


（6）如果document有新增或修改，那么cached bitset会被自动更新

document有新增或修改，里面对应的数据符合filter条件的，会自动更新到对应filter的bitset中，全自动，缓存会自动更新。


### 8 基于term+bool实现的multiword搜索底层原理剖析

底层 match query --> bool + term

1、普通match如何转换为term+should
```
{
    "match": { "title": "java elasticsearch"}
}
```
使用上面的match query进行多值搜索的时候，es会在底层自动将这个match query转换为bool的语法

bool should，指定多个搜索词，同时使用term query
```
{
  "bool": {
    "should": [
      { "term": { "title": "java" }},
      { "term": { "title": "elasticsearch"   }}
    ]
  }
}
```

2、and match如何转换为 term + must
```
{
    "match": {
        "title": {
            "query":    "java elasticsearch",
            "operator": "and"
        }
    }
}

{
  "bool": {
    "must": [
      { "term": { "title": "java" }},
      { "term": { "title": "elasticsearch"   }}
    ]
  }
}
```

3、minimum_should_match如何转换
```
{
    "match": {
        "title": {
            "query":                "java elasticsearch hadoop spark",
            "minimum_should_match": "75%"
        }
    }
}

{
  "bool": {
    "should": [
      { "term": { "title": "java" }},
      { "term": { "title": "elasticsearch"   }},
      { "term": { "title": "hadoop" }},
      { "term": { "title": "spark" }}
    ],
    "minimum_should_match": 3 
  }
}
```

### 9 基于boost的细粒度搜索条件权重控制

需求：搜索标题中包含java的帖子，
* 同时，如果标题中包含hadoop或elasticsearch就优先搜索出来，
*　同时，如果一个帖子包含java hadoop，一个帖子包含java elasticsearch，包含hadoop的帖子要比elasticsearch优先搜索出来

知识点：
＊　默认情况下，搜索条件的权重都是一样的，都是1；
＊　搜索条件的权重，boost，可以将某个搜索条件的权重加大；
＊　匹配权重更大的搜索条件的document，relevance score会更高，当然也就会优先被返回回来


```
GET /forum/article/_search 
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": "blog"
          }
        }
      ],
      "should": [
        {
          "match": {
            "title": {
              "query": "java"
            }
          }
        },
        {
          "match": {
            "title": {
              "query": "hadoop"
            }
          }
        },
        {
          "match": {
            "title": {
              "query": "elasticsearch"
            }
          }
        },
        // 匹配到spark的话分数会变成最高，排到最前
        {
          "match": {
            "title": {
              "query": "spark",
              "boost": 5
            }
          }
        }
      ]
    }
  }
}
```

### 10 多shard场景下relevance score不准确问题大揭秘

如果你的一个index有多个shard的话，可能搜索结果会不准确

因为relevance score的计算是在自己那个shard里面算的，如果每个shard的doc不同就会导致不准确。

2、如何解决该问题？

* 生产环境下，数据量大，尽可能实现均匀分配

数据量很大的话，其实一般情况下，在概率学的背景下，es都是在多个shard中均匀路由数据的，路由的时候根据_id，负载均衡
比如说有10个document，title都包含java，一共有5个shard，那么在概率学的背景下，如果负载均衡的话，其实每个shard都应该有2个doc，title包含java
如果说数据分布均匀的话，其实就没有刚才说的那个问题了

* 测试环境下，将索引的primary shard设置为1个，number_of_shards=1，index settings

如果说只有一个shard，那么当然，所有的document都在这个shard里面，就没有这个问题了

* 测试环境下，搜索附带search_type=dfs_query_then_fetch参数，会将local IDF取出来计算global IDF

计算一个doc的相关度分数的时候，就会将所有shard对的local IDF计算一下，获取出来，在本地进行global IDF分数的计算，会将所有shard的doc作为上下文来进行计算，也能确保准确性。
但是production生产环境下，不推荐这个参数，因为性能很差。


### 11 深度探秘搜索技术

#### 在案例中体验如何手动控制全文检索结果的精准度

1、为帖子数据增加标题字段
```
POST /forum/article/_bulk
{ "update": { "_id": "1"} }
{ "doc" : {"title" : "this is java and elasticsearch blog"} }
{ "update": { "_id": "2"} }
{ "doc" : {"title" : "this is java blog"} }
{ "update": { "_id": "3"} }
{ "doc" : {"title" : "this is elasticsearch blog"} }
{ "update": { "_id": "4"} }
{ "doc" : {"title" : "this is java, elasticsearch, hadoop blog"} }
{ "update": { "_id": "5"} }
{ "doc" : {"title" : "this is spark blog"} }
```

2、搜索标题中包含java或elasticsearch的blog
```
GET /forum/article/_search
{
    "query": {
        "match": {
            "title": "java elasticsearch"
        }
    }
}
```
这个跟之前的那个term query，不一样了。
不是搜索exact value，match query是进行full text全文检索（匹配到其中的一个就可以）。
当然，如果要检索的field，是not_analyzed类型的，那么match query也相当于term query。

3、搜索标题中包含java和elasticsearch的blog
搜索结果精准控制的第一步：灵活使用and关键字，
* 所有的搜索关键字都要匹配的就用and，可以实现单纯match query无法实现的效果
```
GET /forum/article/_search
{
    "query": {
        "match": {
            "title": {
				"query": "java elasticsearch",
				"operator": "and"
   	    }
        }
    }
}
```

4、搜索包含java，elasticsearch，spark，hadoop，4个关键字中，至少3个的blog

控制搜索结果的精准度的第二步：
* 指定一些关键字中，必须至少匹配其中的多少个关键字，才能作为结果返回
```
GET /forum/article/_search
{
  "query": {
    "match": {
      "title": {
        "query": "java elasticsearch spark hadoop",
        "minimum_should_match": "75%"
      }
    }
  }
}
```

5、用bool组合多个搜索条件，来搜索title
```
GET /forum/article/_search
{
  "query": {
    "bool": {
      "must":     { "match": { "title": "java" }},
      "must_not": { "match": { "title": "spark"  }},
      "should": [
                  { "match": { "title": "hadoop" }},
                  { "match": { "title": "elasticsearch"   }}
      ]
    }
  }
}
```
6、bool组合多个搜索条件，如何计算relevance score

must和should搜索对应的分数，加起来，除以must和should的总数

排名第一：java，同时包含should中所有的关键字，hadoop，elasticsearch
排名第二：java，同时包含should中的elasticsearch
排名第三：java，不包含should中的任何关键字

should是可以影响相关度分数的

must 确保谁必须有这个关键字，同时会根据这个must的条件去计算出document对这个搜索条件的relevance score
在满足must的基础之上，should中的条件，不匹配也可以，但是如果匹配的更多，那么document的relevance score就会更高
```
{
  "took": 6,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 1.3375794,
    "hits": [
      {
        "_index": "forum",
        "_type": "article",
        "_id": "4",
        "_score": 1.3375794,
        "_source": {
          "articleID": "QQPX-R-3956-#aD8",
          "userID": 2,
          "hidden": true,
          "postDate": "2017-01-02",
          "tag": [
            "java",
            "elasticsearch"
          ],
          "tag_cnt": 2,
          "view_cnt": 80,
          "title": "this is java, elasticsearch, hadoop blog"
        }
      },
      {
        "_index": "forum",
        "_type": "article",
        "_id": "1",
        "_score": 0.53484553,
        "_source": {
          "articleID": "XHDK-A-1293-#fJ3",
          "userID": 1,
          "hidden": false,
          "postDate": "2017-01-01",
          "tag": [
            "java",
            "hadoop"
          ],
          "tag_cnt": 2,
          "view_cnt": 30,
          "title": "this is java and elasticsearch blog"
        }
      },
      {
        "_index": "forum",
        "_type": "article",
        "_id": "2",
        "_score": 0.19856805,
        "_source": {
          "articleID": "KDKE-B-9947-#kL5",
          "userID": 1,
          "hidden": false,
          "postDate": "2017-01-02",
          "tag": [
            "java"
          ],
          "tag_cnt": 1,
          "view_cnt": 50,
          "title": "this is java blog"
        }
      }
    ]
  }
}
```
7、搜索java，hadoop，spark，elasticsearch，至少包含其中3个关键字

默认情况下，should是可以不匹配任何一个的，比如上面的搜索中，this is java blog，就不匹配任何一个should条件
但是有个例外的情况，如果没有must的话，那么should中必须至少匹配一个才可以
比如下面的搜索，should中有4个条件，默认情况下，只要满足其中一个条件，就可以匹配作为结果返回

但是可以精准控制，should的4个条件中，至少匹配几个才能作为结果返回
```
GET /forum/article/_search
{
  "query": {
    "bool": {
      "should": [
        { "match": { "title": "java" }},
        { "match": { "title": "elasticsearch"   }},
        { "match": { "title": "hadoop"   }},
	{ "match": { "title": "spark"   }}
      ],
      "minimum_should_match": 3 
    }
  }
}
```

梳理一下学到的知识点

1. 全文检索的时候，进行多个值的检索，有两种做法，match query；should
2. 控制搜索结果精准度：and operator，minimum_should_match



#### 案例实战基于 dis_max 实现best fields策略进行多字段搜索

1、为帖子数据增加content字段
```
POST /forum/article/_bulk
{ "update": { "_id": "1"} }
{ "doc" : {"content" : "i like to write best elasticsearch article"} }
{ "update": { "_id": "2"} }
{ "doc" : {"content" : "i think java is the best programming language"} }
{ "update": { "_id": "3"} }
{ "doc" : {"content" : "i am only an elasticsearch beginner"} }
{ "update": { "_id": "4"} }
{ "doc" : {"content" : "elasticsearch and hadoop are all very good solution, i am a beginner"} }
{ "update": { "_id": "5"} }
{ "doc" : {"content" : "spark is best big data solution based on scala ,an programming language similar to java"} }
```
2、搜索title或content中包含java或solution的帖子

下面这个就是multi-field搜索，多字段搜索
```
GET /forum/article/_search
{
    "query": {
        "bool": {
            "should": [
                { "match": { "title": "java solution" }},
                { "match": { "content":  "java solution" }}
            ]
        }
    }
}
```
3、结果分析

期望的是doc5，结果是doc2,doc4排在了前面

计算每个document的relevance score：每个query的分数，乘以matched query数量，除以总query数量

算一下doc4的分数
```
{ "match": { "title": "java solution" }}，针对doc4，是有一个分数的
{ "match": { "content":  "java solution" }}，针对doc4，也是有一个分数的
```
所以是两个分数加起来，比如说，1.1 + 1.2 = 2.3
matched query数量 = 2
总query数量 = 2

2.3 * 2 / 2 = 2.3

算一下doc5的分数
```
{ "match": { "title": "java solution" }}，针对doc5，是没有分数的
{ "match": { "content":  "java solution" }}，针对doc5，是有一个分数的
```
所以说，只有一个query是有分数的，比如2.3
matched query数量 = 1
总query数量 = 2

2.3 * 1 / 2 = 1.15

doc5的分数 = 1.15 < doc4的分数 = 2.3

4、best fields策略，dis_max

best fields：
* 搜索到的结果，应该是某一个field中匹配到了尽可能多的关键词，就被排在前面；
* 而不是尽可能多的field匹配到了少数的关键词，排在了前面

dis_max语法，直接取多个query中，分数最高的那一个query的分数即可

{ "match": { "title": "java solution" }}，针对doc4，是有一个分数的，1.1
{ "match": { "content":  "java solution" }}，针对doc4，也是有一个分数的，1.2
取最大分数，1.2

{ "match": { "title": "java solution" }}，针对doc5，是没有分数的
{ "match": { "content":  "java solution" }}，针对doc5，是有一个分数的，2.3
取最大分数，2.3

然后doc4的分数 = 1.2 < doc5的分数 = 2.3，所以doc5就可以排在更前面的地方，符合我们的需要
```
GET /forum/article/_search
{
    "query": {
        "dis_max": {
            "queries": [
                { "match": { "title": "java solution" }},
                { "match": { "content":  "java solution" }}
            ]
        }
    }
}
```

#### 案例实战基于tie_breaker参数优化dis_max搜索效果

1、dis_max 搜索title或content中包含java beginner的帖子
可能在实际场景中出现的一个情况是这样的：

（1）某个帖子，doc1，title中包含java，content不包含java beginner任何一个关键词
（2）某个帖子，doc2，content中包含beginner，title中不包含任何一个关键词
（3）某个帖子，doc3，title中包含java，content中包含beginner
（4）最终搜索，可能出来的结果是，doc1和doc2排在doc3的前面，而不是我们期望的doc3排在最前面

dis_max，只是取分数最高的那个query的分数而已，完全不考虑其他query的分数

3、使用tie_breaker将其他query的分数也考虑进去

tie_breaker参数将其他query的分数，乘以tie_breaker，然后综合与最高分数的那个query的分数，综合在一起进行计算
除了取最高分以外，还会考虑其他的query的分数
tie_breaker的值，在0~1之间，是个小数，就ok
```
GET /forum/article/_search
{
    "query": {
        "dis_max": {
            "queries": [
                { "match": { "title": "java beginner" }},
                { "match": { "body":  "java beginner" }}
            ],
            "tie_breaker": 0.3
        }
    }
}
```


* 优化： 基于multi_match语法实现dis_max+tie_breaker

minimum_should_match作用：
1. 去长尾，long tail
2. 长尾，比如你搜索5个关键词，但是很多结果是只匹配1个关键词的，其实跟你想要的结果相差甚远，这些结果就是长尾
minimum_should_match，控制搜索结果的精准度，只有匹配一定数量的关键词的数据，才能返回

```
GET /forum/article/_search
{
  "query": {
    "multi_match": {
        "query":                "java solution",
        "type":                 "best_fields", 
        "fields":               [ "title^2", "content" ],
        "tie_breaker":          0.3,
        "minimum_should_match": "50%" 
    }
  } 
}

GET /forum/article/_search
{
  "query": {
    "dis_max": {
      "queries":  [
        {
          "match": {
            "title": {
              "query": "java beginner",
              "minimum_should_match": "50%",
	      "boost": 2
            }
          }
        },
        {
          "match": {
            "body": {
              "query": "java beginner",
              "minimum_should_match": "30%"
            }
          }
        }
      ],
      "tie_breaker": 0.3
    }
  } 
}
```