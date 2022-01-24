package com.stillcoolme.framework.flink.asyncio.flink_1_9;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-09 11:37:00
 * @Description
 *  flink1.9 的优化，实现 LookupableTableSource，来决定是否使用异步方式访问外部数据源，需要引入 Blink 的 Planner
 */
public class App {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //env.setParallelism(1);
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        final ParameterTool params = ParameterTool.fromArgs(args);
        String fileName = params.get("f");
        DataStream<String> source = env.readTextFile("hdfs://172.16.44.28:8020" + fileName, "UTF-8");

        TypeInformation[] types = new TypeInformation[]{Types.STRING, Types.STRING, Types.LONG};
        String[] fields = new String[]{"id", "user_click", "time"};
        RowTypeInfo typeInformation = new RowTypeInfo(types, fields);

        DataStream<Row> stream = source.map(new MapFunction<String, Row>() {
            private static final long serialVersionUID = 2349572543469673349L;

            @Override
            public Row map(String s) {
                String[] split = s.split(",");
                Row row = new Row(split.length);
                for (int i = 0; i < split.length; i++) {

                    Object value = split[i];
                    if (types[i].equals(Types.STRING)) {
                        value = split[i];
                    }
                    if (types[i].equals(Types.LONG)) {
                        value = Long.valueOf(split[i]);
                    }
                    row.setField(i, value);
                }
                return row;
            }
        }).returns(typeInformation);

        // 主表 user_click_name表
        tableEnv.registerDataStream("user_click_name", stream, String.join(",", typeInformation.getFieldNames()) + ",proctime.proctime");

        // 维度表 info表
        RedisAsyncLookupTableSource tableSource = RedisAsyncLookupTableSource.Builder.newBuilder()
                .withFieldNames(new String[]{"id", "name"})
                .withFieldTypes(new TypeInformation[]{Types.STRING, Types.STRING})
                .build();
        tableEnv.registerTableSource("info", tableSource);

        String SQL = "select t1.id,t1.user_click,t2.name" +
                " from user_click_name as t1" +
                " join info FOR SYSTEM_TIME AS OF t1.proctime as t2" +
                " on t1.id = t2.id";

        Table table = tableEnv.sqlQuery(SQL);

        DataStream<Row> result = tableEnv.toAppendStream(table, Row.class);

        DataStream<String> printStream = result.map(new MapFunction<Row, String>() {
            @Override
            public String map(Row value) throws Exception {
                return value.toString();
            }
        });

//        Properties properties = new Properties();
//        properties.setProperty("bootstrap.servers", "127.0.0.1:9094");
//        FlinkKafkaProducer011<String> kafkaProducer = new FlinkKafkaProducer011<String>(
//                "user_click_name",
//                new SimpleStringSchema(),
//                properties);
//        printStream.addSink(kafkaProducer);


        tableEnv.execute(Thread.currentThread().getStackTrace()[1].getClassName());
    }

}
