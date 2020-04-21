import org.apache.spark.Partitioner

class  MySparkPartition(numParts: Int) extends Partitioner {

  override def numPartitions: Int = numParts

  /**
   * 可以自定义分区算法
   *
   * @param key
   * @return
   */
  override def getPartition(key: Any): Int = {
    val domain = new java.net.URL(key.toString).getHost()
    val code = (domain.hashCode % numPartitions)
    if (code < 0) {
      code + numPartitions
    } else {
      code
    }
  }

  override def equals(other: Any): Boolean = other match {
    case mypartition: MySparkPartition =>
      mypartition.numPartitions == numPartitions
    case _ =>
      false
  }

  override def hashCode: Int = numPartitions
}

/**
val conf=new SparkConf()
      .setMaster("local[2]")
      .setAppName("TestMyParttioner")
      .set("spark.app.id","test-partition-id")
    val sc=new SparkContext(conf)

    //读取hdfs文件
    val lines=sc.textFile("hdfs://hadoop2:8020/user/test/word.txt")
    val splitMap=lines.flatMap(line=>line.split("\t")).map(word=>(word,2))//注意：RDD一定要是key-value

    //保存
    splitMap.partitionBy(new MySparkPartition(3)).saveAsTextFile("F:/partrion/test")

    sc.stop()
*/