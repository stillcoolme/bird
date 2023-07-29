package com.stillcoolme.leetcode.linklist;

import com.stillcoolme.leetcode.data.ListNode;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/7/20 9:52
 */


public class lc0203removeElements {

    /**
     * 1. 需要构造多一个头节点！
     * 2. 不能用赋 null 来将节点置空 temp.next = null
     * @param head
     * @param val
     * @return
     */
    public ListNode removeElements(ListNode head, int val) {
        ListNode temp = new ListNode(0);
        temp.next = head;
        ListNode result = temp;
        while (temp.next != null) {
            if (temp.next.val == val) {
                temp.next = temp.next.next;
            } else {
                temp = temp.next;
            }
        }
        return result.next;
    }

    public static void main(String[] args) {
        ListNode listNode1 = new ListNode(1, null);
        ListNode listNode2 = new ListNode(2, listNode1);
        ListNode listNode3 = new ListNode(3, listNode2);
        ListNode listNode6 = new ListNode(6, listNode3);
        ListNode listNode5 = new ListNode(5, listNode6);
        ListNode listNode66 = new ListNode(6, listNode5);
        lc0203removeElements lc0203removeElements = new lc0203removeElements();
        ListNode listNode = lc0203removeElements.removeElements(listNode66, 6);
        System.out.println(listNode);

    }
}
