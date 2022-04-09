package com.stillcoolme.framework.flink.asyncio;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-12-26 19:44:00
 * @Description AsyncIO 例子
 * 一般关联维表信息通过mapFunction以阻塞的方式查询数据库，等待数据结果返回；
 * 要想性能更好Flink中提供了一种异步IO的模式。API很简洁让用户只需要关注业务逻辑，一些脏活累活比如消息顺序性和一致性保证都由框架处理了。
 * 使用 Async I/O 的前提是需要一个支持异步请求的客户端。没有异步请求客户端的话也可以将同步客户端丢到线程池中执行作为异步客户端。
 */
@Slf4j
public class MysqlAsyncFunction extends RichAsyncFunction<String, String> {

    private transient SQLClient mySQLClient;
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/dlink";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";
    public static String SEARCH_COLUMN = "nickname";
    public static String SEARCH_SQL = "select " + SEARCH_COLUMN +" from dlink_user where username = '%s';";


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        JsonObject mySQLClientConfig = new JsonObject();
        mySQLClientConfig.put("url", URL)
                .put("driver_class", JDBC_DRIVER)
                .put("max_pool_size", 20)
                .put("user", USER)
                .put("password", PASSWORD);

        VertxOptions vo = new VertxOptions();
        vo.setEventLoopPoolSize(10);
        vo.setWorkerPoolSize(20);
        Vertx vertx = Vertx.vertx(vo);
        mySQLClient = JDBCClient.createNonShared(vertx, mySQLClientConfig);
    }

    public void close() throws Exception {
        super.close();
        mySQLClient.close();
    }

    /**
     * {@link MysqlAsyncFunction#asyncInvoke} timeout occurred.
     * By default, the result future is exceptionally completed with a timeout exception.
     *
     * @param input        element coming from an upstream task
     * @param resultFuture to be completed with the result data
     */
    @Override
    public void timeout(String input, ResultFuture<String> resultFuture) throws Exception {
        System.out.println("async call time out!");
        resultFuture.complete(Collections.singleton(input));
    }

    @Override
    public void asyncInvoke(String input, ResultFuture<String> resultFuture) throws Exception {
        mySQLClient.getConnection(new Handler<AsyncResult<SQLConnection>>() {
            @Override
            public void handle(AsyncResult<SQLConnection> sqlConnectionAsyncResult) {
                if (sqlConnectionAsyncResult.failed()) {
                    System.out.println("connection fail");
                    log.info("conn fail");
                    return;
                }
                SQLConnection connection = sqlConnectionAsyncResult.result();
                System.out.println(SEARCH_SQL);
                connection.query(String.format(SEARCH_SQL, input), new Handler<AsyncResult<ResultSet>>() {
                    @Override
                    public void handle(AsyncResult<ResultSet> resultSetAsyncResult) {
                        if (resultSetAsyncResult.succeeded()) {
                            List<JsonObject> rows = resultSetAsyncResult.result().getRows();
                            for (JsonObject ele : rows) {
                                resultFuture.complete(
                                        Collections.singletonList(ele.getString(SEARCH_COLUMN))
                                );
                            }
                        }
                    }
                });
            }
        });
    }
}

