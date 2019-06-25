package com.stillcoolme.structure.linkedlist;

import com.stillcoolme.data.ListNode;

/**
 * Author: stillcoolme
 * Date: 2019/6/25 9:38
 * Description: com.stillcoolme.structure.linkedlist
 *  Given a linked list, remove the n-th node from the end of list and return its head.
 */
public class _19_RemoveNthNodeFromEndofList {

    /**
     * 我的解答：
     * 1. 用三个指针： 指针1先走n步，指针2和3在后面跟着到指针1指向最后一个节点
     * 2. 然后删除指针2对应的结点
     * 一次AC！！！
     * @param head
     * @param n
     * @return
     */
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummyHead = new ListNode(0);
        dummyHead.next = head;
        ListNode prepre = dummyHead;
        ListNode pre = head, curr = head;
        for (int i = 0; i < n - 1; i++) {
            curr = curr.next;
        }
        while (curr.next != null){
            curr = curr.next;
            pre = pre.next;
            prepre = prepre.next;
        }
        prepre.next = pre.next;
        return dummyHead.next;
    }

}
