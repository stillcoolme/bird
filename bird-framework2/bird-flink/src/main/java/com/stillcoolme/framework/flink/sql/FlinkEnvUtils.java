package com.stillcoolme.framework.flink.sql;

import lombok.Builder;
import lombok.Data;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FlinkEnvUtils {
    private static final boolean ENABLE_INCREMENTAL_CHECKPOINT = true;
    private static final int NUMBER_OF_TRANSFER_THREADS = 3;

    @Data
    @Builder
    public class FlinkEnv {
        // 要使⽤ Java DataStream API 去写⼀个 Flink 任务就需要使⽤到 StreamExecutionEnvironment
        private StreamExecutionEnvironment streamExecutionEnvironment;
        // TableEnvironment 是使⽤ SQL API 永远都离不开的⼀个接⼝。
        private StreamTableEnvironment streamTableEnvironment;
        private TableEnvironment tableEnvironment;

        public StreamExecutionEnvironment env() {
            return this.streamExecutionEnvironment;
        }

        public StreamTableEnvironment streamTEnv() {
            return this.streamTableEnvironment;
        }

        public TableEnvironment batchEnv() {
            return this.tableEnvironment;
        }

    }

    /**
     * 创建 流SQL Env环境
     * @param args
     * @return
     */
    public static FlinkEnv getStreamTableEnv(String[] args) throws IOException {
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        Configuration configuration = Configuration.fromMap(parameterTool.toMap());
        configuration.setString("rest.flamegraph.enabled", "true");

        // 先构建 Java DataStream API Env环境
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        String stateBackend = parameterTool.get("state.backend", "rocksdb");

        env.setParallelism(1);

        if ("rocksdb".equals(stateBackend)) {
            setRocksDBStateBackend(env);
        } else if ("filesystem".equals(stateBackend)) {
            setFsStateBackend(env);
        } else if ("jobmanager".equals(stateBackend)) {
            setMemoryStateBackend(env);
        }

        env.setRestartStrategy(RestartStrategies.failureRateRestart(6, org.apache.flink.api.common.time.Time
                .of(10L, TimeUnit.MINUTES), org.apache.flink.api.common.time.Time.of(5L, TimeUnit.SECONDS)));
        env.getConfig().setGlobalJobParameters(parameterTool);


        EnvironmentSettings envSetting = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, envSetting);

        FlinkEnv flinkEnv = FlinkEnv.builder()
                .streamExecutionEnvironment(env)
                .tableEnvironment(tEnv)
                .build();
//        initHiveEnv(flinkEnv, parameterTool);
        return flinkEnv;

    }

    private static void setMemoryStateBackend(StreamExecutionEnvironment env) {
        setCheckpointConfig(env);
        env.setStateBackend(new MemoryStateBackend());
    }

    private static void setRocksDBStateBackend(StreamExecutionEnvironment env) throws IOException {
        setCheckpointConfig(env);
        env.setStateBackend(new EmbeddedRocksDBStateBackend(true));
  		env.getCheckpointConfig().setCheckpointStorage("file:///Users/flink/checkpoints");

    }

    /**
     * 设置状态后端 FsStateBackend
     * @param env
     */
    private static void setFsStateBackend(StreamExecutionEnvironment env) {
        setCheckpointConfig(env);
    }

    private static void setCheckpointConfig(StreamExecutionEnvironment env) {
        env.getCheckpointConfig().setCheckpointTimeout(TimeUnit.MINUTES.toMillis(3));
        // ck 设置
        env.getCheckpointConfig().setFailOnCheckpointingErrors(false);
        env.enableCheckpointing(180 * 1000L, CheckpointingMode.EXACTLY_ONCE);

        Configuration configuration = new Configuration();
        configuration.setString("state.checkpoints.num-retained", "3");

        env.configure(configuration, Thread.currentThread().getContextClassLoader());
        env.getCheckpointConfig()
                .enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
    }

}
