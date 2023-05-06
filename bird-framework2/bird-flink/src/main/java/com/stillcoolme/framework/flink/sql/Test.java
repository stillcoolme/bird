//package com.stillcoolme.framework.flink.sql;
//
//import org.apache.flink.api.java.tuple.Tuple3;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.EnvironmentSettings;
//import org.apache.flink.table.api.Table;
//import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
//import org.apache.flink.types.Row;
//
//import java.util.Arrays;
//
///**
// * <p>计算用户等级例子 https://mp.weixin.qq.com/s/5XDkmuEIfHB_WsMHPeinkw
// * </p>
// *
// * @author stillcoolme
// * @version V1.0.0
// * @date 2023/2/8 19:59
// */
//public class Test {
//
//    public static void main(String[] args) throws Exception {
//
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//        env.setParallelism(1);
//
//        EnvironmentSettings settings = EnvironmentSettings
//                .newInstance()
//                .useBlinkPlanner()
//                .inStreamingMode().build();
//
//        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);
//
//        // 模拟输入
//        DataStream<Tuple3<String, Long, Long>> tuple3DataStream =
//                env.fromCollection(Arrays.asList(
//                        Tuple3.of("2", 1L, 1627218000000L),
//                        Tuple3.of("2", 101L, 1627218000000L + 6000L),
//                        Tuple3.of("2", 201L, 1627218000000L + 7000L),
//                        Tuple3.of("2", 301L, 1627218000000L + 7000L)));
//        // 分桶取模 udf
//        tEnv.registerFunction("mod", new Mod_UDF());
//
//        // 中文映射 udf
//        tEnv.registerFunction("status_mapper", new StatusMapper_UDF());
//
//        tEnv.createTemporaryView("source_db.source_table", tuple3DataStream,
//                "status, id, timestamp");
//
//        String sql = "WITH detail_tmp AS (\n"
//                + "  SELECT\n"
//                + "    status,\n"
//                + "    id,\n"
//                + "    `timestamp`\n"
//                + "  FROM\n"
//                + "    (\n"
//                + "      SELECT\n"
//                + "        status,\n"
//                + "        id,\n"
//                + "        `timestamp`,\n"
//                + "        row_number() over(\n"
//                + "          PARTITION by id\n"
//                + "          ORDER BY\n"
//                + "            `timestamp` DESC\n"
//                + "        ) AS rn\n"
//                + "      FROM source_db.source_table"
//                + "    )\n"
//                + "  WHERE\n"
//                + "    rn = 1\n"
//                + ")\n"
//                + "SELECT\n"
//                + "  DIM.status_new as status,\n"
//                + "  sum(part_uv) as uv\n"
//                + "FROM\n"
//                + "  (\n"
//                + "    SELECT\n"
//                + "      status,\n"
//                + "      count(distinct id) as part_uv\n"
//                + "    FROM\n"
//                + "      detail_tmp\n"
//                + "    GROUP BY\n"
//                + "      status,\n"
//                + "      mod(id, 100)\n"
//                + "  )\n"
//                + "LEFT JOIN LATERAL TABLE(status_mapper(status)) AS DIM(status_new) ON TRUE\n"
//                + "GROUP BY\n"
//                + "  DIM.status_new";
//
//        Table result = tEnv.sqlQuery(sql);
//
//        tEnv.toRetractStream(result, Row.class).print();
//
//        env.execute();
//    }
//}
