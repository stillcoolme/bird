package com.stillcoolme.designpattern.behavior.respchain.first;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 22:57
 * Function:
 */
public class HandlerChain {

    private Handler head = null;
    private Handler tail = null;

    public void addHandler(Handler handler) {
        handler.setSuccessor(null);
        if (head == null) {
            head = handler;
            tail = handler;
            return;
        }
        // 之前的尾巴设置自己的下一个handler，就是现在进来的这个handler
        tail.setSuccessor(handler);
        tail = handler;
    }

    public void handle() {
        if (head != null) {
            head.handle();
        }
    }

}
