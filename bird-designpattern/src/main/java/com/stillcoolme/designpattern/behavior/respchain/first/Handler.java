package com.stillcoolme.designpattern.behavior.respchain.first;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 22:50
 * Function:
 */
public abstract class Handler {

    protected Handler successor = null;

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    // 这里用了模板模式，太妙了。将调用 successor.handle() 的逻辑从具体的处理器类中剥离出来，放到抽象父类中。
    public final void handle() {
        boolean handled = doHandle();
        if (successor != null && !handled) {
            successor.handle();
        }
    }

    protected abstract boolean doHandle();
}
