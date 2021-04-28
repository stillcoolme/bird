package com.stillcoolme.framework2.flink.project;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;

import java.util.Properties;

public class KafkaProducer {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.enableCheckpointing(5000);

        DataStreamSource<String> stream = env.addSource(new KafkaProducerDataSource()).setParallelism(1);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // 2.0 配置 KafkaProducer，这个类继承了 TwoPhaseCommitSinkFunction，是 Flink 和 Kafka 结合实现精确一次处理语义的关键。
        FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<String>(
                "test",
                (KafkaSerializationSchema<String>) new SimpleStringSchema(),  // 消息序列化
                properties,
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE);
        //写入 Kafka 时附加记录的事件时间戳
        producer.setWriteTimestampToKafka(true);

        stream.addSink(producer);

        env.execute();
    }
}
