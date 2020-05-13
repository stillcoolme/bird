package com.stillcoolme.framework.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession


class BaseSpark {
  val spark = SparkSession
    .builder()
    .appName("Spark SQL basic example")
    .config("spark.some.config.option", "some-value")
    .master("local")
    .getOrCreate()
  spark.sparkContext.setLogLevel("WARN")
  val basePath = "bird-framework2/bird-spark/src/main/resources"
}

object SparkExample extends BaseSpark {

  def main(args: Array[String]): Unit = {

      distinctBy()

  }

  /**
   * spark选择去重功能；
   *  原需求：获取同一订单的多行数据最后的订单状态 ->  获取同一车辆的多行数据最后经过卡口的记录
   *
   */
  private def distinctBy(): Unit = {
    val carDF = spark.read.format("csv")
      .option("sep", ";")
      .option("inferSchema", "true")
      .option("header", "true")
      .csv(basePath + "/car.csv")

    // 1. df 实现
    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.expressions.Window
    // This import is needed to use the $-notation
    import spark.implicits._

    val lastPassCar = carDF.withColumn("num",
      row_number().over(
        Window
          .partitionBy($"carNum")
          .orderBy($"capTime" desc)
      )
    ).where($"num" === 1).drop($"num")
    lastPassCar.explain()
    lastPassCar.show()

    // 2. rdd 实现
    // 得到schemaRDD就可以通过字段名来拿数据了
    val carRDD: RDD[CarRecord] =
      carDF.rdd.map(x => CarRecord(x.getInt(0), x.getString(1), x.getInt(2), x.getString(3)))
    val res = carRDD.groupBy(_.carNum).map{
      x => x._2.maxBy { _.capTime }      // x._2 是 iter，然后对 iter 查最大的。
    }
    printf(res.toDebugString)
    res.collect.foreach(x => println(x))

  }

}


case class CarRecord(id: Int, carNum: String, orgId: Int, capTime: String)