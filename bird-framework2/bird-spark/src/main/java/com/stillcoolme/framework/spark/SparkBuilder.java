package com.stillcoolme.framework.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

/**
 * @author: stillcoolme
 * @date: 2020/3/24 9:06
 * Function:
 */
public class SparkBuilder {

    public static String BASE_PATH = "bird-framework2/bird-spark/src/main/resources";

    static SparkSession sparkSession;
    static SQLContext sqlContext;

    public static SparkSession getSparkSession() {
        return getSparkSession(SparkBuilder.class, "local");
    }

    public static SparkSession getSparkSession(Class clazz) {
        return getSparkSession(clazz, "local");
    }

    public static SparkSession getSparkSession(Class clazz, String runMode) {
        SparkConf conf = new SparkConf()
                .setMaster(runMode)
                .setAppName(clazz.getName());

        sparkSession =  SparkSession
                .builder()
                .config(conf)
                .getOrCreate();
        return sparkSession;
    }

    public static SQLContext getSqlContext(Class clazz, String runMode) {
        if (sparkSession == null) {
            throw new RuntimeException("请先初始化 sparkSession");
        }
        sqlContext = sparkSession.sqlContext();
        return sqlContext;
    }

}
