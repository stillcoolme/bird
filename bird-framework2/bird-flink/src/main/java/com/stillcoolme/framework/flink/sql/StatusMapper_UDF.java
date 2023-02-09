package com.stillcoolme.framework.flink.sql;

import org.apache.flink.table.functions.TableFunction;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/2/8 20:00
 */
public class StatusMapper_UDF extends TableFunction<String> {

    private int i = 0;

    public void eval(String status) throws InterruptedException {

        if (i == 6) {
            Thread.sleep(2000L);
        }

        if (i == 5) {
            collect("等级4");
        } else {
            if ("1".equals(status)) {
                collect("等级1");
            } else if ("2".equals(status)) {
                collect("等级2");
            } else if ("3".equals(status)) {
                collect("等级3");
            }
        }
        i++;
    }


/*    public void eval(String status) {
        if (status.equals("1")) {
            collect("等级1");
        } else if (status.equals("2")) {
            collect("等级2");
        } else if (status.equals("3")) {
            collect("等级3");
        }
    }*/

}

