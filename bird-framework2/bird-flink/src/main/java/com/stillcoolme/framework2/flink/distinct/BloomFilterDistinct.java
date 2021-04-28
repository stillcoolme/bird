package com.stillcoolme.framework2.flink.distinct;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.shaded.curator.org.apache.curator.shaded.com.google.common.hash.BloomFilter;
import org.apache.flink.shaded.curator.org.apache.curator.shaded.com.google.common.hash.Funnels;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * BloomFilter去重统计
 * 每当来一条数据时，就检查 state 中的布隆过滤器中是否存在当前的 SKU，如果没有则初始化，如果有则数量加 1。
 */
public class BloomFilterDistinct extends KeyedProcessFunction<Long, String, Long> {

    private transient ValueState<BloomFilter> bloomState;
    private transient ValueState<Long> countState;

    @Override
    public void processElement(String value, Context ctx, Collector<Long> out) throws Exception {
        BloomFilter bloomFilter = bloomState.value();
        Long skuCount = countState.value();
        if (bloomFilter == null) {
            BloomFilter.create(Funnels.unencodedCharsFunnel(), 10000000);
        }
        if (skuCount == null) {
            skuCount = 0L;
        }
        if (!bloomFilter.mightContain(value)) {
            bloomFilter.put(value);
            skuCount += 1;
        }
        bloomState.update(bloomFilter);
        countState.update(skuCount);
        out.collect(countState.value());

    }
}
