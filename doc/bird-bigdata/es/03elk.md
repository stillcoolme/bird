
### 33 深入聚合数据分析

#### 两个核心概念：bucket和metric

bucket：一个数据分组
```
city name
北京 小李
北京 小王
上海 小张
上海 小丽
上海 小陈
```
基于city划分buckets，划分出来两个bucket，（北京bucket，上海bucket）
* 北京bucket：包含了2个人，小李，小王
* 上海bucket：包含了3个人，小张，小丽，小陈

按照某个字段进行bucket划分，那个字段的值相同的那些数据，就会被划分到一个bucket中，
mysql进行聚合，首先第一步就是分组，对每个组内的数据进行聚合分析，分组，就是我们的bucket；

metric：对一个数据分组执行的统计

当我们有了一堆bucket之后，就可以对每个bucket中的数据进行聚合分词了，
比如说计算一个bucket内所有数据的数量，
或者计算一个bucket内所有数据的平均值，最大值，最小值

metric，就是对一个bucket执行的某种聚合分析的操作，比如说求平均值，求最大值，求最小值
```
select count(*) from access_log group by user_id

bucket：group by user_id --> 那些user_id相同的数据，就会被划分到一个bucket中
metric：count(*)，对每个user_id bucket中所有的数据，计算一个数量
```

#### 34 家电卖场案例以及统计哪种颜色电视销量最高

1、家电卖场案例背景

以一个家电卖场中的电视销售数据为背景，来对各种品牌，各种颜色的电视的销量和销售额，进行各种各样角度的分析
```
# 注意7.x版本没有type的了，改为创建 tvs_sales，把sales去掉。。。
PUT /tvs
{
	"mappings": {
		"sales": {
			"properties": {
				"price": {
					"type": "long"
				},
				"color": {
					"type": "keyword"
				},
				"brand": {
					"type": "keyword"
				},
				"sold_date": {
					"type": "date"
				}
			}
		}
	}
}

POST /tvs/sales/_bulk
{ "index": {}}
{ "price" : 1000, "color" : "红色", "brand" : "长虹", "sold_date" : "2016-10-28" }
{ "index": {}}
{ "price" : 2000, "color" : "红色", "brand" : "长虹", "sold_date" : "2016-11-05" }
{ "index": {}}
{ "price" : 3000, "color" : "绿色", "brand" : "小米", "sold_date" : "2016-05-18" }
{ "index": {}}
{ "price" : 1500, "color" : "蓝色", "brand" : "TCL", "sold_date" : "2016-07-02" }
{ "index": {}}
{ "price" : 1200, "color" : "绿色", "brand" : "TCL", "sold_date" : "2016-08-19" }
{ "index": {}}
{ "price" : 2000, "color" : "红色", "brand" : "长虹", "sold_date" : "2016-11-05" }
{ "index": {}}
{ "price" : 8000, "color" : "红色", "brand" : "三星", "sold_date" : "2017-01-01" }
{ "index": {}}
{ "price" : 2500, "color" : "蓝色", "brand" : "小米", "sold_date" : "2017-02-12" }
```
2、统计哪种颜色的电视销量最高
```
GET /tvs/sales/_search
{
    "size" : 0,
    "aggs" : { 
        "popular_colors" : { 
            "terms" : { 
              "field" : "color"
            }
        }
    }
}

size：只获取聚合结果，而不要执行聚合的原始数据
aggs：固定语法，要对一份数据执行分组聚合操作
popular_colors：就是对每个aggs，都要起一个名字，这个名字是随机的，你随便取什么都ok
terms：根据字段的值进行分组
field：根据指定的字段的值进行分组

{
  "took": 61,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 8,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "popular_color": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0,
      "buckets": [
        {
          "key": "红色",
          "doc_count": 4
        },
        {
          "key": "绿色",
          "doc_count": 2
        },
        {
          "key": "蓝色",
          "doc_count": 2
        }
      ]
    }
  }
}

hits.hits：我们指定了size是0，所以hits.hits就是空的，否则会把执行聚合的那些原始数据给你返回回来
aggregations：聚合结果
popular_color：我们指定的某个聚合的名称
buckets：根据我们指定的field划分出的buckets
key：每个bucket对应的那个值
doc_count：这个bucket分组内，有多少个数据
数量，其实就是这种颜色的销量

每种颜色对应的bucket中的数据的
默认的排序规则：按照doc_count降序排序
```


#### 35 实战bucket+metric：统计每种颜色电视平均价格
```
select avg(price)
from tvs.sales
group by color

GET /tvs/sales/_search
{
   "size" : 0,
   "aggs": {
      "colors": {
         "terms": {
            "field": "color"
         },
         "aggs": { 
            "avg_price": { 
               "avg": {
                  "field": "price" 
               }
            }
         }
      }
   }
}
```
按照color去分bucket，可以拿到每个color bucket中的数量，
这个仅仅只是一个bucket操作，doc_count其实只是es的bucket操作默认执行的一个内置metric

除了bucket操作，分组，还要对每个bucket执行一个metric聚合统计操作

在一个aggs执行的bucket操作（terms），平级的json结构下，再加一个aggs同样取个名字，对每个bucket再执行一个metric操作avg，
对之前的每个bucket中的数据的指定的field，price field，求一个平均值


掌握更多metrics：统计每种颜色电视最大最小价格

count：bucket，terms，自动就会有一个doc_count，就相当于是count
avg：avg aggs，求平均值
max：求一个bucket内，指定field值最大的那个数据
min：求一个bucket内，指定field值最小的那个数据
sum：求一个bucket内，指定field值的总和

一般来说，90%的常见的数据分析的操作，metric，无非就是count，avg，max，min，sum
```
GET /tvs/sales/_search
{
   "size" : 0,
   "aggs": {
      "colors": {
         "terms": {
            "field": "color"
         },
         "aggs": {
            "avg_price": { "avg": { "field": "price" } },
            "min_price" : { "min": { "field": "price"} }, 
            "max_price" : { "max": { "field": "price"} },
            "sum_price" : { "sum": { "field": "price" } } 
         }
      }
   }
}
```

* 排序：按每种颜色的平均销售额降序排序

排序，默认是按照每个bucket的doc_count降序来排的

但是我们现在统计出来每个颜色的电视的销售额，需要按照销售额降序排序

```

GET /tvs/sales/_search 
{
  "size": 0,
  "aggs": {
    "group_by_color": {
      "terms": {
        "field": "color",
        "order": {
          "avg_price": "asc"
        }
      },
      "aggs": {
        "avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  }
}

```


* 颜色+品牌下钻分析时按最深层metric进行排序
```
GET /tvs/sales/_search 
{
  "size": 0,
  "aggs": {
    "group_by_color": {
      "terms": {
        "field": "color"
      },
      "aggs": {
        "group_by_brand": {
          "terms": {
            "field": "brand",
            "order": {
              "avg_price": "desc"
            }
          },
          "aggs": {
            "avg_price": {
              "avg": {
                "field": "price"
              }
            }
          }
        }
      }
    }
  }
}
```



#### 36 bucket嵌套实现颜色+品牌的多层下钻分析
从颜色到品牌进行多层次下钻分析：每种颜色的平均价格，以及找到每种颜色每个品牌的平均价格

* 现在红色的电视有4台，同时这4台电视中，有3台是属于长虹的，1台是属于小米的
* 红色电视中的3台长虹的平均价格是多少？
* 红色电视中的1台小米的平均价格是多少？

下钻：
* 已经分了一个组了，比如说颜色的分组，
* 然后还要继续对这个分组内的数据，再分组，比如一个颜色内，还可以分成多个不同的品牌的组，
* 最后对每个最小粒度的分组执行聚合分析操作，这就叫做下钻分析
* es，下钻分析，就要对bucket进行多层嵌套，多次分组

按照多个维度（颜色+品牌）多层下钻分析，而且学会了每个下钻维度（颜色，颜色+品牌），都可以对每个维度分别执行一次metric聚合操作

```
GET /tvs/sales/_search 
{
  "size": 0,
  "aggs": {
    "group_by_color": {
      "terms": {
        "field": "color"
      },
      "aggs": {
        "color_avg_price": {
          "avg": {
            "field": "price"
          }
        },
        "group_by_brand": {
          "terms": {
            "field": "brand"
          },
          "aggs": {
            "brand_avg_price": {
              "avg": {
                "field": "price"
              }
            }
          }
        }
      }
    }
  }
}

{
  "took": 8,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 8,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "group_by_color": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0,
      "buckets": [
        {
          "key": "红色",
          "doc_count": 4,
          "color_avg_price": {
            "value": 3250
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
              {
                "key": "长虹",
                "doc_count": 3,
                "brand_avg_price": {
                  "value": 1666.6666666666667
                }
              },
              {
                "key": "三星",
                "doc_count": 1,
                "brand_avg_price": {
                  "value": 8000
                }
              }
            ]
          }
        },
        {
          "key": "绿色",
          "doc_count": 2,
          "color_avg_price": {
            "value": 2100
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
              {
                "key": "TCL",
                "doc_count": 1,
                "brand_avg_price": {
                  "value": 1200
                }
              },
              {
                "key": "小米",
                "doc_count": 1,
                "brand_avg_price": {
                  "value": 3000
                }
              }
            ]
          }
        },
        {
          "key": "蓝色",
          "doc_count": 2,
          "color_avg_price": {
            "value": 2000
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
              {
                "key": "TCL",
                "doc_count": 1,
                "brand_avg_price": {
                  "value": 1500
                }
              },
              {
                "key": "小米",
                "doc_count": 1,
                "brand_avg_price": {
                  "value": 2500
                }
              }
            ]
          }
        }
      ]
    }
  }
}
```


#### 38 实战hitogram按价格区间统计电视销量和销售额

histogram：
* 类似于terms，也是进行bucket分组操作
* 接收一个field，按照这个field的值的各个范围区间，进行bucket分组操作

"histogram":{ 
  "field": "price",
  "interval": 2000
},

interval：2000，划分范围，0~2000，2000~4000...

buckets去根据price的值，比如2500，看落在哪个区间内，比如2000~4000，此时就会将这条数据放入2000~4000对应的那个bucket中

有了bucket之后，一样的去对每个bucket执行avg，count，sum，max，min，等各种metric操作，聚合分析

```
GET /tvs/sales/_search
{
   "size" : 0,
   "aggs":{
      "price":{
         "histogram":{ 
            "field": "price",
            "interval": 2000
         },
         "aggs":{
            "revenue": {
               "sum": { 
                 "field" : "price"
               }
             }
         }
      }
   }
}

{
  "took": 13,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 8,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "group_by_price": {
      "buckets": [
        {
          "key": 0,
          "doc_count": 3,
          "sum_price": {
            "value": 3700
          }
        },
        {
          "key": 2000,
          "doc_count": 4,
          "sum_price": {
            "value": 9500
          }
        },
        {
          "key": 4000,
          "doc_count": 0,
          "sum_price": {
            "value": 0
          }
        },
        {
          "key": 6000,
          "doc_count: {
            "value":": 0,
          "sum_price" 0
          }
        },
        {
          "key": 8000,
          "doc_count": 1,
          "sum_price": {
            "value": 8000
          }
        }
      ]
    }
  }
}
```


实战date hitogram之统计每月电视销量

* bucket，分组操作，
* histogram，按照某个值指定的interval，划分一个一个的bucket
* date histogram，按照我们指定的某个date类型的日期field，以及日期interval，按照一定的日期间隔，去划分bucket

date interval = 1m，

2017-01-01~2017-01-31，就是一个bucket
2017-02-01~2017-02-28，就是一个bucket

然后会去扫描每个数据的date field，判断date落在哪个bucket中，就将其放入那个bucket

