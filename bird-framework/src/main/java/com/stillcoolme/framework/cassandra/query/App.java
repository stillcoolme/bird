package com.stillcoolme.framework.cassandra.query;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.internal.core.type.codec.CounterCodec;
import com.stillcoolme.framework.cassandra.CassandraClient;

import java.util.concurrent.*;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.update;

/**
 * @author: stillcoolme
 * @date: 2019/11/29 10:29
 * @description:
 */
public class App {

    public static void main(String[] args) {

        ExecutorService executorService =
                new ThreadPoolExecutor(10,
                        20,
                        10,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(10));

        CqlSession cassandraSession = CassandraClient.getCassandraSession();
        for (int i = 0; i < 5; i++) {
            executorService.submit(new CounterRunnabnle(cassandraSession, "task" + i));
        }
    }


    private static class CounterRunnabnle implements Runnable {
        private CqlSession session;
        private String taskName;

        public CounterRunnabnle(CqlSession cqlSession, String taskName) {
            this.session = cqlSession;
            this.taskName = taskName;
        }

        @Override
        public void run() {
            ResultSet resultSet = session
                    .execute(String.format("select id from ?.ks_face_counter where table_name = ?", "ks_facedb", "ks_face_counter", "table_name"));
            Select query1 = selectFrom("ks_facedb", "ks_face_counter").columns("table_name", "id");
            SimpleStatement build = query1.build();

            ResultSet set = session.execute(build);
            Row row1 = set.one();
            System.out.println(row1.getString(0) + " " + row1.getLong(1));

            // === transaction 开始

            // 获取 id，设置到其他表中

            // 增加 id
            update("ks_facedb", "ks_face_counter")
                    // UPDATE ks_face_counter SET id+=1...
                    .increment("id");


            // === transaction 结束

        }
    }
}
