package com.stillcoolme.leetcode.all;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/5/17 11:00
 */
//给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串 s ，判断字符串是否有效。
//
// 有效字符串需满足：
//
//
// 左括号必须用相同类型的右括号闭合。
// 左括号必须以正确的顺序闭合。
// 每个右括号都有一个对应的相同类型的左括号。
//
//
//
//
// 示例 1：
//
//
//输入：s = "()"
//输出：true
//
//
// 示例 2：
//
//
//输入：s = "()[]{}"
//输出：true
//
//
// 示例 3：
//
//
//输入：s = "(]"
//输出：false
//
//
//
//
// 提示：
//
//
// 1 <= s.length <= 10⁴
// s 仅由括号 '()[]{}' 组成
//
// Related Topics 栈 字符串 👍 3925 👎 0


import java.util.Stack;

//leetcode submit region begin(Prohibit modification and deletion)
class lc0020valid_kuohao {
    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        char[] arr = s.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '(' || arr[i] == '[' || arr[i] == '{') {
                stack.push(arr[i]);
            } else if(arr[i] == ')' || arr[i] == ']' || arr[i] == '}'){
                if (stack.isEmpty()) {
                    return false;
                }
                char pop = stack.pop();
                /**
                 * 我这里弄复杂了，用了数学的思维要一一对应来判断。其实高效的解法是
                 * 每遇到一个 左括号，就再将一个 右括号 入栈即可
                 */
                if ((arr[i] == ')' && pop == '(')
                     || (arr[i] == '}' && pop == '{')
                     || (arr[i] == ']' && pop == '[')) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        if (stack.isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

    public static void main(String[] args) {
        lc0020valid_kuohao solution = new lc0020valid_kuohao();
        boolean valid = solution.isValid("[{}]");
        System.out.println(valid);
    }
}
//leetcode submit region end(Prohibit modification and deletion)

