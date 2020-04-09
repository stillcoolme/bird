package com.stillcoolme.framework.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author: stillcoolme
 * @date: 2020/3/26 22:21
 * Function:
 */
public class DataEventFactory implements EventFactory<DataEvent> {

    @Override
    public DataEvent newInstance() {
        return new DataEvent();
    }
}
