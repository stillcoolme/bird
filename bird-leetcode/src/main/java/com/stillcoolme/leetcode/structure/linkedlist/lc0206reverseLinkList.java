package com.stillcoolme.leetcode.structure.linkedlist;

import com.stillcoolme.leetcode.data.ListNode;

/**
 * 2023-07-24 10min做不出来， 不知到往前的指针怎么变成往后？
 */
<<<<<<<< HEAD:bird-leetcode/src/main/java/com/stillcoolme/leetcode/structure/linkedlist/lc0206reverseLinkList.java
public class lc0206reverseLinkList {
========
public class lc0206_ReverseLinkList {
>>>>>>>> d20e00e0ee0a0e9c12588cb0364b61b1af96356b:bird-leetcode/src/main/java/com/stillcoolme/leetcode/structure/linkedlist/lc0206_ReverseLinkList.java


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

    /**
     * 递归的方法
     * @param head
     * @return
     */
    public ListNode reverseList2(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode newHead = reverseList(head.next);
        // 下面处理的是 因为递归的原因，除了头节点，后面的节点都已经反转了，画图分析一下会比较直观
        head.next.next = head;
        head.next = null;
        return newHead;
    }



    public static void main(String[] args) {

    }
}
