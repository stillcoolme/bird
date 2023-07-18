package com.stillcoolme.framework.flink.sql;

import org.apache.flink.table.functions.ScalarFunction;

/**
 * <p>分桶取模 udf</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/2/8 20:02
 */

public class Mod_UDF extends ScalarFunction {

    public int eval(long id, int remainder) {
        return (int) (id % remainder);
    }

}
