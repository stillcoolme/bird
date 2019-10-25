package com.stillcoolme.designpattern.callback;

/**
 * Author: stillcoolme
 * Date: 2019/9/18 15:29
 * Description:
 */
public class StudentTom implements Student {
    @Override
    public void resolveQuestion(Callback callback) {
        // 模拟解决问题
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // 回调，告诉老师作业写了多久
        callback.tellAnswer(3);
    }
}
