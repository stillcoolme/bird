package com.stillcoolme.leetcode.structure.linkedlist;

import com.stillcoolme.leetcode.data.ListNode;

/**
 * 2023-07-24 10min做不出来， 不知到往前的指针怎么变成往后？
 */
public class lc0206_ReverseLinkList {


    public ListNode reverseList(ListNode head) {
        ListNode pre = null;
        ListNode curr = head;

        //  null (pre)  -> head (curr)  ->  1  (temp)  -> 2  -> 3

        while (curr != null) {
            ListNode temp = curr.next;    // 让 temp 在前面做前导， 不然 curr 的 next 掉转方向了找不到 前面的节点
            curr.next = pre;              // curr 往后指
            pre = curr;                   // curr 往后指完，大家继续前进
            curr = temp;                  // curr 往后指完，大家继续前进
        }
        return pre;
    }


    public static void main(String[] args) {

    }
}
