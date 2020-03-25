package com.stillcoolme.framework.spark.mianshi;

import com.stillcoolme.framework.spark.SparkBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;

import static com.stillcoolme.framework.spark.SparkBuilder.BASE_PATH;

/**
 * @author: stillcoolme
 * @date: 2020/3/24 8:54
 * Function:
 *  面试题：找出两个文件中共同存在的词，文件的格式都是一行一个单词。
 */
public class FindSameWordJob {

    public static void main(String[] args) {

        SparkSession sparkSession = SparkBuilder.getSparkSession(FindSameWordJob.class);

        Dataset<String> stringDataset1 = sparkSession.read().textFile(BASE_PATH + "/file1.txt");
        Dataset<String> stringDataset2 = sparkSession.read().textFile(BASE_PATH + "/file2.txt");
        // 求并集
        Dataset<String> intersectDataset = stringDataset1.intersect(stringDataset2);
        intersectDataset.show(false);

    }


}
