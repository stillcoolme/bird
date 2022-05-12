package com.stillcoolme.framework2.flink.distinct;


import org.apache.flink.api.common.functions.AggregateFunction;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

public class BitMapDistinct implements AggregateFunction<Long, Roaring64NavigableMap, Long> {

    @Override
    public Roaring64NavigableMap createAccumulator() {
        return new Roaring64NavigableMap();
    }

    @Override
    public Roaring64NavigableMap add(Long value, Roaring64NavigableMap accumulator) {
        accumulator.add(value);
        return accumulator;
    }

    @Override
    public Long getResult(Roaring64NavigableMap accumulator) {
        return accumulator.getLongCardinality();
    }

    @Override
    // merge函数在使用session窗口的时候用到，因为需要合并窗口，这里不会被调用。
    public Roaring64NavigableMap merge(Roaring64NavigableMap a, Roaring64NavigableMap b) {
        return null;
    }
}
