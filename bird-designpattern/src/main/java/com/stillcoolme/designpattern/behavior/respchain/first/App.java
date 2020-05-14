package com.stillcoolme.designpattern.behavior.respchain.first;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 22:59
 * Function:
 *  通过 HandlerChain 的 head tail 来存放责任链
 */
public class App {
    public static void main(String[] args) {
        HandlerChain chain = new HandlerChain();
        chain.addHandler(new HandlerA());
        chain.addHandler(new HandlerB());
        chain.handle();
    }
}
