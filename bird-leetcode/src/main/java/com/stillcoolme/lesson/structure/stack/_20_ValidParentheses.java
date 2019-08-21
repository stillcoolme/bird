package com.stillcoolme.lesson.structure.stack;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Author: still
 * Date: 2019/6/25 13:37
 * Description: com.stillcoolme.lesson.structure.stack
 *
 */
public class _20_ValidParentheses {

    /**
     * 我的解答：
     *  使用stack
     * @param s
     * @return
     */
    public boolean isValid(String s) {
        Stack stack = new Stack<Character>();
        char[] charArray = s.toCharArray();
        Map map = new HashMap<Character, Character>();
        map.put(')', '(');
        map.put(']', '[');
        map.put('}', '{');
        for (int i = 0; i < charArray.length; i++) {
            // 检查后括号
            if(map.keySet().contains(charArray[i])){
                // 此时stack要不为空
                if(!stack.isEmpty() && stack.peek().equals(map.get(charArray[i]))){
                    stack.pop();
                } else {
                    return false;
                }
            // 检查前括号
            } else if(map.values().contains(charArray[i])){
                stack.push(charArray[i]);
            } else {
                return false;
            }
        }
        if(stack.isEmpty()){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        _20_ValidParentheses validParentheses = new _20_ValidParentheses();
        boolean flag = validParentheses.isValid("{}[()]");
        System.out.println(flag);
    }
}
