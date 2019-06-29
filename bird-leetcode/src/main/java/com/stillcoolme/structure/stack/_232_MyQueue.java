package com.stillcoolme.structure.stack;

import java.util.Stack;

/**
 * Author: still
 * Date: 2019/6/26 9:11
 * Description: com.stillcoolme.structure.stack
 * Implement the following operations of a queue using stacks.
 *
 * push(x) -- Push element x to the back of queue.
 * pop() -- Removes the element from in front of queue.
 * peek() -- Get the front element.
 * empty() -- Return whether the queue is empty.
 * 我的解法：用两个stack来实现heap
 */
public class _232_MyQueue {

    Stack inputStack;
    Stack outputStack;

    /** Initialize your data structure here. */
    public _232_MyQueue() {
        inputStack = new Stack<Integer>();
        outputStack = new Stack<Integer>();
    }

    /** Push element x to the back of queue. */
    public void push(int x) {
        inputStack.push(x);
    }

    /** Removes the element from in front of queue and returns that element. */
    public int pop() {
        synchronized (inputStack){
            if(outputStack.isEmpty()){
                while(!inputStack.isEmpty()){
                    outputStack.push(inputStack.pop());
                }
            }
            if(!outputStack.isEmpty()){
                return (int) outputStack.pop();
            }
        }
        return 0;
    }

    /** Get the front element. */
    public int peek() {
        synchronized (inputStack){
            if(outputStack.isEmpty()){
                while(!inputStack.isEmpty()){
                    outputStack.push(inputStack.pop());
                }
            }
            if(!outputStack.isEmpty()){
                return (int) outputStack.peek();
            }
        }
        return 0;
    }

    /** Returns whether the queue is empty. */
    public boolean empty() {
        if(outputStack.isEmpty() && inputStack.isEmpty()){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
         _232_MyQueue obj = new _232_MyQueue();
         obj.push(10);
         int param_3 = obj.peek();
         int param_2 = obj.pop();
         boolean param_4 = obj.empty();
        System.out.println(param_2 + " " + param_3 + " " + param_4);
    }

}
