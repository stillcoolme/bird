package com.stillcoolme.framework.flink.asyncio.flink_1_9;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.functions.AsyncTableFunction;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.sources.LookupableTableSource;
import org.apache.flink.table.sources.StreamTableSource;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.utils.TypeConversions;
import org.apache.flink.types.Row;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-09 11:39:00
 * @Description
 */
public class RedisAsyncLookupTableSource implements LookupableTableSource<Row>, StreamTableSource<Row> {

    private final String[] fieldNames;
    private final TypeInformation[] fieldTypes;

    public RedisAsyncLookupTableSource(String[] fieldNames, TypeInformation[] fieldTypes) {
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
    }

    // 同步方法
    @Override
    public TableFunction<Row> getLookupFunction(String[] strings) {
        return null;
    }

    // 异步方法
    @Override
    public AsyncTableFunction getAsyncLookupFunction(String[] strings) {
        return RedisAsyncLookupFunction.Builder.getBuilder()
                .withFieldNames(fieldNames)
                .withFieldTypes(fieldTypes)
                .build();

    }

    @Override
    public boolean isAsyncEnabled() {
        return true;
    }

    @Override
    public TableSchema getTableSchema() {
        return TableSchema.builder()
                .fields(fieldNames, TypeConversions.fromLegacyInfoToDataType(fieldTypes))
                .build();
    }


    @Override
    public DataStream<Row> getDataStream(StreamExecutionEnvironment streamExecutionEnvironment) {
        throw new UnsupportedOperationException("do not support getDataStream");
    }

    @Override
    public DataType getProducedDataType() {
        return TypeConversions.fromLegacyInfoToDataType(new RowTypeInfo(fieldTypes, fieldNames));
    }


    public static final class Builder {
        private String[] fieldNames;
        private TypeInformation[] fieldTypes;

        private Builder() {
        }

        public static Builder newBuilder() {
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

        public RedisAsyncLookupTableSource build() {
            return new RedisAsyncLookupTableSource(fieldNames, fieldTypes);
        }
    }
}

