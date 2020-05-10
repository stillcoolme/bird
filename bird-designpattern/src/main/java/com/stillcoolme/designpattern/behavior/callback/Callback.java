package com.stillcoolme.designpattern.behavior.callback;

/**
 * Author: stillcoolme
 * Date: 2019/9/18 15:23
 * Description:
 *  回调接口，即学生思考完毕告诉老师答案
 */
public interface Callback {

    public void tellAnswer(int answer);
}
