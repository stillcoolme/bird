package com.stillcoolme.framework.flink.cep;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.nfa.aftermatch.SkipPastLastStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.List;
import java.util.Map;

/**
 * 在Flink中实现一个CEP可以总结为四步：
 * 一，构建需要的数据流，从一个 Source 作为输入
 * 二，构造正确的模式，
 * 三，将数据流和模式进行结合，经过一个 Pattern 算子转换为 PatternStream
 * 四，在模式流中获取匹配到的数据。经过 select/process 算子转换为 DataStream
 * 其中第一步和第三步一般会是标准操作，核心在于第二步构建模式，
 * 需要利用Flink CEP支持的特性，构造出正确反映业务需求的匹配模式。
 *
 */
public class CepEvent {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Tuple3<Integer, String, String>> eventStream = env.fromElements(
                Tuple3.of(1500, "login", "fail"),
                Tuple3.of(1500, "login", "fail"),
                Tuple3.of(1500, "login", "fail"),
                Tuple3.of(1320, "login", "success"),
                Tuple3.of(1450, "exit", "success"),
                Tuple3.of(982, "login", "fail"));

        // 匹配了某个模式后，如何参与后续模式的策略
        SkipPastLastStrategy skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent();

        // 二，构造正确的模式，
        Pattern<Tuple3<Integer, String, String>, ?> loginFail =
                Pattern.<Tuple3<Integer, String, String>>begin("begin", skipStrategy)
                    .where(new IterativeCondition<Tuple3<Integer, String, String>>() {
                        @Override
                        public boolean filter(Tuple3<Integer, String, String> value, Context<Tuple3<Integer, String, String>> ctx) throws Exception {
                            return value.f2.equalsIgnoreCase("fail");
                        }
                    }).times(3).within(Time.seconds(5)); // 在5秒内匹配3次

        PatternStream<Tuple3<Integer, String, String>> patternStream =
                CEP.pattern(eventStream.keyBy(x -> x.f0), loginFail);

        DataStream<String> alarmStream =
                patternStream.select(new PatternSelectFunction<Tuple3<Integer, String, String>, String>() {
                    @Override
                    public String select(Map<String, List<Tuple3<Integer, String, String>>> map) throws Exception {
                        String msg = String.format("ID %d has login failed 3 times in 5 seconds."
                                , map.values().iterator().next().get(0).f0);
                        return msg;
                    }
                });

        alarmStream.print();
        env.execute("cep event start");
    }
}
