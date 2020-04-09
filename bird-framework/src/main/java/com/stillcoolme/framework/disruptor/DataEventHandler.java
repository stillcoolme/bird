package com.stillcoolme.framework.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * @author: stillcoolme
 * @date: 2020/3/26 22:24
 * Function:
 */
public class DataEventHandler implements EventHandler<DataEvent> {

    @Override
    public void onEvent(DataEvent dataEvent, long l, boolean b) throws Exception {
        new DataEventConsumer(dataEvent);
    }
}
