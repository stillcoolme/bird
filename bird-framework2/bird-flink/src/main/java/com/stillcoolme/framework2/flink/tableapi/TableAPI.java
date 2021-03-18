package com.stillcoolme.framework2.flink.tableapi;

import akka.japi.tuple.Tuple4;
import com.stillcoolme.framework2.flink.Item;
import com.stillcoolme.framework2.flink.MyStreamingSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import scala.Int;

import java.util.ArrayList;
import java.util.List;

public class TableAPI {


    public static void main(String[] args) throws Exception {
        test_item_sql();

    }

    /**
     * 把实时的商品数据流进行分流，分成 even 和 odd 两个流进行 JOIN，
     * 条件是名称相同，最后，把两个流的 JOIN 结果输出。
     */
    public static void test_item_sql() throws Exception {
        EnvironmentSettings bsSettings = EnvironmentSettings.newInstance()
                .useBlinkPlanner().inStreamingMode().build();
        StreamExecutionEnvironment bsEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment bsTableEnv = StreamTableEnvironment.create(bsEnv, bsSettings);

        SingleOutputStreamOperator<Item> source = bsEnv.addSource(new MyStreamingSource()).map(new MapFunction<Item, Item>() {
            @Override
            public Item map(Item item) throws Exception {
                return item;
            }
        });

        DataStream<Item> evenSelect = source.split(new OutputSelector<Item>() {
            @Override
            public Iterable<String> select(Item value) {
                List<String> output = new ArrayList<>();
                if (value.getId() % 2 == 0) {
                    output.add("even");
                } else {
                    output.add("odd");
                }
                return output;
            }
        }).select("even");

        DataStream<Item> oddSelect = source.split(new OutputSelector<Item>() {
            @Override
            public Iterable<String> select(Item value) {
                List<String> output = new ArrayList<>();
                if (value.getId() % 2 == 0) {
                    output.add("even");
                } else {
                    output.add("odd");
                }
                return output;
            }
        }).select("odd");

        bsTableEnv.createTemporaryView("evenTable", evenSelect, "name, id");
        bsTableEnv.createTemporaryView("oddTable", oddSelect, "name, id");

        Table queryTable = bsTableEnv.sqlQuery("select a.id, a.name, b.id, b.name from evenTable as a join  oddTable as b on a.name = b.name");
        queryTable.printSchema();

        bsTableEnv.toRetractStream(queryTable, TypeInformation.of(new TypeHint<Tuple4<Int, String, Int, String>>() {
        })).print();

        bsEnv.execute("streaming sql job");

    }
}