2017-01-05，就将其放入2017-01-01~2017-01-31，就是一个bucket
```
GET /tvs/sales/_search
{
   "size" : 0,
   "aggs": {
      "sales": {
         "date_histogram": {
            "field": "sold_date",
            "interval": "month", 
            "format": "yyyy-MM-dd",
            // 即使某个日期interval，2017-01-01~2017-01-31中，一条数据都没有，那么这个区间也是要返回的，不然默认是会过滤掉这个区间的
            "min_doc_count" : 0, 
            // 划分bucket的时候，会限定在这个起始日期，和截止日期内
            "extended_bounds" : { 
                "min" : "2016-01-01",
                "max" : "2017-12-31"
            }
         }
      }
   }
}

{
  "took": 16,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 8,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "group_by_sold_date": {
      "buckets": [
        {
          "key_as_string": "2016-01-01",
          "key": 1451606400000,
          "doc_count": 0
        },
        {
          "key_as_string": "2016-02-01",
          "key": 1454284800000,
          "doc_count": 0
        },
        {
          "key_as_string": "2016-03-01",
          "key": 1456790400000,
          "doc_count": 0
        },
        {
          "key_as_string": "2016-04-01",
          "key": 1459468800000,
          "doc_count": 0
        },
        {
          "key_as_string": "2016-05-01",
          "key": 1462060800000,
          "doc_count": 1
        },
        {
          "key_as_string": "2016-06-01",
          "key": 1464739200000,
          "doc_count": 0
        },
        {
          "key_as_string": "2016-07-01",
          "key": 1467331200000,
          "doc_count": 1
        },
        {
          "key_as_strin
          "key_as_string": "2016-09-01",
          "key": 1472688000000,
          "doc_count": 0
        },g": "2016-08-01",
          "key": 1470009600000,
          "doc_count": 1
        },
        {
        {
          "key_as_string": "2016-10-01",
          "key": 1475280000000,
          "doc_count": 1
        },
        {
          "key_as_string": "2016-11-01",
          "key": 1477958400000,
          "doc_count": 2
        },
        {
          "key_as_string": "2016-12-01",
          "key": 1480550400000,
          "doc_count": 0
        },
        {
          "key_as_string": "2017-01-01",
          "key": 1483228800000,
          "doc_count": 1
        },
        {
          "key_as_string": "2017-02-01",
          "key": 1485907200000,
          "doc_count": 1
        }
      ]
    }
  }
}
```
* 下钻分析之统计每季度每个品牌的销售额
```
GET /tvs/sales/_search 
{
  "size": 0,
  "aggs": {
    "group_by_sold_date": {
      "date_histogram": {
        "field": "sold_date",
        "interval": "quarter",
        "format": "yyyy-MM-dd",
        "min_doc_count": 0,
        "extended_bounds": {
          "min": "2016-01-01",
          "max": "2017-12-31"
        }
      },
      "aggs": {
        "group_by_brand": {
          "terms": {
            "field": "brand"
          },
          "aggs": {
            "sum_price": {
              "sum": {
                "field": "price"
              }
            }
          }
        },
        "total_sum_price": {
          "sum": {
            "field": "price"
          }
        }
      }
    }
  }
}

{
  "took": 10,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 8,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "group_by_sold_date": {
      "buckets": [
        {
          "key_as_string": "2016-01-01",
          "key": 1451606400000,
          "doc_count": 0,
          "total_sum_price": {
            "value": 0
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": []
          }
        },
        {
          "key_as_string": "2016-04-01",
          "key": 1459468800000,
          "doc_count": 1,
          "total_sum_price": {
            "value": 3000
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
              {
                "key": "小米",
                "doc_count": 1,
                "sum_price": {
                  "value": 3000
                }
              }
            ]
          }
        },
        {
          "key_as_string": "2016-07-01",
          "key": 1467331200000,
          "doc_count": 2,
          "total_sum_price": {
            "value": 2700
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
              {
                "key": "TCL",
                "doc_count": 2,
                "sum_price": {
                  "value": 2700
                }
              }
            ]
          }
        },
        {
          "key_as_string": "2016-10-01",
          "key": 1475280000000,
          "doc_count": 3,
          "total_sum_price": {
            "value": 5000
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
              {
                "key": "长虹",
                "doc_count": 3,
                "sum_price": {
                  "value": 5000
                }
              }
            ]
          }
        },
        {
          "key_as_string": "2017-01-01",
          "key": 1483228800000,
          "doc_count": 2,
          "total_sum_price": {
            "value": 10500
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
              {
                "key": "三星",
                "doc_count": 1,
                "sum_price": {
                  "value": 8000
                }
              },
              {
                "key": "小米",
                "doc_count": 1,
                "sum_price": {
                  "value": 2500
                }
              }
            ]
          }
        },
        {
          "key_as_string": "2017-04-01",
          "key": 1491004800000,
          "doc_count": 0,
          "total_sum_price": {
            "value": 0
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": []
          }
        },
        {
          "key_as_string": "2017-07-01",
          "key": 1498867200000,
          "doc_count": 0,
          "total_sum_price": {
            "value": 0
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": []
          }
        },
        {
          "key_as_string": "2017-10-01",
          "key": 1506816000000,
          "doc_count": 0,
          "total_sum_price": {
            "value": 0
          },
          "group_by_brand": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": []
          }
        }
      ]
    }
  }
}
```


#### 41 搜索+聚合：统计指定品牌下每个颜色的销量
* 将之前学习的搜索相关的知识，完全可以和聚合组合起来使用

select count(*)
from tvs.sales
where brand like "%长%"
group by price

es任何的聚合都必须在搜索出来的结果数据中行，搜索结果，就是聚合分析操作的scope
```
// 对小米手机的颜色进行分组

GET /tvs/sales/_search 
{
  "size": 0,
  "query": {
    "term": {
      "brand": {
        "value": "小米"
      }
    }
  },
  "aggs": {
    "group_by_color": {
      "terms": {
        "field": "color"
      }
    }
  }
}

```

* global bucket：实现单个品牌与所有品牌销量对比

aggregation，scope，一个聚合操作，必须在query的搜索结果范围内执行

我们要出来两个结果，
* 一个结果，是基于query搜索结果来聚合的; 
* 一个结果，是对所有数据执行聚合的

```
GET /tvs/sales/_search 
{
  "size": 0, 
  "query": {
    "term": {
      "brand": {
        "value": "长虹"
      }
    }
  },
  "aggs": {
    "single_brand_avg_price": {
      "avg": {
        "field": "price"
      }
    },
    "all": {
      // global bucket，就是将所有数据纳入聚合的scope，而不管之前的query
      "global": {},
      "aggs": {
        "all_brand_avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  }
}

{
  "took": 4,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "all": {
      "doc_count": 8,
      // 拿到所有品牌的平均价格
      "all_brand_avg_price": {
        "value": 2650
      }
    },
    // 针对query搜索结果，执行的，拿到的，就是长虹品牌的平均价格
    "single_brand_avg_price": {
      "value": 1666.6666666666667
    }
  }
}
```

* 搜索过滤+聚合：统计价格大于1200的电视平均价格

```
GET /tvs/sales/_search 
{
  "size": 0,
  "query": {
    "constant_score": {
      "filter": {
        "range": {
          "price": {
            "gte": 1200
          }
        }
      }
    }
  },
  "aggs": {
    "avg_price": {
      "avg": {
        "field": "price"
      }
    }
  }
}

{
  "took": 41,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 7,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "avg_price": {
      "value": 2885.714285714286
    }
  }
}
```

#### 44 bucket filter：统计品牌最近一个月的平均价格
bucket filter：对不同的bucket下的aggs，进行filter

```
GET /tvs/sales/_search 
{
  "size": 0,
  "query": {
    "term": {
      "brand": {
        "value": "长虹"
      }
    }
  },
  "aggs": {
    "recent_150d": {
      "filter": {
        "range": {
          "sold_date": {
            "gte": "now-150d"
          }
        }
      },
      "aggs": {
        "recent_150d_avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    },
    "recent_140d": {
      "filter": {
        "range": {
          "sold_date": {
            "gte": "now-140d"
          }
        }
      },
      "aggs": {
        "recent_140d_avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    },
    "recent_130d": {
      "filter": {
        "range": {
          "sold_date": {
            "gte": "now-130d"
          }
        }
      },
      "aggs": {
        "recent_130d_avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  }
}

```
aggs.filter，针对的是聚合去做的

如果放query里面的filter，是全局的，会对所有的数据都有影响

但是，如果你要统计，长虹电视，最近1个月的平均值; 最近3个月的平均值; 最近6个月的平均值


#### 48 cardinality去重算法以及每月销售品牌数量统计

有些聚合分析的算法，是很容易就可以并行的，比如说max。

有些聚合分析的算法，是不好并行的，比如count(distinct)，es会采取近似聚合（牺牲精准度）的方式，
在每个node上进行近估计的方式，得到最终的结论（不拿回master节点进行归并）；
近似估计后的结果，不完全准确，但是速度会很快，一般会达到完全精准的算法的性能的数十倍



* cartinality metric对每个bucket中的指定的field进行去重，取去重后的count，类似于count(distcint)
5%的错误率，性能在100ms左右
```
GET /tvs/sales/_search
{
  "size" : 0,
  "aggs" : {
      "months" : {
        "date_histogram": {
          "field": "sold_date",
          "interval": "month"
        },
        "aggs": {
          "distinct_colors" : {
              "cardinality" : {
                "field" : "brand"
              }
          }
        }
      }
  }
}
```

1、precision_threshold优化准确率和内存开销
```
GET /tvs/sales/_search
{
    "size" : 0,
    "aggs" : {
        "distinct_brand" : {
            "cardinality" : {
              "field" : "brand",
              "precision_threshold" : 100 
            }
        }
    }
}
```
brand去重，如果brand的unique value，在100个以内，小米，长虹，三星，TCL，HTL。。。

在多少个unique value以内，cardinality，几乎保证100%准确

cardinality算法，会占用precision_threshold * 8 byte 内存消耗，100 * 8 = 800个字节
占用内存很小。。。而且unique value如果的确在值以内，那么可以确保100%准确
100，数百万的unique value，错误率在5%以内

precision_threshold，值设置的越大，占用内存越大，1000 * 8 = 8000 / 1000 = 8KB，可以确保更多unique value的场景下，100%的准确

field，去重，count，这时候，unique value，10000，precision_threshold=10000，10000 * 8 = 80000个byte，80KB

2、HyperLogLog++ (HLL)算法性能优化

cardinality底层算法：HLL算法，HLL算法的性能

会对所有的uqniue value取hash值，通过hash值近似去求distcint count，误差

默认情况下，发送一个cardinality请求的时候，会动态地对所有的field value，取hash值; 将取hash值的操作，前移到建立索引的时候
```
PUT /tvs/
{
  "mappings": {
    "sales": {
      "properties": {
        "brand": {
          "type": "text",
          "fields": {
            "hash": {
              "type": "murmur3" 
            }
          }
        }
      }
    }
  }
}

GET /tvs/sales/_search
{
    "size" : 0,
    "aggs" : {
        "distinct_brand" : {
            "cardinality" : {
              "field" : "brand.hash",
              "precision_threshold" : 100 
            }
        }
    }
}
```


#### 50 percentiles百分比算法以及网站访问时延统计

需求：比如有一个网站，记录下了每次请求的访问的耗时，需要统计tp50，tp90，tp99

tp50：50%的请求的耗时最长在多长时间
tp90：90%的请求的耗时最长在多长时间
tp99：99%的请求的耗时最长在多长时间
```
PUT /website_logs
{
    "mappings": {
        "properties": {
            "latency": {
                "type": "long"
            },
            "province": {
                "type": "keyword"
            },
            "timestamp": {
                "type": "date"
            }
        }
    }
}

POST /website_logs/_bulk
{ "index": {}}
{ "latency" : 105, "province" : "江苏", "timestamp" : "2016-10-28" }
{ "index": {}}
{ "latency" : 83, "province" : "江苏", "timestamp" : "2016-10-29" }
{ "index": {}}
{ "latency" : 92, "province" : "江苏", "timestamp" : "2016-10-29" }
{ "index": {}}
{ "latency" : 112, "province" : "江苏", "timestamp" : "2016-10-28" }
{ "index": {}}
{ "latency" : 68, "province" : "江苏", "timestamp" : "2016-10-28" }
{ "index": {}}
{ "latency" : 76, "province" : "江苏", "timestamp" : "2016-10-29" }
{ "index": {}}
{ "latency" : 101, "province" : "新疆", "timestamp" : "2016-10-28" }
{ "index": {}}
{ "latency" : 275, "province" : "新疆", "timestamp" : "2016-10-29" }
{ "index": {}}
{ "latency" : 166, "province" : "新疆", "timestamp" : "2016-10-29" }
{ "index": {}}
{ "latency" : 654, "province" : "新疆", "timestamp" : "2016-10-28" }
{ "index": {}}
{ "latency" : 389, "province" : "新疆", "timestamp" : "2016-10-28" }
{ "index": {}}
{ "latency" : 302, "province" : "新疆", "timestamp" : "2016-10-29" }
```
50%的请求，数值的最大的值是多少，不是完全准确的
```
GET /website_logs/_search 
{
  "size": 0,
  "aggs": {
    "group_by_province": {
      "terms": {
        "field": "province"
      },
      "aggs": {
        "latency_percentiles": {
          "percentiles": {
            "field": "latency",
            "percents": [
              50,
              95,
              99
            ]
          }
        },
        "latency_avg": {
          "avg": {
            "field": "latency"
          }
        }
      }
    }
  }
}

{
  "took": 33,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 12,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "group_by_province": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0,
      "buckets": [
        {
          "key": "新疆",
          "doc_count": 6,
          "latency_avg": {
            "value": 314.5
          },
          "latency_percentiles": {
            "values": {
              "50.0": 288.5,
              "95.0": 587.75,
              "99.0": 640.75
            }
          }
        },
        {
          "key": "江苏",
          "doc_count": 6,
          "latency_avg": {
            "value": 89.33333333333333
          },
          "latency_percentiles": {
            "values": {
              "50.0": 87.5,
              "95.0": 110.25,
              "99.0": 111.65
            }
          }
        }
      ]
    }
  }
}
```

