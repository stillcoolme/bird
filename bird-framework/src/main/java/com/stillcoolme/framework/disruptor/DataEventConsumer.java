package com.stillcoolme.framework.disruptor;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: stillcoolme
 * @date: 2020/3/26 22:25
 * Function:
 */
@Slf4j
public class DataEventConsumer {

    public DataEventConsumer(DataEvent dataEvent) {
        // 业务逻辑 对事件dataEvent进行处理
        log.info("处理业务数据：{}" , dataEvent.getValue());
    }
}
