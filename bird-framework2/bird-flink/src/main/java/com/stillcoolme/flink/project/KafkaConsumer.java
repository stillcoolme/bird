package com.stillcoolme.framework2.flink.project;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Properties;

public class KafkaConsumer {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.enableCheckpointing(5000);
        // 指定 Flink 系统使用的时间类型为 EventTime
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // 设置消费组
        properties.setProperty("group.id", "group_test");
        // 打开动态分区发现功能，消费者每隔 10ms 会动态获取 Topic 的元数据，对于新增的 Partition 会自动从最早的位点开始消费数据。
        properties.setProperty(FlinkKafkaConsumerBase.KEY_PARTITION_DISCOVERY_INTERVAL_MILLIS, "10");

        FlinkKafkaConsumer<ConsumerRecord<String, String>> consumer = new FlinkKafkaConsumer<>("test",
                new CustomDeSerializationSchema(),
                properties);

        //设置从最早的offset消费
        consumer.setStartFromEarliest();

        // 设置 watermark
        // watermark = max(进入窗口的最大eventtime, currtime) - 当前时间戳
        // 当 watermark 大于 可忍受的延迟时间 就触发计算
        consumer.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<ConsumerRecord<String, String>>() {
            private Long currentTimeStamp = 0L;
            //设置允许乱序时间为5秒
            private Long maxOutOfOrderness = 5000L;

            @Override
            public Watermark getCurrentWatermark() {
                return new Watermark(currentTimeStamp - maxOutOfOrderness);
            }

            @Override
            public long extractTimestamp(ConsumerRecord<String, String> element, long previousElementTimestamp) {

                currentTimeStamp = System.currentTimeMillis();
                long eventTime = element.timestamp();
                currentTimeStamp = Math.max(eventTime, currentTimeStamp);
                System.out.println("currentTime: " + currentTimeStamp + ", eventTime: " + eventTime +
                        ", " + "waterMark: " + (currentTimeStamp - maxOutOfOrderness));
                return element.timestamp();
            }
        });


        env.addSource(consumer).flatMap(new FlatMapFunction<ConsumerRecord<String, String>, String>() {
            @Override
            public void flatMap(ConsumerRecord<String, String> value, Collector<String> out) throws Exception {
                System.out.println(value.key());
            }

        });
        env.execute("start comsumer...");
    }
}
