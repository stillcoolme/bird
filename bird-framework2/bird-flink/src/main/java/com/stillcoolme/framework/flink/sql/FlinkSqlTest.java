package com.stillcoolme.framework.flink.sql;

import com.stillcoolme.framework.flink.entity.Item;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.util.ArrayList;
import java.util.Random;

import static org.apache.flink.table.api.Expressions.$;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/2/8 9:39
 */
public class FlinkSqlTest {

    public static void main(String[] args) throws Exception {

        EnvironmentSettings bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamExecutionEnvironment bsEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment bsTableEnv = StreamTableEnvironment.create(bsEnv, bsSettings);

        SingleOutputStreamOperator<Item> source = bsEnv.addSource(new MyStreamingSource())
                .map(new MapFunction<Item, Item>() {
                    @Override
                    public Item map(Item item) throws Exception {
                        return item;
                    }
                });

        bsTableEnv.createTemporaryView("product",
                source,
                $("name"),
                $("id")
        );

        Table queryTable = bsTableEnv.sqlQuery("select name, id from product limit 5");
        queryTable.printSchema();

        bsTableEnv.toRetractStream(queryTable, TypeInformation.of(new TypeHint<Tuple2<String,Integer>>(){})).print();

        bsEnv.execute("streaming sql job");


    }
}


/**
 * flinksql也是  Source、Transformation、Sink 构成， 这里先实现一个自定义的实时数据源，
 */
class MyStreamingSource implements SourceFunction<Item> {
    private boolean isRunning = true;

    /**
     * 重写run方法产生一个源源不断的数据发送源
     * @param ctx
     * @throws Exception
     */
    @Override
    public void run(SourceContext<Item> ctx) throws Exception {
        while(isRunning){
            Item item = (Item) generateItem();
            ctx.collect(item);
            //每秒产生一条数据
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    /**
     * 随机产生商品数据
     * @return
     */
    Item generateItem() {
        int i = new Random().nextInt(100);
        ArrayList<String> list = new ArrayList();
        list.add("HAT");
        list.add("TIE");
        list.add("SHOE");
        Item item = new Item();
        item.setName(list.get(new Random().nextInt(3)));
        item.setId(i);
        return item;
    }

}