package com.stillcoolme.framework.flink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.net.URL;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-12-24 10:49:00
 * @Description
 *
 */
public class WordCountDemo {
    public static void main(String[] args) throws Exception {
        // socket ip 和 端口
        String hostname = "localhost";
        int port = 9000;

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        URL resource = WordCountDemo.class.getResource("/wordcount.txt");
        DataStream<String> text = env.readTextFile(resource.getPath());

        // nc -l 9000 or nc -l -p 9000  mac机器有问题
//        DataStream<String> text = env.socketTextStream(hostname, port, "\n");

        DataStream<WordWithCount> windowCounts = text
                .flatMap(new FlatMapFunction<String, WordWithCount>() {
                    public void flatMap(String s, Collector<WordWithCount> out) throws Exception {
                        for (String word: s.split(",")) {
                            out.collect(new WordWithCount(word, 1L));
                        }
                    }
                })
                .keyBy("word")
                .timeWindow(Time.seconds(5), Time.seconds(1))
                .reduce(new ReduceFunction<WordWithCount>() {
                    @Override
                    public WordWithCount reduce(WordWithCount a, WordWithCount b) {
                        return new WordWithCount(a.word, a.count + b.count);
                    }
                });
        windowCounts.print().setParallelism(1);
        env.execute("socket Window WordCount");



    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordWithCount {
        public String word;
        public long count;
    }
}
