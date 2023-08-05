package com.stillcoolme.leetcode.algorithm.divideconquer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/7/10 8:21
 * @description:
 * 形成有效的左右括号,返回所有可能的结果
 **/
public class _22_GenerateParentheses {

    /**
     * 怎么知道这次要加什么括号？
     * 如果我们还剩一个位置，我们可以开始放一个左括号。 如果它不超过左括号的数量，我们可以放一个右括号。
     * @param n
     * @return
     */
    //TODO
    List<String> result = new ArrayList();
    public List<String> generateParenthesis(int n) {
        if(n <= 0) return result;
        _gen(n, n, "");
        return result;
    }

    private void _gen(int left, int right, String str) {
        if(left == 0 && right == 0){
            result.add(str);
        }
        if(left > 0){
            _gen(left - 1, right, str + "(");
        }
        if(right > 0 && right > left){
            _gen(left, right - 1, str + ")");
        }
    }

    public static void main(String[] args) {
        _22_GenerateParentheses generateParentheses = new _22_GenerateParentheses();
        List list = generateParentheses.generateParenthesis(3);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }


}
