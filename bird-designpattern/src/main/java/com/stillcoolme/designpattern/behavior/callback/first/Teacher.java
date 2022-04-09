package com.stillcoolme.designpattern.behavior.callback.first;

/**
 * Author: stillcoolme
 * Date: 2019/9/18 15:26
 * Description:
 *
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

    /**
     * 对调用方的具体回答，事先定义逻辑处理
     * @param answer
     */
    @Override
    public void tellAnswer(int answer) {
        if(answer == 1) {
            System.out.println("知道了，你的答案是" + answer);
        } else {
            System.out.println("知道了，你的答案是" + answer + ", 这是不正确的");
        }
    }

}