* percentiles rank以及网站访问时延SLA统计

我们的网站的提供的访问延时的SLA，确保大公司内，一般都是要求100%在200ms以内

如果超过1s，则需要升级到A级故障，代表网站的访问性能和用户体验急剧下降

需求：在200ms以内的，有百分之多少，在1000毫秒以内的有百分之多少，percentile ranks metric（这个percentile ranks，其实比pencentile还要常用）

按照品牌分组，计算，电视机，售价在1000占比，2000占比，3000占比

```
GET /website_logs/_search 
{
  "size": 0,
  "aggs": {
    "group_by_province": {
      "terms": {
        "field": "province"
      },
      "aggs": {
        "latency_percentile_ranks": {
          "percentile_ranks": {
            "field": "latency",
            "values": [
              200,
              1000
            ]
          }
        }
      }
    }
  }
}

{
  "took": 38,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 12,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "group_by_province": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0,
      "buckets": [
        {
          "key": "新疆",
          "doc_count": 6,
          "latency_percentile_ranks": {
            "values": {
              "200.0": 29.40613026819923,
              "1000.0": 100
            }
          }
        },
        {
          "key": "江苏",
          "doc_count": 6,
          "latency_percentile_ranks": {
            "values": {
              "200.0": 100,
              "1000.0": 100
            }
          }
        }
      ]
    }
  }
}
```

percentile的优化：

TDigest算法，用很多节点来执行百分比的计算，近似估计，有误差，节点越多，越精准

通过compression（默认100）限制节点数量最多 compression * 20 = 2000个node去计算

compression越大，占用内存越多，越精准，性能越差

一个节点占用32字节，100 * 20 * 32 = 64KB

如果你想要percentile算法越精准，compression可以设置的越大


#### 52 基于doc value正排索引的聚合内部原理
聚合分析的内部原理是什么？？？？
* aggs，term，metric avg max，执行一个聚合操作的时候，内部原理是怎样的呢？
* 用了什么样的数据结构去执行聚合？是不是用的倒排索引？

搜索+聚合，写个示例，看一下纯用倒排索引来实现的弊端
```
GET /test_index/test_type/_search 
{
  "query": {
    "match": {
      "search_field": "test"
    }
  },
  "aggs": {
    "group_by_agg_field": {
      "terms": {
        "field": "agg_field"
      }
    }
  }
}

search_field

doc1: hello world test1, test2
doc2: hello test
doc3: world test

hello doc1,doc2
world doc1,doc3
test1 doc1
test2 doc1
test  doc2,doc3

"query": {
  "match": {
    "search_field": "test"
  }
}

test --> doc2,doc3 --> search result, doc2,doc3

agg_field

doc2: agg1
doc3: agg2


100万个值
...
...
...
...
agg1  doc2
agg2  doc3
```

doc2, doc3, search result --> 实际上，要搜索到doc2的agg_field的值是多少，doc3的agg_field的值是多少

doc2和doc3的agg_field的值之后，就可以根据值进行分组，实现terms bucket操作

doc2的agg_field的值是多少，这个时候，如果你手上只有一个倒排索引，你该怎么办？？？
* 你要扫描整个倒排索引，去一个一个的搜，拿到每个值
* 比如说agg1，看一下，它是不是doc2的值；拿到agg2，看一下，是不是doc2的值，直到找到doc2的agg_field的值，在倒排索引中
* 在倒排索引中找到一个值，发现它是属于某个doc的时候，还不能停，必须遍历完整个倒排索引，才能说确保找到了每个doc对应的所有terms，然后进行分组聚合
* 如果用纯倒排索引去实现聚合，性能是很低下的，很不现实。


倒排索引+正排索引（doc value）的原理和优势
接着上面的例子

先经过正排搜索出 search_field 所在的 doc

test --> doc2,doc3 --> search result, doc2,doc3

doc value数据结构，正排索引
...
...
...
100万个
doc2: agg1
doc3: agg2

不用搜索完整个正排索引啊？？1万个doc --> 搜 -> 可能跟搜索到15000次，就搜索完了，就找到了1万个doc的聚合field的所有值了，然后就可以执行分组聚合操作了


* doc value机制内核级原理深入探秘

1、doc value原理

（1）index-time生成

PUT/POST的时候，就会生成doc value数据，也就是正排索引

（2）核心原理与倒排索引类似

正排索引，也会写入磁盘文件中，然后呢，os cache先进行缓存，以提升访问doc value正排索引的性能
如果os cache内存大小不足够放得下整个正排索引，doc value，就会将doc value的数据写入磁盘文件中

（3）性能问题：给jvm更少内存，64g服务器，给jvm最多16g

es官方是建议，es大量是基于os cache来进行缓存和提升性能的，不建议用jvm内存来进行缓存，那样会导致一定的gc开销和oom问题
给jvm更少的内存，给os cache更大的内存
64g服务器，给jvm最多16g，几十个g的内存给os cache
os cache可以提升doc value和倒排索引的缓存和查询效率

2、column压缩

doc1: 550
doc2: 550
doc3: 500

合并相同值，550，doc1和doc2都保留一个550的标识即可

（1）所有值相同，直接保留单值
（2）少于256个值，使用table encoding模式：一种压缩方式
（3）大于256个值，看有没有最大公约数，有就除以最大公约数，然后保留这个最大公约数

doc1: 36
doc2: 24

6 --> doc1: 6, doc2: 4 --> 保留一个最大公约数6的标识，6也保存起来

（4）如果没有最大公约数，采取offset结合压缩的方式：


3、disable doc value

如果的确不需要doc value，比如聚合等操作，那么可以禁用，减少磁盘空间占用

PUT my_index
{
  "mappings": {
    "my_type": {
      "properties": {
        "my_field": {
          "type": "keyword"
          "doc_values": false 
        }
      }
    }
  }
}


#### 54 string field聚合实验以及fielddata原理初探

1、对于分词的field执行aggregation，发现报错。。。
```
GET /test_index/test_type/_search 
{
  "aggs": {
    "group_by_test_field": {
      "terms": {
        "field": "test_field"
      }
    }
  }
}
```

对分词的field，直接执行聚合操作，会报错：
你必须要打开fielddata，然后将正排索引数据加载到内存中，才可以对分词的field执行聚合操作，而且会消耗很大的内存

2、给分词的field，设置fielddata=true，发现可以执行，但是结果却。。。
```
POST /test_index/_mapping/test_type 
{
  "properties": {
    "test_field": {
      "type": "text",
      "fielddata": true
    }
  }
}
```
如果要对分词的field执行聚合操作，必须将fielddata设置为true

3、使用内置field不分词，对string field进行聚合
```
GET /test_index/test_type/_search 
{
  "size": 0,
  "aggs": {
    "group_by_test_field": {
      "terms": {
        "field": "test_field.keyword"
      }
    }
  }
}
```

如果对不分词的field执行聚合操作，直接就可以执行，不需要设置fieldata=true


4、分词field+fielddata的工作原理

doc value
* 不分词的所有field，可以执行聚合操作
* 如果你的某个field不分词，那么在index-time，就会自动生成doc value
* 针对这些不分词的field执行聚合操作的时候，自动就会用doc value来执行

* 分词field，是没有doc value的，所以直接对分词field执行聚合操作，是会报错的
* 在index-time，如果某个field是分词的，默认是不会给它建立doc value正排索引的，因为分词后占用的空间过于大
* 对于分词field，必须打开和使用fielddata=true，然后es就会在执行聚合操作的时候，现场将field对应的数据，建立一份fielddata正排索引,完全存在于纯内存中；
* 结构和doc value类似，如果是ngram或者是大量term，那么必将占用大量的内存。

为什么fielddata必须在内存？

因为大家自己思考一下，分词的字符串，需要按照term进行聚合，需要执行更加复杂的算法和操作，如果基于磁盘和os cache，那么性能会很差


#### 55_深入聚合数据分析_fielddata内存控制以及circuit breaker断路器

1. fielddata核心原理

fielddata加载到内存的过程是lazy加载的，对一个analzyed field执行聚合时，才会加载，而且是field-level加载的
一个index的一个field，所有doc都会被加载，而不是少数doc
不是index-time创建，是query-time创建

2. fielddata内存限制

indices.fielddata.cache.size: 20%，超出限制，清除内存已有fielddata数据
fielddata占用的内存超出了这个比例的限制，那么就清除掉内存中已有的fielddata数据
默认无限制，限制内存使用，但是会导致频繁evict和reload，大量IO性能损耗，以及内存碎片和gc

3. 监控fielddata内存使用

GET /_stats/fielddata?fields=*
GET /_nodes/stats/indices/fielddata?fields=*
GET /_nodes/stats/indices/fielddata?level=indices&fields=*


4. circuit breaker

如果一次query load的feilddata超过总内存，就会oom --> 内存溢出

circuit breaker会估算query要加载的fielddata大小，如果超出总内存，就短路，query直接失败

indices.breaker.fielddata.limit：fielddata的内存限制，默认60%
indices.breaker.request.limit：执行聚合的内存限制，默认40%
indices.breaker.total.limit：综合上面两个，限制在70%以内


* 56_fielddata filter的细粒度内存加载控制

```
POST /test_index/_mapping/my_type
{
  "properties": {
    "my_field": {
      "type": "text",
      "fielddata": { 
        "filter": {
          "frequency": {
            "min":              0.01, 
            "min_segment_size": 500  
          }
        }
      }
    }
  }
}
```
* min：仅仅加载至少在1%的doc中出现过的term对应的fielddata
       比如说某个值，hello，总共有1000个doc，hello必须在10个doc中出现，那么这个hello对应的fielddata才会加载到内存中来

* min_segment_size：少于500 doc的segment不加载fielddata

加载fielddata的时候，也是按照segment去进行加载的，某个segment里面的doc数量少于500个，那么这个segment的fielddata就不加载

这个，就我的经验来看，有点底层了，一般不会去设置它，大家知道就好


* 57 fielddata预加载机制以及序号标记预加载

如果真的要对分词的field执行聚合，那么每次都在query-time现场生产fielddata并加载到内存中来，速度可能会比较慢

我们是不是可以预先生成加载fielddata到内存中来？？？

1、fielddata预加载
```
POST /test_index/_mapping/test_type
{
  "properties": {
    "test_field": {
      "type": "string",
      "fielddata": {
        "loading" : "eager" 
      }
    }
  }
}
```
query-time的fielddata生成和加载到内存，变为index-time，建立倒排索引的时候，会同步生成fielddata并且加载到内存中来，
这样对分词field的聚合性能当然会大幅度增强

2、序号标记预加载

global ordinal原理解释

doc1: status1
doc2: status2
doc3: status2
doc4: status1

有很多重复值的情况，会进行global ordinal标记

status1 --> 0
status2 --> 1

doc1: 0
doc2: 1
doc3: 1
doc4: 0

建立的fielddata也会是这个样子的，这样的好处就是减少重复字符串的出现的次数，减少内存的消耗
```
POST /test_index/_mapping/test_type
{
  "properties": {
    "test_field": {
      "type": "string",
      "fielddata": {
        "loading" : "eager_global_ordinals" 
      }
    }
  }
}
```

#### 58海量bucket优化机制：从深度优先到广度优先

我们的数据，是每个演员的每个电影的评论

