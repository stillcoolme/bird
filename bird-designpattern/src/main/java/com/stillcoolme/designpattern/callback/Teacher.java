package com.stillcoolme.designpattern.callback;

/**
 * Author: stillcoolme
 * Date: 2019/9/18 15:26
 * Description:
 *  定义一个老师对象，实现Callback接口
 *  老师对象有两个public方法：
 * （1）回调接口方法tellAnswer(int answer)，即学生回答完毕问题之后，老师要做的事情
 * （2）问问题方法askQuestion()，即向学生问问题
 */
public class Teacher implements Callback {

    private Student student;

    public Teacher(Student student) {
        this.student = student;
    }

    public void askQuestion() {
        System.out.println("问题一的答案是？");
        student.resolveQuestion(this);
    }

    @Override
    public void tellAnswer(int answer) {
        System.out.println("知道了，你的答案是" + answer);
    }

}
