package com.stillcoolme.designpattern.behavior.respchain.first;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 22:52
 * Function:
 */
public class HandlerA extends Handler{

    @Override
    protected boolean doHandle() {
        boolean handled = false;
        //...
        return handled;
    }
}