每个演员的评论的数量 --> 每个演员的每个电影的评论的数量

评论数量排名前10个的演员 --> 每个演员的电影取到评论数量排名前5的电影

{
  "aggs" : {
    "actors" : {
      "terms" : {
         "field" :        "actors",
         "size" :         10,
         "collect_mode" : "breadth_first" 
      },
      "aggs" : {
        "costars" : {
          "terms" : {
            "field" : "films",
            "size" :  5
          }
        }
      }
    }
  }
}

深度优先的方式去执行聚合操作的

    actor1            actor2            .... actor
film1 film2 film3   film1 film2 film3   ...film

比如说，我们有10万个actor，最后其实是主要10个actor就可以了

但是深度优先的方式，构建了一整颗完整的树出来了，10万个actor，每个actor平均有10部电影，10万 + 100万 --> 110万的数据量的一颗树

裁剪掉10万个actor中的99990 actor，99990 * 10 = film，剩下10个actor，每个actor的10个film裁剪掉5个，110万 --> 10 * 5 = 50个

构建了大量的数据，然后裁剪掉了99.99%的数据，浪费了

广度优先的方式去执行聚合

actor1    actor2    actor3    ..... n个actor

10万个actor，不去构建它下面的film数据，10万 --> 99990，10个actor，构建出film，裁剪出其中的5个film即可，10万 -> 50个

10倍


###   59_数据建模实战

#### 关系型与document类型数据模型对比

关系型数据库的数据模型

三范式 --> 将每个数据实体拆分为一个独立的数据表，同时使用主外键关联关系将多个数据表关联起来 --> 确保没有任何冗余的数据

一份数据 部门名称，只会放在一个数据表 department表 中，如果要查看某个员工的部门名称，那么必须通过员工表中的外键，dept_id，找到在部门表中对应的记录，然后找到部门名称

```
public class Department {
  
  private Integer deptId;
  private String name;
  private String desc;
  private List<Employee> employees;

}

public class Employee {
  
  private Integer empId;
  private String name;
  private Integer age;
  private String gender;
  private Department dept;

}

关系型数据库

department表

dept_id
name
desc

employee表

emp_id
name
age
gender
dept_id
```

es文档数据模型：更加类似于面向对象的数据模型，将所有由关联关系的数据，放在一个doc json类型数据中，整个数据的关系，还有完整的数据，都放在了一起
```
{
  "deptId": "1",
  "name": "研发部门",
  "desc": "负责公司的所有研发项目",
  "employees": [
    {
      "empId": "1",
      "name": "张三",
      "age": 28,
      "gender": "男"
    },
    {
      "empId": "2",
      "name": "王兰",
      "age": 25,
      "gender": "女"
    },
    {
      "empId": "3",
      "name": "李四",
      "age": 34,
      "gender": "男"
    }
  ]
}
```

* 通过应用层join实现用户与博客的关联

1、构造用户与博客数据

在构造数据模型的时候，还是将有关联关系的数据，然后分割为不同的实体，类似于关系型数据库中的模型

案例背景：博客网站， 我们会模拟各种用户发表各种博客，然后针对用户和博客之间的关系进行数据建模，同时针对建模好的数据执行各种搜索/聚合的操作

PUT /website/users/1 
{
  "name":     "小鱼儿",
  "email":    "xiaoyuer@sina.com",
  "birthday":      "1980-01-01"
}

PUT /website/blogs/1
{
  "title":    "我的第一篇博客",
  "content":     "这是我的第一篇博客，开通啦！！！"
  "userId":     1 
}

一个用户对应多个博客，一对多的关系，做了建模

建模方式，分割实体，类似三范式的方式，用主外键关联关系，将多个实体关联起来

2、搜索小鱼儿发表的所有博客
```
// 先搜索用户ID
// 如果这里搜索的是，1万个用户的博客，会得到1万个userId，量太大
GET /website/users/_search 
{
  "query": {
    "term": {
      "name.keyword": {
        "value": "小鱼儿"
      }
    }
  }
}

// 第二次搜索的时候，要放入terms中1万个userId，才能进行搜索，这个时候性能比较差了
GET /website/blogs/_search 
{
  "query": {
    "constant_score": {
      "filter": {
        "terms": {
          "userId": [
            1
          ]
        }
      }
    }
  }
}
```
上面的操作，就属于应用层的join，在应用层先查出一份数据，然后再查出一份数据，进行关联

3、优点和缺点

优点：数据不冗余，维护方便
缺点：应用层join，如果关联数据过多，导致查询过大，性能很差


* 通过数据冗余实现用户与博客的关联

1、第二种建模方式

* 用冗余数据，将可能会进行搜索的条件和要搜索的数据，放在一个doc中
* 采用文档数据模型，进行数据建模，实现用户和博客的关联

```
PUT /website/users/1
{
  "name":     "小鱼儿",
  "email":    "xiaoyuer@sina.com",
  "birthday":      "1980-01-01"
}

PUT /website/blogs/1
{
  "title": "小鱼儿的第一篇博客",
  "content": "大家好，我是小鱼儿。。。",
  "userInfo": {
    "userId": 1,
    "username": "小鱼儿"
  }
}
```
2、基于冗余用户数据搜索博客
```
GET /website/blogs/_search 
{
  "query": {
    "term": {
      "userInfo.username.keyword": {
        "value": "小鱼儿"
      }
    }
  }
}
```

就不需要走应用层的join，先搜一个数据，找到id，再去搜另一份数据

直接走一个有冗余数据的type即可，指定要的搜索条件，即可搜索出自己想要的数据来

3、优点和缺点

优点：性能高，不需要执行两次搜索
缺点：数据冗余，维护成本高 --> 每次如果冗余数据username变化了，必须记得同时要更新user type和blog type

一般来说，对于es这种NoSQL类型的数据存储来讲，都是冗余模式....


#### 62 对每个用户发表的博客进行分组

1、构造更多测试数据
```
PUT /website/users/3
{
  "name": "黄药师",
  "email": "huangyaoshi@sina.com",
  "birthday": "1970-10-24"
}

PUT /website/blogs/3
{
  "title": "我是黄药师",
  "content": "我是黄药师啊，各位同学们！！！",
  "userInfo": {
    "userId": 1,
    "userName": "黄药师"
  }
}

PUT /website/users/2
{
  "name": "花无缺",
  "email": "huawuque@sina.com",
  "birthday": "1980-02-02"
}

PUT /website/blogs/4
{
  "title": "花无缺的身世揭秘",
  "content": "大家好，我是花无缺，所以我的身世是。。。",
  "userInfo": {
    "userId": 2,
    "userName": "花无缺"
  }
}
```

2、对每个用户发表的博客进行分组

比如说，小鱼儿发表的那些博客，花无缺发表了哪些博客，黄药师发表了哪些博客
```
GET /website/blogs/_search 
{
  "size": 0, 
  "aggs": {
    "group_by_username": {
      "terms": {
        "field": "userInfo.username.keyword"
      },
      "aggs": {
        "top_blogs": {
          "top_hits": {
            "_source": {
              "include": "title"
            }, 
            "size": 5
          }
        }
      }
    }
  }
}
```
#### 61 对文件系统进行数据建模以及文件搜索实战

数据建模，对类似文件系统这种的有多层级关系的数据进行建模

1、文件系统数据构造

PUT /fs
{
  "settings": {
    "analysis": {
      "analyzer": {
        "paths": { 
          "tokenizer": "path_hierarchy"
        }
      }
    }
  }
}

path_hierarchy tokenizer讲解

/a/b/c/d --> path_hierarchy -> /a/b/c/d, /a/b/c, /a/b, /a

PUT /fs/_mapping/file
{
  "properties": {
    "name": { 
      "type":  "keyword"
    },
    "path": { 
      "type":  "keyword",
      "fields": {
        "tree": { 
          "type":     "text",
          "analyzer": "paths"
        }
      }
    }
  }
}

PUT /fs/file/1
{
  "name":     "README.txt", 
  "path":     "/workspace/projects/helloworld", 
  "contents": "这是我的第一个elasticsearch程序"
}

2、对文件系统执行搜索

文件搜索需求：查找一份，内容包括elasticsearch，在/workspace/projects/hellworld这个目录下的文件

GET /fs/file/_search 
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "contents": "elasticsearch"
          }
        },
        {
          "constant_score": {
            "filter": {
              "term": {
                "path": "/workspace/projects/helloworld"
              }
            }
          }
        }
      ]
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
    "max_score": 1.284885,
    "hits": [
      {
        "_index": "fs",
        "_type": "file",
        "_id": "1",
        "_score": 1.284885,
        "_source": {
          "name": "README.txt",
          "path": "/workspace/projects/helloworld",
          "contents": "这是我的第一个elasticsearch程序"
        }
      }
    ]
  }
}

搜索需求2：搜索/workspace目录下，内容包含elasticsearch的所有的文件

/workspace/projects/helloworld    doc1
/workspace/projects               doc1
/workspace                        doc1

GET /fs/file/_search 
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "contents": "elasticsearch"
          }
        },
        {
          "constant_score": {
            "filter": {
              "term": {
                "path.tree": "/workspace"
              }
            }
          }
        }
      ]
    }
  }
}

#### 64 基于全局锁实现悲观锁并发控制

1、悲观锁的简要说明

基于version的乐观锁并发控制

在数据建模，结合文件系统建模的这个案例，把悲观锁的并发控制，3种锁粒度，都给大家仔细讲解一下

最粗的一个粒度，全局锁

/workspace/projects/helloworld

如果多个线程，都过来，要并发地给/workspace/projects/helloworld下的README.txt修改文件名

实际上要进行并发的控制，避免出现多线程的并发安全问题，比如多个线程修改，纯并发，先执行的修改操作被后执行的修改操作给覆盖了

get current version

带着这个current version去执行修改，如果一旦发现数据已经被别人给修改了，version号跟之前自己获取的已经不一样了; 那么必须重新获取新的version号再次尝试修改

上来就尝试给这条数据加个锁，然后呢，此时就只有你能执行各种各样的操作了，其他人不能执行操作

第一种锁：全局锁，直接锁掉整个fs index

2、全局锁的上锁实验

PUT /fs/lock/global/_create
{}

fs: 你要上锁的那个index
lock: 就是你指定的一个对这个index上全局锁的一个type
global: 就是你上的全局锁对应的这个doc的id
_create：强制必须是创建，如果/fs/lock/global这个doc已经存在，那么创建失败，报错

利用了doc来进行上锁

/fs/lock/global /index/type/id --> doc

{
  "_index": "fs",
  "_type": "lock",
  "_id": "global",
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "created": true
}

另外一个线程同时尝试上锁

PUT /fs/lock/global/_create
{}

{
  "error": {
    "root_cause": [
      {
        "type": "version_conflict_engine_exception",
        "reason": "[lock][global]: version conflict, document already exists (current version [1])",
        "index_uuid": "IYbj0OLGQHmMUpLfbhD4Hw",
        "shard": "2",
        "index": "fs"
      }
    ],
    "type": "version_conflict_engine_exception",
    "reason": "[lock][global]: version conflict, document already exists (current version [1])",
    "index_uuid": "IYbj0OLGQHmMUpLfbhD4Hw",
    "shard": "2",
    "index": "fs"
  },
  "status": 409
}

如果失败，就再次重复尝试上锁

执行各种操作。。。

POST /fs/file/1/_update
{
  "doc": {
    "name": "README1.txt"
  }
}

{
  "_index": "fs",
  "_type": "file",
  "_id": "1",
  "_version": 2,
  "result": "updated",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}

DELETE /fs/lock/global

{
  "found": true,
  "_index": "fs",
  "_type": "lock",
  "_id": "global",
  "_version": 2,
  "result": "deleted",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}

另外一个线程，因为之前发现上锁失败，反复尝试重新上锁，终于上锁成功了，因为之前获取到全局锁的那个线程已经delete /fs/lock/global全局锁了

PUT /fs/lock/global/_create
{}

{
  "_index": "fs",
  "_type": "lock",
  "_id": "global",
  "_version": 3,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "created": true
}

POST /fs/file/1/_update 
{
  "doc": {
    "name": "README.txt"
  }
}

{
  "_index": "fs",
  "_type": "file",
  "_id": "1",
  "_version": 3,
  "result": "updated",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}

DELETE /fs/lock/global

3、全局锁的优点和缺点

