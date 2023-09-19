package com.stillcoolme.framework.flink.sql;

import org.apache.commons.lang3.SystemUtils;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment;

/**
 * <p>TODO</p>
 *
 * @author zhangjianhua
 * @version V1.0.0
 * @date 2023/9/19 23:10 周二
 */
public class EndToEnd_ExactlyOne_By_CheckPoint {

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 要实现端对端一次性语义，必须要开启checkpoint
        env.enableCheckpointing(10000L);

        env.setStateBackend(new HashMapStateBackend());
        // 设置 checkpoint 参数
        // 同一个时间只能有一个栅栏在运行
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        //设置checkpoint最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000L);
        env.getCheckpointConfig().setCheckpointTimeout(2000L);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC) {
            env.getCheckpointConfig().setCheckpointStorage("file:///D:\\checkpoint");
        } else {
            env.getCheckpointConfig().setCheckpointStorage(args[0]);
        }

        // 接入数据源，读取文件获取数据




    }
}
