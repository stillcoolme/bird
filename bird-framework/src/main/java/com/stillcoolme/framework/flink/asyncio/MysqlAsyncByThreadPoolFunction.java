package com.stillcoolme.framework.flink.asyncio;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.*;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-12-27 09:45:00
 * @Description 此类采用 线程池模拟异步mysql客户端 来实现asyncio
 */
public class MysqlAsyncByThreadPoolFunction extends RichAsyncFunction<String, String> {
    private transient MysqlSyncClient client;
    private ExecutorService executorService;
    public static String SEARCH_COLUMN = "nickname";
    public static String SEARCH_SQL = "select " + SEARCH_COLUMN +" from dlink_user where username = '%s';";

    @Override
    public void asyncInvoke(String input, ResultFuture resultFuture) throws Exception {
        String sql = String.format(SEARCH_SQL, input);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                resultFuture.complete(
                        Collections.singletonList(client.query(sql))
                );
            }
        });
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        client = new MysqlSyncClient();
        executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    @Override
    public void timeout(String input, ResultFuture<String> resultFuture) throws Exception {
        System.out.println("async call time out!");
        resultFuture.complete(Collections.singleton(input));
    }


    static class MysqlSyncClient<T> {
        private static transient Connection connection;
        private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        private static final String URL = "jdbc:mysql://127.0.0.1:3306/dlink";
        private static final String USER = "root";
        private static final String PASSWORD = "123456";

        static {
            init();
        }

        private static void init() {
            try {
                Class.forName(JDBC_DRIVER);
            } catch (ClassNotFoundException e) {
                System.out.println("Driver not found!" + e.getMessage());
            }
            try {
                connection = DriverManager.getConnection(URL, USER,
                        PASSWORD);
            } catch (SQLException e) {
                System.out.println("init connection failed!" + e.getMessage());
            }
        }

        public void close() {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("close connection failed!" + e.getMessage());
            }
        }

        public String query(String sql) {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet =
                        statement.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                     return resultSet.getString(1);
                }
            } catch (SQLException e) {
                System.out.println("query failed!" + e.getMessage());
            }
            return null;
        }

    }

}
