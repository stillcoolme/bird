package com.stillcoolme.framework.spark.mianshi;

import com.stillcoolme.framework.spark.SparkBuilder;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Function1;

import static com.stillcoolme.framework.spark.SparkBuilder.BASE_PATH;

/**
 * @author: stillcoolme
 * @date: 2020/3/24 10:26
 * Function:
 *  面试题： 使用 RDD 统计年龄大于 30 岁的人数
 */
public class RDDCountJob {

    public static void main(String[] args) {
        SparkSession sparkSession = SparkBuilder.getLocalSparkSession();

        Dataset<Row> peopleDataset = sparkSession.read().json(BASE_PATH + "/people.json");
        peopleDataset.show();
        /**
         *
         * +---+-------+
         * |age|   name|
         * +---+-------+
         * | 40|Michael|
         * | 30|   Andy|
         * | 19| Justin|
         * +---+----+-------+
         */
        // 转RDD以后，还是Row，而且 Row 是按照字段名的字母大小排列的，所以下面拿 age 就 rdd.get(0)
        JavaRDD<Row> ageRDD = peopleDataset.toJavaRDD();
        Long count = ageRDD.filter(rdd -> Integer.valueOf(rdd.get(0).toString()) > 30
        ).count();

        System.out.println(count);

    }

}