优点：操作非常简单，非常容易使用，成本低
缺点：你直接就把整个index给上锁了，这个时候对index中所有的doc的操作，都会被block住，导致整个系统的并发能力很低

上锁解锁的操作不是频繁，然后每次上锁之后，执行的操作的耗时不会太长，用这种方式，方便


#### 65_数据建模实战_基于document锁实现悲观锁并发控制
1、对document level锁，详细的讲解
细粒度的一个锁，document锁，顾名思义，每次就锁你要操作的，你要执行增删改的那些doc，doc锁了，其他线程就不能对这些doc执行增删改操作了
但是你只是锁了部分doc，其他线程对其他的doc还是可以上锁和执行增删改操作的

document锁，是用脚本进行上锁
```
POST /fs/lock/1/_update
{
  "upsert": { "process_id": 123 },
  "script": "if ( ctx._source.process_id != process_id ) { assert false }; ctx.op = 'noop';"
  "params": {
    "process_id": 123
  }
}

/fs/lock，是固定的，就是说fs下的lock type，专门用于进行上锁
/fs/lock/id，比如1，id其实就是你要上锁的那个doc的id，代表了某个doc数据对应的lock（也是一个doc）
_update + upsert：执行upsert操作

params，里面有个process_id，process_id，是你的要执行增删改操作的进程的唯一id，比如说可以在java系统，启动的时候，给你的每个线程都用UUID自动生成一个thread id，你的系统进程启动的时候给整个进程也分配一个UUID。process_id + thread_id就代表了某一个进程下的某个线程的唯一标识。可以自己用UUID生成一个唯一ID

process_id很重要，会在lock中，设置对对应的doc加锁的进程的id，这样其他进程过来的时候，才知道，这条数据已经被别人给锁了

assert false，不是当前进程加锁的话，则抛出异常
ctx.op='noop'，不做任何修改
```

如果该document之前没有被锁，/fs/lock/1之前不存在，也就是doc id=1没有被别人上过锁; upsert的语法，那么执行index操作，创建一个/fs/lock/id这条数据，而且用params中的数据作为这个lock的数据。process_id被设置为123，script不执行。
这个时候象征着process_id=123的进程已经锁了一个doc了。

如果document被锁了，就是说/fs/lock/1已经存在了，代表doc id=1已经被某个进程给锁了。
那么执行update操作，script，此时会比对process_id，如果相同，就是说，某个进程，之前锁了这个doc，然后这次又过来，就可以直接对这个doc执行操作，说明是该进程之前锁的doc，则不报错，不执行任何操作，返回success; 如果process_id比对不上，说明doc被其他doc给锁了，此时报错

/fs/lock/1
{
  "process_id": 123
}

POST /fs/lock/1/_update
{
  "upsert": { "process_id": 123 },
  "script": "if ( ctx._source.process_id != process_id ) { assert false }; ctx.op = 'noop';"
  "params": {
    "process_id": 123
  }
}


script：ctx._source.process_id，123
process_id：加锁的upsert请求中带过来额proess_id

如果两个process_id相同，说明是一个进程先加锁，然后又过来尝试加锁，可能是要执行另外一个操作，此时就不会block，对同一个process_id是不会block，ctx.op= 'noop'，什么都不做，返回一个success

如果说已经有一个进程加了锁了

/fs/lock/1
{
  "process_id": 123
}

POST /fs/lock/1/_update
{
  "upsert": { "process_id": 123 },
  "script": "if ( ctx._source.process_id != process_id ) { assert false }; ctx.op = 'noop';"
  "params": {
    "process_id": 234
  }
}

"script": "if ( ctx._source.process_id != process_id ) { assert false }; ctx.op = 'noop';"

ctx._source.process_id：123
process_id: 234

process_id不相等，说明这个doc之前已经被别人上锁了，process_id=123上锁了; process_id=234过来再次尝试上锁，失败，assert false，就会报错

此时遇到报错的process，就应该尝试重新上锁，直到上锁成功

有报错的话，如果有些doc被锁了，那么需要重试

直到所有锁定都成功，执行自己的操作。。。

释放所有的锁

2、上document锁的完整实验过程
```
scripts/judge-lock.groovy: if ( ctx._source.process_id != process_id ) { assert false }; ctx.op = 'noop';

POST /fs/lock/1/_update
{
  "upsert": { "process_id": 123 },
  "script": {
    "lang": "groovy",
    "file": "judge-lock", 
    "params": {
      "process_id": 123
    }
  }
}

{
  "_index": "fs",
  "_type": "lock",
  "_id": "1",
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}

GET /fs/lock/1

{
  "_index": "fs",
  "_type": "lock",
  "_id": "1",
  "_version": 1,
  "found": true,
  "_source": {
    "process_id": 123
  }
}

POST /fs/lock/1/_update
{
  "upsert": { "process_id": 234 },
  "script": {
    "lang": "groovy",
    "file": "judge-lock", 
    "params": {
      "process_id": 234
    }
  }
}

{
  "error": {
    "root_cause": [
      {
        "type": "remote_transport_exception",
        "reason": "[4onsTYV][127.0.0.1:9300][indices:data/write/update[s]]"
      }
    ],
    "type": "illegal_argument_exception",
    "reason": "failed to execute script",
    "caused_by": {
      "type": "script_exception",
      "reason": "error evaluating judge-lock",
      "caused_by": {
        "type": "power_assertion_error",
        "reason": "assert false\n"
      },
      "script_stack": [],
      "script": "",
      "lang": "groovy"
    }
  },
  "status": 400
}

POST /fs/lock/1/_update
{
  "upsert": { "process_id": 123 },
  "script": {
    "lang": "groovy",
    "file": "judge-lock", 
    "params": {
      "process_id": 123
    }
  }
}

{
  "_index": "fs",
  "_type": "lock",
  "_id": "1",
  "_version": 1,
  "result": "noop",
  "_shards": {
    "total": 0,
    "successful": 0,
    "failed": 0
  }
}

POST /fs/file/1/_update
{
  "doc": {
    "name": "README1.txt"
  }
}

{
  "_index": "fs",
  "_type": "file",
  "_id": "1",
  "_version": 4,
  "result": "updated",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}

POST /fs/_refresh 

GET /fs/lock/_search?scroll=1m
{
  "query": {
    "term": {
      "process_id": 123
    }
  }
}

{
  "_scroll_id": "DnF1ZXJ5VGhlbkZldGNoBQAAAAAAACPkFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAAj5RY0b25zVFlWWlRqR3ZJajlfc3BXejJ3AAAAAAAAI-YWNG9uc1RZVlpUakd2SWo5X3NwV3oydwAAAAAAACPnFjRvbnNUWVZaVGpHdklqOV9zcFd6MncAAAAAAAAj6BY0b25zVFlWWlRqR3ZJajlfc3BXejJ3",
  "took": 51,
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
        "_index": "fs",
        "_type": "lock",
        "_id": "1",
        "_score": 1,
        "_source": {
          "process_id": 123
        }
      }
    ]
  }
}

PUT /fs/lock/_bulk
{ "delete": { "_id": 1}}

{
  "took": 20,
  "errors": false,
  "items": [
    {
      "delete": {
        "found": true,
        "_index": "fs",
        "_type": "lock",
        "_id": "1",
        "_version": 2,
        "result": "deleted",
        "_shards": {
          "total": 2,
          "successful": 1,
          "failed": 0
        },
        "status": 200
      }
    }
  ]
}

POST /fs/lock/1/_update
{
  "upsert": { "process_id": 234 },
  "script": {
    "lang": "groovy",
    "file": "judge-lock", 
    "params": {
      "process_id": 234
    }
  }
}
```
process_id=234上锁就成功了


#### 66 基于共享锁和排他锁实现悲观锁并发控制
1、共享锁和排他锁的说明

共享锁：这份数据是共享的，然后多个线程过来，都可以获取同一个数据的共享锁，然后对这个数据执行读操作
排他锁：是排他的操作，只能一个线程获取排他锁，然后执行增删改操作

读写锁的分离

如果只是要读取数据的话，那么任意个线程都可以同时进来然后读取数据，每个线程都可以上一个共享锁
但是这个时候，如果有线程要过来修改数据，那么会尝试上排他锁，排他锁会跟共享锁互斥，也就是说，如果有人已经上了共享锁了，那么排他锁就不能上，就得等

如果有人在读数据，就不允许别人来修改数据

反之，也是一样的

如果有人在修改数据，就是加了排他锁
那么其他线程过来要修改数据，也会尝试加排他锁，此时会失败，锁冲突，必须等待，同时只能有一个线程修改数据
如果有人过来同时要读取数据，那么会尝试加共享锁，此时会失败，因为共享锁和排他锁是冲突的

如果有在修改数据，就不允许别人来修改数据，也不允许别人来读取数据

2、共享锁和排他锁的实验

第一步：有人在读数据，其他人也能过来读数据

judge-lock-2.groovy: if (ctx._source.lock_type == 'exclusive') { assert false }; ctx._source.lock_count++

POST /fs/lock/1/_update 
{
  "upsert": { 
    "lock_type":  "shared",
    "lock_count": 1
  },
  "script": {
    "lang": "groovy",
    "file": "judge-lock-2"
  }
}

POST /fs/lock/1/_update 
{
  "upsert": { 
    "lock_type":  "shared",
    "lock_count": 1
  },
  "script": {
    "lang": "groovy",
    "file": "judge-lock-2"
  }
}

GET /fs/lock/1

{
  "_index": "fs",
  "_type": "lock",
  "_id": "1",
  "_version": 3,
  "found": true,
  "_source": {
    "lock_type": "shared",
    "lock_count": 3
  }
}

就给大家模拟了，有人上了共享锁，你还是要上共享锁，直接上就行了，没问题，只是lock_count加1

2、已经有人上了共享锁，然后有人要上排他锁

PUT /fs/lock/1/_create
{ "lock_type": "exclusive" }

排他锁用的不是upsert语法，create语法，要求lock必须不能存在，直接自己是第一个上锁的人，上的是排他锁

{
  "error": {
    "root_cause": [
      {
        "type": "version_conflict_engine_exception",
        "reason": "[lock][1]: version conflict, document already exists (current version [3])",
        "index_uuid": "IYbj0OLGQHmMUpLfbhD4Hw",
        "shard": "3",
        "index": "fs"
      }
    ],
    "type": "version_conflict_engine_exception",
    "reason": "[lock][1]: version conflict, document already exists (current version [3])",
    "index_uuid": "IYbj0OLGQHmMUpLfbhD4Hw",
    "shard": "3",
    "index": "fs"
  },
  "status": 409
}

如果已经有人上了共享锁，明显/fs/lock/1是存在的，create语法去上排他锁，肯定会报错

3、对共享锁进行解锁

POST /fs/lock/1/_update
{
  "script": {
    "lang": "groovy",
    "file": "unlock-shared"
  }
}

连续解锁3次，此时共享锁就彻底没了

每次解锁一个共享锁，就对lock_count先减1，如果减了1之后，是0，那么说明所有的共享锁都解锁完了，此时就就将/fs/lock/1删除，就彻底解锁所有的共享锁

3、上排他锁，再上排他锁

PUT /fs/lock/1/_create
{ "lock_type": "exclusive" }

其他线程

PUT /fs/lock/1/_create
{ "lock_type": "exclusive" }

{
  "error": {
    "root_cause": [
      {
        "type": "version_conflict_engine_exception",
        "reason": "[lock][1]: version conflict, document already exists (current version [7])",
        "index_uuid": "IYbj0OLGQHmMUpLfbhD4Hw",
        "shard": "3",
        "index": "fs"
      }
    ],
    "type": "version_conflict_engine_exception",
    "reason": "[lock][1]: version conflict, document already exists (current version [7])",
    "index_uuid": "IYbj0OLGQHmMUpLfbhD4Hw",
    "shard": "3",
    "index": "fs"
  },
  "status": 409
}

4、上排他锁，上共享锁

POST /fs/lock/1/_update 
{
  "upsert": { 
    "lock_type":  "shared",
    "lock_count": 1
  },
  "script": {
    "lang": "groovy",
    "file": "judge-lock-2"
  }
}

{
  "error": {
    "root_cause": [
      {
        "type": "remote_transport_exception",
        "reason": "[4onsTYV][127.0.0.1:9300][indices:data/write/update[s]]"
      }
    ],
    "type": "illegal_argument_exception",
    "reason": "failed to execute script",
    "caused_by": {
      "type": "script_exception",
      "reason": "error evaluating judge-lock-2",
      "caused_by": {
        "type": "power_assertion_error",
        "reason": "assert false\n"
      },
      "script_stack": [],
      "script": "",
      "lang": "groovy"
    }
  },
  "status": 400
}

