package com.stillcoolme.leetcode.linklist;

import com.stillcoolme.leetcode.data.ListNode;

/**
 * <p>将链表在位置 start 到 end 之间的 节点反转</p>
 * 2023-07-26 没做出来，对于  只是反链表中间的几个结点 与  反转整个链表的 两种情况，头节点要变化，不太会写啊！！
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/7/25 9:19
 */
public class lc0092reverseLinkList2 {

    public ListNode reverseBetween(ListNode head, int left, int right) {
        if (head == null || left == right) {
            return null;
        }
        return null;
    }

    /**
     * 反转前n个节点
     * @param head
     * @param n
     * @return
     */
    public ListNode reverseN(ListNode head, int n) {
        if (head == null || n -- == 0) {
            return null;
        }
        return null;
    }

    public static void main(String[] args) {

        ListNode listNode1 = new ListNode(1, null);
        ListNode listNode2 = new ListNode(2, listNode1);
        ListNode listNode3 = new ListNode(3, listNode2);
        ListNode listNode6 = new ListNode(6, listNode3);
        ListNode listNode5 = new ListNode(5, listNode6);
        ListNode listNode66 = new ListNode(6, listNode5);
        lc0092reverseLinkList2 lc0203removeElements = new lc0092reverseLinkList2();
        ListNode listNode = lc0203removeElements.reverseBetween(listNode66, 2, 3);
        System.out.println(listNode);
    }
}
