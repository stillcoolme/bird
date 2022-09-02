package com.stillcoolme.framework.flink.sql;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class FirstTest {

    public static void main(String[] args) {

        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .build();
        TableEnvironment tableEnvironment = TableEnvironment.create(settings);

    }
}