5、解锁排他锁

DELETE /fs/lock/1

#### 67 基于nested object实现博客与评论嵌套关系
1、做一个实验，引出来为什么需要nested object

冗余数据方式的来建模，其实用的就是object类型，我们这里又要引入一种新的object类型，nested object类型

博客评论这种数据模型
```
PUT /website/blogs/6
{
  "title": "花无缺发表的一篇帖子",
  "content":  "我是花无缺，大家要不要考虑一下投资房产和买股票的事情啊。。。",
  "tags":  [ "投资", "理财" ],
  "comments": [ 
    {
      "name":    "小鱼儿",
      "comment": "什么股票啊？推荐一下呗",
      "age":     28,
      "stars":   4,
      "date":    "2016-09-01"
    },
    {
      "name":    "黄药师",
      "comment": "我喜欢投资房产，风，险大收益也大",
      "age":     31,
      "stars":   5,
      "date":    "2016-10-22"
    }
  ]
}
```
被年龄是28岁的黄药师评论过的博客，搜索：
```
GET /website/blogs/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "comments.name": "黄药师" }},
        { "match": { "comments.age":  28      }} 
      ]
    }
  }
}

{
  "took": 102,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 1,
    "max_score": 1.8022683,
    "hits": [
      {
        "_index": "website",
        "_type": "blogs",
        "_id": "6",
        "_score": 1.8022683,
        "_source": {
          "title": "花无缺发表的一篇帖子",
          "content": "我是花无缺，大家要不要考虑一下投资房产和买股票的事情啊。。。",
          "tags": [
            "投资",
            "理财"
          ],
          "comments": [
            {
              "name": "小鱼儿",
              "comment": "什么股票啊？推荐一下呗",
              "age": 28,
              "stars": 4,
              "date": "2016-09-01"
            },
            {
              "name": "黄药师",
              "comment": "我喜欢投资房产，风，险大收益也大",
              "age": 31,
              "stars": 5,
              "date": "2016-10-22"
            }
          ]
        }
      }
    ]
  }
}
```
居然能出来结果，好像不太对啊？黄药师是28岁？？

object类型数据结构的底层存储会将一个json数组中的数据，进行扁平化：
```
{
  "title":            [ "花无缺", "发表", "一篇", "帖子" ],
  "content":             [ "我", "是", "花无缺", "大家", "要不要", "考虑", "一下", "投资", "房产", "买", "股票", "事情" ],
  "tags":             [ "投资", "理财" ],
  "comments.name":    [ "小鱼儿", "黄药师" ],
  "comments.comment": [ "什么", "股票", "推荐", "我", "喜欢", "投资", "房产", "风险", "收益", "大" ],
  "comments.age":     [ 28, 31 ],
  "comments.stars":   [ 4, 5 ],
  "comments.date":    [ 2016-09-01, 2016-10-22 ]
}
```
所以，直接命中了这个document，name=黄药师，age=28，正好符合

2、引入nested object类型，来解决object类型底层数据结构导致的问题

修改mapping，将comments的类型从object设置为nested
```
PUT /website
{
  "mappings": {
    "blogs": {
      "properties": {
        "comments": {
          "type": "nested", 
          "properties": {
            "name":    { "type": "string"  },
            "comment": { "type": "string"  },
            "age":     { "type": "short"   },
            "stars":   { "type": "short"   },
            "date":    { "type": "date"    }
          }
        }
      }
    }
  }
}

{ 
  "comments.name":    [ "小鱼儿" ],
  "comments.comment": [ "什么", "股票", "推荐" ],
  "comments.age":     [ 28 ],
  "comments.stars":   [ 4 ],
  "comments.date":    [ 2014-09-01 ]
}
{ 
  "comments.name":    [ "黄药师" ],
  "comments.comment": [ "我", "喜欢", "投资", "房产", "风险", "收益", "大" ],
  "comments.age":     [ 31 ],
  "comments.stars":   [ 5 ],
  "comments.date":    [ 2014-10-22 ]
}
{ 
  "title":            [ "花无缺", "发表", "一篇", "帖子" ],
  "body":             [ "我", "是", "花无缺", "大家", "要不要", "考虑", "一下", "投资", "房产", "买", "股票", "事情" ],
  "tags":             [ "投资", "理财" ]
}
```

再次搜索，成功了
```
GET /website/blogs/_search 
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": "花无缺"
          }
        },
        {
          "nested": {
            "path": "comments",
            "query": {
              "bool": {
                "must": [
                  {
                    "match": {
                      "comments.name": "黄药师"
                    }
                  },
                  {
                    "match": {
                      "comments.age": 28
                    }
                  }
                ]
              }
            }
          }
        }
      ]
    }
  }
}

score_mode：max，min，avg，none，默认是avg
```
如果搜索命中了多个nested document，如何讲个多个nested document的分数合并为一个分数？

=====================


* 68 对嵌套的博客评论数据进行聚合分析

聚合数据分析的需求1：按照评论日期进行bucket划分，然后拿到每个月的评论的评分的平均值
```
GET /website/blogs/_search 
{
  "size": 0, 
  "aggs": {
    "comments_path": {
      "nested": {
        "path": "comments"
      }, 
      "aggs": {
        "group_by_comments_date": {
          "date_histogram": {
            "field": "comments.date",
            "interval": "month",
            "format": "yyyy-MM"
          },
          "aggs": {
            "avg_stars": {
              "avg": {
                "field": "comments.stars"
              }
            }
          }
        }
      }
    }
  }
}

{
  "took": 52,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 2,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "comments_path": {
      "doc_count": 4,
      "group_by_comments_date": {
        "buckets": [
          {
            "key_as_string": "2016-08",
            "key": 1470009600000,
            "doc_count": 1,
            "avg_stars": {
              "value": 3
            }
          },
          {
            "key_as_string": "2016-09",
            "key": 1472688000000,
            "doc_count": 2,
            "avg_stars": {
              "value": 4.5
            }
          },
          {
            "key_as_string": "2016-10",
            "key": 1475280000000,
            "doc_count": 1,
            "avg_stars": {
              "value": 5
            }
          }
        ]
      }
    }
  }
}



GET /website/blogs/_search 
{
  "size": 0,
  "aggs": {
    "comments_path": {
      "nested": {
        "path": "comments"
      },
      "aggs": {
        "group_by_comments_age": {
          "histogram": {
            "field": "comments.age",
            "interval": 10
          },
          "aggs": {
            "reverse_path": {
              "reverse_nested": {}, 
              "aggs": {
                "group_by_tags": {
                  "terms": {
                    "field": "tags.keyword"
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

{
  "took": 5,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 2,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "comments_path": {
      "doc_count": 4,
      "group_by_comments_age": {
        "buckets": [
          {
            "key": 20,
            "doc_count": 1,
            "reverse_path": {
              "doc_count": 1,
              "group_by_tags": {
                "doc_count_error_upper_bound": 0,
                "sum_other_doc_count": 0,
                "buckets": [
                  {
                    "key": "投资",
                    "doc_count": 1
                  },
                  {
                    "key": "理财",
                    "doc_count": 1
                  }
                ]
              }
            }
          },
          {
            "key": 30,
            "doc_count": 3,
            "reverse_path": {
              "doc_count": 2,
              "group_by_tags": {
                "doc_count_error_upper_bound": 0,
                "sum_other_doc_count": 0,
                "buckets": [
                  {
                    "key": "大侠",
                    "doc_count": 1
                  },
                  {
                    "key": "投资",
                    "doc_count": 1
                  },
                  {
                    "key": "理财",
                    "doc_count": 1
                  },
                  {
                    "key": "练功",
                    "doc_count": 1
                  }
                ]
              }
            }
          }
        ]
      }
    }
  }
}
```


#### 69 父子关系数据建模：研发中心管理案例

nested object的建模，有个不好的地方，就是采取的是类似冗余数据的方式，将多个数据都放在一起了，维护成本就比较高

parent child建模方式，采取的是类似于关系型数据库的三范式类的建模：
* 多个实体都分割开来，每个实体之间都通过一些关联方式，进行了父子关系的关联；
* 各种数据不需要都放在一起，父doc和子doc分别在进行更新的时候，都不会影响对方


一对多关系的建模，维护起来比较方便，类似关系型数据库的建模方式，应用层join的方式，会导致性能比较差，因为做多次搜索。
父子关系的数据模型，不会，性能很好。
因为虽然数据实体之间分割开来，但是我们在搜索的时候，由es自动为我们处理底层的关联关系，并且通过一些手段保证搜索性能。

父子关系数据模型，相对于nested数据模型来说，优点是父doc和子doc互相之间不会影响

要点：父子关系元数据映射，用于确保查询时候的高性能，但是有一个限制，就是父子数据必须存在于一个shard中

父子关系数据存在一个shard中，而且还有映射其关联关系的元数据，那么搜索父子关系数据的时候，不用跨分片，一个分片本地自己就搞定了，性能当然高咯


案例背景：研发中心员工管理案例，一个IT公司有多个研发中心，每个研发中心有多个员工
```
PUT /company
{
  "mappings": {
    "rd_center": {},
    "employee": {
      "_parent": {
        "type": "rd_center" 
      }
    }
  }
}
```
父子关系建模的核心，多个type之间有父子关系，用_parent指定父type

POST /company/rd_center/_bulk
{ "index": { "_id": "1" }}
{ "name": "北京研发总部", "city": "北京", "country": "中国" }
{ "index": { "_id": "2" }}
{ "name": "上海研发中心", "city": "上海", "country": "中国" }
{ "index": { "_id": "3" }}
{ "name": "硅谷人工智能实验室", "city": "硅谷", "country": "美国" }

shard路由的时候，id=1的rd_center doc，默认会根据id进行路由，到某一个shard

PUT /company/employee/1?parent=1 
{
  "name":  "张三",
  "birthday":   "1970-10-24",
  "hobby": "爬山"
}

维护父子关系的核心，parent=1，指定了这个数据的父doc的id

此时，parent-child关系，就确保了说，父doc和子doc都是保存在一个shard上的。

内部原理还是doc routing，employee和rd_center的数据，都会用parent id作为routing，这样就会到一个shard

就不会根据id=1的employee doc的id进行路由了，而是根据parent=1进行路由，会根据父doc的id进行路由，
那么就可以通过底层的路由机制，保证父子数据存在于一个shard中

POST /company/employee/_bulk
{ "index": { "_id": 2, "parent": "1" }}
{ "name": "李四", "birthday": "1982-05-16", "hobby": "游泳" }
{ "index": { "_id": 3, "parent": "2" }}
{ "name": "王二", "birthday": "1979-04-01", "hobby": "爬山" }
{ "index": { "_id": 4, "parent": "3" }}
{ "name": "赵五", "birthday": "1987-05-11", "hobby": "骑马" }


===========================

* 70 根据员工信息和研发中心互相搜索父子数据

我们已经建立了父子关系的数据模型之后，就要基于这个模型进行各种搜索和聚合了

