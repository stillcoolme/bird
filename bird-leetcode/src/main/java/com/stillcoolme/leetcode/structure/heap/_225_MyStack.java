package com.stillcoolme.leetcode.structure.heap;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author: create by stillcoolme
 * @date: 2019/6/30 15:13
 * @description:
 * 堆实现栈
 **/
public class _225_MyStack {

    private Queue<Integer> queue1 = new LinkedList<>();
    private Queue<Integer> queue2 = new LinkedList<>();

    /** Initialize your data structure here. */
    public _225_MyStack() {

    }

    /** Push element x onto stack. */
    public void push(int x) {
        synchronized (queue1){
            queue1.add(x);
        }
    }

    // 把 q1 和 q2 互相交换的方式来避免把 q2 中的元素往 q1 中拷贝。
    /** Removes the element on top of the stack and returns that element. */
    public int pop() {
        while(queue1.size() > 1){
            Integer number = queue1.remove();
            queue2.add(number);
        }
        int returnNumber = queue1.remove();
        Queue<Integer> temp = queue1;
        queue1 = queue2;
        queue2 = temp;
        return returnNumber;
    }

    /** Get the top element. */
    public int top() {
        while(queue1.size() > 1){
            Integer number = queue1.remove();
            queue2.add(number);
        }
        int returnNumber = queue1.remove();
        queue2.add(returnNumber);
        Queue<Integer> temp = queue1;
        queue1 = queue2;
        queue2 = temp;
        return returnNumber;
    }

    /** Returns whether the stack is empty. */
    public boolean empty() {
        if(queue1.isEmpty()){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        _225_MyStack obj = new _225_MyStack();
        obj.push(10);
        obj.push(20);
        int param_2 = obj.pop();
        int param_3 = obj.top();
        boolean param_4 = obj.empty();
    }

}
