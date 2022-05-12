package com.stillcoolme.framework2.flink.distinct;

import net.agkn.hll.HLL;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.List;

public class HyperLogLogDistinct implements AggregateFunction<Tuple2<String,Long>,HLL,Long> {

    @Override
    public HLL createAccumulator() {
        return new HLL(14, 5);
    }

    @Override
    public HLL add(Tuple2<String, Long> value, HLL accumulator) {
        // addRaw 方法用于向 HyperLogLog 中插入元素。如果插入的元素非数值型的，则需要 hash 过后才能插入。
        accumulator.addRaw(value.f1);
        return accumulator;
    }

    @Override
    public Long getResult(HLL accumulator) {
        long cardinality = accumulator.cardinality();
        return cardinality;
    }

    @Override
    public HLL merge(HLL a, HLL b) {
        a.union(b);
        return a;
    }
}