1、搜索有1980年以后出生的员工的研发中心
```
GET /company/rd_center/_search
{
  "query": {
    "has_child": {
      "type": "employee",
      "query": {
        "range": {
          "birthday": {
            "gte": "1980-01-01"
          }
        }
      }
    }
  }
}

{
  "took": 33,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 2,
    "max_score": 1,
    "hits": [
      {
        "_index": "company",
        "_type": "rd_center",
        "_id": "1",
        "_score": 1,
        "_source": {
          "name": "北京研发总部",
          "city": "北京",
          "country": "中国"
        }
      },
      {
        "_index": "company",
        "_type": "rd_center",
        "_id": "3",
        "_score": 1,
        "_source": {
          "name": "硅谷人工智能实验室",
          "city": "硅谷",
          "country": "美国"
        }
      }
    ]
  }
}
```
2、搜索有名叫张三的员工的研发中心
```
GET /company/rd_center/_search
{
  "query": {
    "has_child": {
      "type":       "employee",
      "query": {
        "match": {
          "name": "张三"
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
        "_index": "company",
        "_type": "rd_center",
        "_id": "1",
        "_score": 1,
        "_source": {
          "name": "北京研发总部",
          "city": "北京",
          "country": "中国"
        }
      }
    ]
  }
}
```
3、搜索有至少2个以上员工的研发中心
```
GET /company/rd_center/_search
{
  "query": {
    "has_child": {
      "type":         "employee",
      "min_children": 2, 
      "query": {
        "match_all": {}
      }
    }
  }
}

{
  "took": 5,
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
        "_index": "company",
        "_type": "rd_center",
        "_id": "1",
        "_score": 1,
        "_source": {
          "name": "北京研发总部",
          "city": "北京",
          "country": "中国"
        }
      }
    ]
  }
}
```
4、搜索在中国的研发中心的员工
```
GET /company/employee/_search 
{
  "query": {
    "has_parent": {
      "parent_type": "rd_center",
      "query": {
        "term": {
          "country.keyword": "中国"
        }
      }
    }
  }
}

{
  "took": 5,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 1,
    "hits": [
      {
        "_index": "company",
        "_type": "employee",
        "_id": "3",
        "_score": 1,
        "_routing": "2",
        "_parent": "2",
        "_source": {
          "name": "王二",
          "birthday": "1979-04-01",
          "hobby": "爬山"
        }
      },
      {
        "_index": "company",
        "_type": "employee",
        "_id": "1",
        "_score": 1,
        "_routing": "1",
        "_parent": "1",
        "_source": {
          "name": "张三",
          "birthday": "1970-10-24",
          "hobby": "爬山"
        }
      },
      {
        "_index": "company",
        "_type": "employee",
        "_id": "2",
        "_score": 1,
        "_routing": "1",
        "_parent": "1",
        "_source": {
          "name": "李四",
          "birthday": "1982-05-16",
          "hobby": "游泳"
        }
      }
    ]
  }
}
```

========================

* 71 对每个国家的员工兴趣爱好进行聚合统计
统计每个国家的喜欢每种爱好的员工有多少个
```
GET /company/rd_center/_search 
{
  "size": 0,
  "aggs": {
    "group_by_country": {
      "terms": {
        "field": "country.keyword"
      },
      "aggs": {
        "group_by_child_employee": {
          "children": {
            "type": "employee"
          },
          "aggs": {
            "group_by_hobby": {
              "terms": {
                "field": "hobby.keyword"
              }
            }
          }
        }
      }
    }
  }
}

{
  "took": 15,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 0,
    "hits": []
  },
  "aggregations": {
    "group_by_country": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0,
      "buckets": [
        {
          "key": "中国",
          "doc_count": 2,
          "group_by_child_employee": {
            "doc_count": 3,
            "group_by_hobby": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [
                {
                  "key": "爬山",
                  "doc_count": 2
                },
                {
                  "key": "游泳",
                  "doc_count": 1
                }
              ]
            }
          }
        },
        {
          "key": "美国",
          "doc_count": 1,
          "group_by_child_employee": {
            "doc_count": 1,
            "group_by_hobby": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [
                {
                  "key": "骑马",
                  "doc_count": 1
                }
              ]
            }
          }
        }
      ]
    }
  }
}

```

#### 72 祖孙三层数据关系建模以及搜索实战
父子关系，祖孙三层关系的数据建模，搜索
```
PUT /company
{
  "mappings": {
    "country": {},
    "rd_center": {
      "_parent": {
        "type": "country" 
      }
    },
    "employee": {
      "_parent": {
        "type": "rd_center" 
      }
    }
  }
}

country -> rd_center -> employee，祖孙三层数据模型

POST /company/country/_bulk
{ "index": { "_id": "1" }}
{ "name": "中国" }
{ "index": { "_id": "2" }}
{ "name": "美国" }

POST /company/rd_center/_bulk
{ "index": { "_id": "1", "parent": "1" }}
{ "name": "北京研发总部" }
{ "index": { "_id": "2", "parent": "1" }}
{ "name": "上海研发中心" }
{ "index": { "_id": "3", "parent": "2" }}
{ "name": "硅谷人工智能实验室" }

PUT /company/employee/1?parent=1&routing=1
{
  "name":  "张三",
  "dob":   "1970-10-24",
  "hobby": "爬山"
}
```
routing参数的讲解，必须跟grandparent相同，否则有问题

country，用的是自己的id去路由; rd_center，parent，用的是country的id去路由; employee，如果也是仅仅指定一个parent，那么用的是rd_center的id去路由，这就导致祖孙三层数据不会在一个shard上

孙子辈儿，要手动指定routing，指定为爷爷辈儿的数据的id

搜索有爬山爱好的员工所在的国家
```
GET /company/country/_search
{
  "query": {
    "has_child": {
      "type": "rd_center",
      "query": {
        "has_child": {
          "type": "employee",
          "query": {
            "match": {
              "hobby": "爬山"
            }
          }
        }
      }
    }
  }
}

{
  "took": 10,
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
        "_index": "company",
        "_type": "country",
        "_id": "1",
        "_score": 1,
        "_source": {
          "name": "中国"
        }
      }
    ]
  }
}
```

###　elasticsearch高手进阶

#### 73 基于term vector深入探查数据的情况

1、term vector介绍

获取document中的某个field内的各个term的统计信息

* term information: term frequency in the field, term positions, start and end offsets, term payloads

* term statistics: 设置term_statistics=true; total term frequency, 一个term在所有document中出现的频率; document frequency，有多少document包含这个term

* field statistics: document count，有多少document包含这个field; sum of document frequency，一个field中所有term的df之和; sum of total term frequency，一个field中的所有term的tf之和

GET /twitter/tweet/1/_termvectors
GET /twitter/tweet/1/_termvectors?fields=text

term statistics和field statistics并不精准，不会被考虑有的doc可能被删除了

我告诉大家，其实很少用，用的时候，一般来说，就是你需要对一些数据做探查的时候。比如说，你想要看到某个term，某个词条，大话西游，这个词条，在多少个document中出现了。或者说某个field，film_desc，电影的说明信息，有多少个doc包含了这个说明信息。

2、index-iime term vector实验

term vector，涉及了很多的term和field相关的统计信息，有两种方式可以采集到这个统计信息

（1）index-time，你在mapping里配置一下，然后建立索引的时候，就直接给你生成这些term和field的统计信息了
（2）query-time，你之前没有生成过任何的Term vector信息，然后在查看term vector的时候，直接就可以看到了，会on the fly，现场计算出各种统计信息，然后返回给你

这一讲，不会手敲任何命令，直接copy我做好的命令，因为这一讲的重点，不是掌握什么搜索或者聚合的语法，而是说，掌握，如何采集term vector信息，然后如何看懂term vector信息，你能掌握利用term vector进行数据探查

PUT /my_index
{
  "mappings": {
    "my_type": {
      "properties": {
        "text": {
            "type": "text",
            "term_vector": "with_positions_offsets_payloads",
            "store" : true,
            "analyzer" : "fulltext_analyzer"
         },
         "fullname": {
            "type": "text",
            "analyzer" : "fulltext_analyzer"
        }
      }
    }
  },
  "settings" : {
    "index" : {
      "number_of_shards" : 1,
      "number_of_replicas" : 0
    },
    "analysis": {
      "analyzer": {
        "fulltext_analyzer": {
          "type": "custom",
          "tokenizer": "whitespace",
          "filter": [
            "lowercase",
            "type_as_payload"
          ]
        }
      }
    }
  }
}


PUT /my_index/my_type/1
{
  "fullname" : "Leo Li",
  "text" : "hello test test test "
}

PUT /my_index/my_type/2
{
  "fullname" : "Leo Li",
  "text" : "other hello test ..."
}

GET /my_index/my_type/1/_termvectors
{
  "fields" : ["text"],
  "offsets" : true,
  "payloads" : true,
  "positions" : true,
  "term_statistics" : true,
  "field_statistics" : true
}

{
  "_index": "my_index",
  "_type": "my_type",
  "_id": "1",
  "_version": 1,
  "found": true,
  "took": 10,
  "term_vectors": {
    "text": {
      "field_statistics": {
        "sum_doc_freq": 6,
        "doc_count": 2,
        "sum_ttf": 8
      },
      "terms": {
        "hello": {
          "doc_freq": 2,
          "ttf": 2,
          "term_freq": 1,
          "tokens": [
            {
              "position": 0,
              "start_offset": 0,
              "end_offset": 5,
              "payload": "d29yZA=="
            }
          ]
        },
        "test": {
          "doc_freq": 2,
          "ttf": 4,
          "term_freq": 3,
          "tokens": [
            {
              "position": 1,
              "start_offset": 6,
              "end_offset": 10,
              "payload": "d29yZA=="
            },
            {
              "position": 2,
              "start_offset": 11,
              "end_offset": 15,
              "payload": "d29yZA=="
            },
            {
              "position": 3,
              "start_offset": 16,
              "end_offset": 20,
              "payload": "d29yZA=="
            }
          ]
        }
      }
    }
  }
}

3、query-time term vector实验

GET /my_index/my_type/1/_termvectors
{
  "fields" : ["fullname"],
  "offsets" : true,
  "positions" : true,
  "term_statistics" : true,
  "field_statistics" : true
}

一般来说，如果条件允许，你就用query time的term vector就可以了，你要探查什么数据，现场去探查一下就好了

4、手动指定doc的term vector

GET /my_index/my_type/_termvectors
{
  "doc" : {
    "fullname" : "Leo Li",
    "text" : "hello test test test"
  },
  "fields" : ["text"],
  "offsets" : true,
  "payloads" : true,
  "positions" : true,
  "term_statistics" : true,
  "field_statistics" : true
}

手动指定一个doc，实际上不是要指定doc，而是要指定你想要安插的词条，hello test，那么就可以放在一个field中

将这些term分词，然后对每个term，都去计算它在现有的所有doc中的一些统计信息

这个挺有用的，可以让你手动指定要探查的term的数据情况，你就可以指定探查“大话西游”这个词条的统计信息

5、手动指定analyzer来生成term vector

GET /my_index/my_type/_termvectors
{
  "doc" : {
    "fullname" : "Leo Li",
    "text" : "hello test test test"
  },
  "fields" : ["text"],
  "offsets" : true,
  "payloads" : true,
  "positions" : true,
  "term_statistics" : true,
  "field_statistics" : true,
  "per_field_analyzer" : {
    "text": "standard"
  }
}

6、terms filter

GET /my_index/my_type/_termvectors
{
  "doc" : {
    "fullname" : "Leo Li",
    "text" : "hello test test test"
  },
  "fields" : ["text"],
  "offsets" : true,
  "payloads" : true,
  "positions" : true,
  "term_statistics" : true,
  "field_statistics" : true,
  "filter" : {
      "max_num_terms" : 3,
      "min_term_freq" : 1,
      "min_doc_freq" : 1
    }
}

这个就是说，根据term统计信息，过滤出你想要看到的term vector统计结果
也挺有用的，比如你探查数据把，可以过滤掉一些出现频率过低的term，就不考虑了

7、multi term vector

GET _mtermvectors
{
   "docs": [
      {
         "_index": "my_index",
         "_type": "my_type",
         "_id": "2",
         "term_statistics": true
      },
      {
         "_index": "my_index",
         "_type": "my_type",
         "_id": "1",
         "fields": [
            "text"
         ]
      }
   ]
}

GET /my_index/_mtermvectors
{
   "docs": [
      {
         "_type": "test",
         "_id": "2",
         "fields": [
            "text"
         ],
         "term_statistics": true
      },
      {
         "_type": "test",
         "_id": "1"
      }
   ]
}

GET /my_index/my_type/_mtermvectors
{
   "docs": [
      {
         "_id": "2",
         "fields": [
            "text"
         ],
         "term_statistics": true
      },
      {
         "_id": "1"
      }
   ]
}

GET /_mtermvectors
{
   "docs": [
      {
         "_index": "my_index",
         "_type": "my_type",
         "doc" : {
            "fullname" : "Leo Li",
            "text" : "hello test test test"
         }
      },
      {
         "_index": "my_index",
         "_type": "my_type",
         "doc" : {
           "fullname" : "Leo Li",
           "text" : "other hello test ..."
         }
      }
   ]
}


#### 74 深入剖析搜索结果的highlight高亮显示

