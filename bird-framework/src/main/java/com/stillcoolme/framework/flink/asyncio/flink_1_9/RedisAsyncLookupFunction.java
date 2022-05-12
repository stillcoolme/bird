package com.stillcoolme.framework.flink.asyncio.flink_1_9;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.table.functions.AsyncTableFunction;
import org.apache.flink.types.Row;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-09 11:43:00
 * @Description
 */
public class RedisAsyncLookupFunction extends AsyncTableFunction<Row> {

    private final String[] fieldNames;
    private final TypeInformation[] fieldTypes;

    //  外部数据源必须是异步客户端：如果是线程安全的(多个客户端一起使用)，你可以不加 transient 关键字,初始化一次。
    //  否则，你需要加上 transient，不对其进行初始化，而在 open 方法中，为每个 Task 实例初始化一个。
    private transient RedisAsyncCommands<String, String> async;

    public RedisAsyncLookupFunction(String[] fieldNames, TypeInformation[] fieldTypes) {
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
    }

    public void open() {
        // 配置redis异步连接
        // Lettuce的API是线程安全的，所以其实可以操作单个Lettuce连接来完成各种操作，不用再这初始化多个。。
        RedisClient redisClient = RedisClient.create("redis://127.0.0.1:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        async = connection.async();

    }

    //每一条流数据都会调用此方法进行join
    public void eval(CompletableFuture<Collection<Row>> future, Object... paramas) {
        //表名、主键名、主键值、列名
        String[] info = {"userInfo", "userId", paramas[0].toString(), "userName"};
        String key = String.join(":", info);
        RedisFuture<String> redisFuture = async.get(key);

        redisFuture.thenAccept(new Consumer<String>() {
            @Override
            public void accept(String value) {
                future.complete(Collections.singletonList(Row.of(key, value)));
                //todo
//                BinaryRow row = new BinaryRow(2);
            }
        });
    }

    @Override
    public TypeInformation<Row> getResultType() {
        return new RowTypeInfo(fieldTypes, fieldNames);
    }

    public static final class Builder {
        private String[] fieldNames;
        private TypeInformation[] fieldTypes;

        private Builder() {
        }

        public static Builder getBuilder() {
            return new Builder();
        }

        public Builder withFieldNames(String[] fieldNames) {
            this.fieldNames = fieldNames;
            return this;
        }

        public Builder withFieldTypes(TypeInformation[] fieldTypes) {
            this.fieldTypes = fieldTypes;
            return this;
        }

        public RedisAsyncLookupFunction build() {
            return new RedisAsyncLookupFunction(fieldNames, fieldTypes);
        }
    }


}
