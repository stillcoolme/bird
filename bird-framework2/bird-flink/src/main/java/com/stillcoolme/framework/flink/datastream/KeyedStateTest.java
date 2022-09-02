package com.stillcoolme.framework.flink.datastream;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 在 Flink 中，根据数据集是否按照某一个 Key 进行分区，将状态分为 Keyed State 和 Operator State（Non-Keyed State）两种类型。
 * 1. Keyed State 是经过分区后的流状态，每个 Key 都有自己的状态，并且只有指定的 key 才能访问和更新自己对应的状态。
 * 2. 与 Keyed State 不同的是，Operator State 可以用在所有算子上，
 *    每个算子子任务或者说每个算子实例共享一个状态，流入这个算子子任务的数据可以访问和更新这个状态。
 *    每个算子子任务上的数据共享自己的状态。
 *
 * 下面的例子中通过继承 RichFlatMapFunction 来访问 State，
 * 通过 getRuntimeContext().getState(descriptor) 来获取状态的句柄。
 * 而真正的访问和更新状态则在 Map 函数中实现。
 * 这里的输出条件为，每当第一个元素的和达到二，就把第二个元素的和与第一个元素的和相除，最后输出。
 */
public class KeyedStateTest {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.fromElements(
                Tuple2.of(1L, 3L),
                Tuple2.of(1L, 5L),
                Tuple2.of(1L, 7L),
                Tuple2.of(1L, 5L),
                Tuple2.of(1L, 2L),
                Tuple2.of(1L, 2L)
            )
            .keyBy(0)
            .flatMap(new CountWindowAverage())
            .printToErr();

        env.execute("submit job");
    }

    private static class CountWindowAverage extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>> {

        private transient ValueState<Tuple2<Long, Long>> sum;

        @Override
        public void flatMap(Tuple2<Long, Long> input,
                            Collector<Tuple2<Long, Long>> out) throws Exception {

            Tuple2<Long, Long> currentSum;

            if (sum.value() == null) {
                currentSum = Tuple2.of(0L, 0L);
            } else {
                currentSum = sum.value();
            }
            // 更新
            currentSum.f0 += 1;
            // 第二个元素加1
            currentSum.f1 += input.f1;
            // 更新state
            sum.update(currentSum);

            // 如果count的值大于等于2，求值 并 清空state
            if (currentSum.f0 >= 2) {
                out.collect(new Tuple2<>(input.f0, currentSum.f1 / currentSum.f0));
                sum.clear();
            }

        }

        public void open(Configuration config) {
            ValueStateDescriptor<Tuple2<Long, Long>> descriptor =
                    new ValueStateDescriptor<>(
                            "average", // state的名字
                            TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})
                    ); // 设置默认值

            StateTtlConfig ttlConfig = StateTtlConfig
                    .newBuilder(Time.seconds(10))
                    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                    .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                    .build();

            descriptor.enableTimeToLive(ttlConfig);

            sum = getRuntimeContext().getState(descriptor);
        }
    }

}