1、一个最基本的高亮例子
```
PUT /blog_website
{
  "mappings": {
    "blogs": {
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "ik_max_word"
        },
        "content": {
          "type": "text",
          "analyzer": "ik_max_word"
        }
      }
    }
  }
}

PUT /blog_website/blogs/1
{
  "title": "我的第一篇博客",
  "content": "大家好，这是我写的第一篇博客，特别喜欢这个博客网站！！！"
}

GET /blog_website/blogs/_search 
{
  "query": {
    "match": {
      "title": "博客"
    }
  },
  "highlight": {
    "fields": {
      "title": {}
    }
  }
}

{
  "took": 103,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "failed": 0
  },
  "hits": {
    "total": 1,
    "max_score": 0.28582606,
    "hits": [
      {
        "_index": "blog_website",
        "_type": "blogs",
        "_id": "1",
        "_score": 0.28582606,
        "_source": {
          "title": "我的第一篇博客",
          "content": "大家好，这是我写的第一篇博客，特别喜欢这个博客网站！！！"
        },
        "highlight": {
          "title": [
            "我的第一篇<em>博客</em>"
          ]
        }
      }
    ]
  }
}
```
<em></em>表现，会变成红色，所以说你的指定的field中，如果包含了那个搜索词的话，就会在那个field的文本中，对搜索词进行红色的高亮显示
```
GET /blog_website/blogs/_search 
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "title": "博客"
          }
        },
        {
          "match": {
            "content": "博客"
          }
        }
      ]
    }
  },
  "highlight": {
    "fields": {
      "title": {},
      "content": {}
    }
  }
}
```
highlight中的field，必须跟query中的field一一对齐的

2、三种highlight介绍

plain highlight，lucene highlight，默认

posting highlight，index_options=offsets

（1）性能比plain highlight要高，因为不需要重新对高亮文本进行分词
（2）对磁盘的消耗更少
（3）将文本切割为句子，并且对句子进行高亮，效果更好

```
PUT /blog_website
{
  "mappings": {
    "blogs": {
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "ik_max_word"
        },
        "content": {
          "type": "text",
          "analyzer": "ik_max_word",
          "index_options": "offsets"
        }
      }
    }
  }
}

PUT /blog_website/blogs/1
{
  "title": "我的第一篇博客",
  "content": "大家好，这是我写的第一篇博客，特别喜欢这个博客网站！！！"
}

GET /blog_website/blogs/_search 
{
  "query": {
    "match": {
      "content": "博客"
    }
  },
  "highlight": {
    "fields": {
      "content": {}
    }
  }
}

fast vector highlight

index-time term vector设置在mapping中，就会用fast verctor highlight
```
（1）对大field而言（大于1mb），性能更高

```
PUT /blog_website
{
  "mappings": {
    "blogs": {
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "ik_max_word"
        },
        "content": {
          "type": "text",
          "analyzer": "ik_max_word",
          "term_vector" : "with_positions_offsets"
        }
      }
    }
  }
}
```

强制使用某种highlighter，比如对于开启了term vector的field而言，可以强制使用plain highlight
```
GET /blog_website/blogs/_search 
{
  "query": {
    "match": {
      "content": "博客"
    }
  },
  "highlight": {
    "fields": {
      "content": {
        "type": "plain"
      }
    }
  }
}
```
总结一下，其实可以根据你的实际情况去考虑，一般情况下，用plain highlight也就足够了，不需要做其他额外的设置
如果对高亮的性能要求很高，可以尝试启用posting highlight
如果field的值特别大，超过了1M，那么可以用fast vector highlight

3、设置高亮html标签，默认是<em>标签
```
GET /blog_website/blogs/_search 
{
  "query": {
    "match": {
      "content": "博客"
    }
  },
  "highlight": {
    "pre_tags": ["<tag1>"],
    "post_tags": ["</tag2>"], 
    "fields": {
      "content": {
        "type": "plain"
      }
    }
  }
}
```
4、高亮片段fragment的设置
```
GET /_search
{
    "query" : {
        "match": { "user": "kimchy" }
    },
    "highlight" : {
        "fields" : {
            "content" : {"fragment_size" : 150, "number_of_fragments" : 3, "no_match_size": 150 }
        }
    }
}
```
* fragment_size: 你一个Field的值，比如有长度是1万，但是你不可能在页面上显示这么长啊。。。设置要显示出来的fragment文本判断的长度，默认是100
* number_of_fragments：你可能你的高亮的fragment文本片段有多个片段，你可以指定就显示几个片段

#### 75 使用search template将搜索模板化
可以将我们的一些搜索进行模板化，每次执行这个搜索，就直接调用模板，给传入一些参数就可以了

越高级的功能，越少使用，可能只有在你真的遇到特别合适的场景的时候，才会去使用某个高级功能。

但是，这些高级功能你是否掌握，其实就是普通的es开发人员，和es高手之间的一个区别。

高手，一般来说，会把一个技术掌握的特别好，特别全面，特别深入，也许他平时用不到这个技术，但是当真的遇到一定的场景的时候，高手可以基于自己的深厚的技术储备，立即反应过来，找到一个合适的解决方案。

如果是一个普通的技术人员，一般只会学习跟自己当前工作相关的一些知识和技术，只要求自己掌握的技术可以解决工作目前遇到的问题就可以了，就满足了，就会止步不前了，然后就不会更加深入的去学习一个技术。

但是，当你的项目真正遇到问题的时候，遇到了一些难题，你之前的那一点技术储备已经没法去应付这些更加困难的问题了，此时，普通的技术人员就会扎耳挠腮，没有任何办法。

高手，对技术是很有追求，能够精通很多自己遇到过的技术，但是也许自己学的很多东西，自己都没用过，但是不要紧，这是你的一种技术储备。


1、search template入门

GET /blog_website/blogs/_search/template
{
  "inline" : {
    "query": { 
      "match" : { 
        "{{field}}" : "{{value}}" 
      } 
    }
  },
  "params" : {
      "field" : "title",
      "value" : "博客"
  }
}

GET /blog_website/blogs/_search
{
  "query": { 
    "match" : { 
      "title" : "博客" 
    } 
  }
}

search template："{{field}}" : "{{value}}" 

2、toJson

GET /blog_website/blogs/_search/template
{
  "inline": "{\"query\": {\"match\": {{#toJson}}matchCondition{{/toJson}}}}",
  "params": {
    "matchCondition": {
      "title": "博客"
    }
  }
}

GET /blog_website/blogs/_search
{
  "query": { 
    "match" : { 
      "title" : "博客" 
    } 
  }
}

3、join

GET /blog_website/blogs/_search/template
{
  "inline": {
    "query": {
      "match": {
        "title": "{{#join delimiter=' '}}titles{{/join delimiter=' '}}"
      }
    }
  },
  "params": {
    "titles": ["博客", "网站"]
  }
}

博客,网站

GET /blog_website/blogs/_search
{
  "query": { 
    "match" : { 
      "title" : "博客 网站" 
    } 
  }
}

4、default value

POST /blog_website/blogs/1/_update
{
  "doc": {
    "views": 5
  }
}

GET /blog_website/blogs/_search/template
{
  "inline": {
    "query": {
      "range": {
        "views": {
          "gte": "{{start}}",
          "lte": "{{end}}{{^end}}20{{/end}}"
        }
      }
    }
  },
  "params": {
    "start": 1,
    "end": 10
  }
}

GET /blog_website/blogs/_search
{
  "query": {
    "range": {
      "views": {
        "gte": 1,
        "lte": 10
      }
    }
  }
}

GET /blog_website/blogs/_search/template
{
  "inline": {
    "query": {
      "range": {
        "views": {
          "gte": "{{start}}",
          "lte": "{{end}}{{^end}}20{{/end}}"
        }
      }
    }
  },
  "params": {
    "start": 1
  }
}

GET /blog_website/blogs/_search
{
  "query": {
    "range": {
      "views": {
        "gte": 1,
        "lte": 20
      }
    }
  }
}


5、conditional

es的config/scripts目录下，预先保存这个复杂的模板，后缀名是.mustache，文件名是conditonal

{
  "query": {
    "bool": {
      "must": {
        "match": {
          "line": "{{text}}" 
        }
      },
      "filter": {
        {{#line_no}} 
          "range": {
            "line_no": {
              {{#start}} 
                "gte": "{{start}}" 
                {{#end}},{{/end}} 
              {{/start}} 
              {{#end}} 
                "lte": "{{end}}" 
              {{/end}} 
            }
          }
        {{/line_no}} 
      }
    }
  }
}

GET /my_index/my_type/_search 

{
  "took": 4,
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
        "_index": "my_index",
        "_type": "my_type",
        "_id": "1",
        "_score": 1,
        "_source": {
          "line": "我的博客",
          "line_no": 5
        }
      }
    ]
  }
}

GET /my_index/my_type/_search/template
{
  "file": "conditional",
  "params": {
    "text": "博客",
    "line_no": true,
    "start": 1,
    "end": 10
  }
}

6、保存search template

config/scripts，.mustache 

提供一个思路

比如说，一般在大型的团队中，可能不同的人，都会想要执行一些类似的搜索操作
这个时候，有一些负责底层运维的一些同学，就可以基于search template，封装一些模板出来，然后是放在各个es进程的scripts目录下的
其他的团队，其实就不用各个团队自己反复手写复杂的通用的查询语句了，直接调用某个搜索模板，传入一些参数就好了


#### 76 - 81 沒学

### 82_熟练掌握ES Java API_

#### client集群自动探查以及汽车零售店案例背景
1、client集群自动探查

默认情况下，是根据我们手动指定的所有节点，依次轮询这些节点，来发送各种请求的，如下面的代码，我们可以手动为client指定多个节点

TransportClient client = new PreBuiltTransportClient(settings)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost1"), 9300))
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost2"), 9300))
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost3"), 9300));

但是问题是，如果我们有成百上千个节点呢？难道也要这样手动添加吗？

es client提供了一种集群节点自动探查的功能，打开这个自动探查机制以后，es client会根据我们手动指定的几个节点连接过去，然后通过集群状态自动获取当前集群中的所有data node，然后用这份完整的列表更新自己内部要发送请求的node list。
默认每隔5秒钟，就会更新一次node list。

但是注意，es cilent是不会将Master node纳入node list的，因为要避免给master node发送搜索等请求。

这样的话，我们其实直接就指定几个master node，或者1个node就好了，client会自动去探查集群的所有节点，而且每隔5秒还会自动刷新。非常棒。

Settings settings = Settings.builder()
        .put("client.transport.sniff", true).build();
TransportClient client = new PreBuiltTransportClient(settings);

使用上述的settings配置，将client.transport.sniff设置为true即可打开集群节点自动探查功能

在实际的生产环境中，都是这么玩儿的。。。


2、 汽车零售案例背景

简单来说，会涉及到三个数据，汽车信息，汽车销售记录，汽车4S店信息

做一个汽车零售数据的mapping，我们要做的第一份数据，其实汽车信息

PUT /car_shop
{
    "mappings": {
        "cars": {
            "properties": {
                "brand": {
                    "type": "text",
                    "analyzer": "ik_max_word",
                    "fields": {
                        "raw": {
                            "type": "keyword"
                        }
                    }
                },
                "name": {
                    "type": "text",
                    "analyzer": "ik_max_word",
                    "fields": {
                        "raw": {
                            "type": "keyword"
                        }
                    }
                }
            }
        }
    }
}

首先的话呢，第一次调整宝马320这个汽车的售价，
我们希望将售价设置为32万，用一个upsert语法，
如果这个汽车的信息之前不存在，那么就insert，
如果存在，那么就update

IndexRequest indexRequest = new IndexRequest("car_shop", "cars", "1")
        .source(jsonBuilder()
            .startObject()
                .field("brand", "宝马")
                .field("name", "宝马320")
                .field("price", 320000)
                .field("produce_date", "2017-01-01")
            .endObject());

UpdateRequest updateRequest = new UpdateRequest("car_shop", "cars", "1")
        .doc(jsonBuilder()
            .startObject()
                .field("price", 320000)
            .endObject())
        .upsert(indexRequest);       
               
client.update(updateRequest).get();

IndexRequest indexRequest = new IndexRequest("car_shop", "cars", "1")
        .source(jsonBuilder()
            .startObject()
                .field("brand", "宝马")
                .field("name", "宝马320")
                .field("price", 310000)
                .field("produce_date", "2017-01-01")
            .endObject());
UpdateRequest updateRequest = new UpdateRequest("car_shop", "cars", "1")
        .doc(jsonBuilder()
            .startObject()
                .field("price", 310000)
            .endObject())
        .upsert(indexRequest);              
client.update(updateRequest).get();
