package com.stillcoolme.lesson.structure.linkedlist;

import com.stillcoolme.data.ListNode;

/**
 * @author: stillcoolme
 * @date: 2019/8/29 8:53
 * @description:
 *  交换链表中两两相邻的节点
 **/
public class SwapNodesInPairs_24 {

    /**
     * 别人的非递归解法
     * @param head
     * @return
     */
    public ListNode swapPairs(ListNode head) {
        ListNode dummyNode = new ListNode(0);
        dummyNode.next = head;
        ListNode temp = dummyNode;

        while(temp.next != null && temp.next.next != null) {
            ListNode start = temp.next;
            ListNode end = temp.next.next;
            // 交换
            start.next = end.next;
            end.next = start;
            temp.next = end;
            // 将指针移动位置
            temp = start;
        }
        return dummyNode.next;
    }

    /**
     * 别人的递归解法
     * 其中我们应该关心的主要有三点:
     * 返回值
     * 调用单元做了什么
     * 终止条件
     *
     * 在本题中:
     *  返回值：交换完成的子链表
     *  调用单元：设需要交换的两个点为 head 和 next，head 连接后面交换完成的子链表，next 连接 head，完成交换
     *  终止条件：head 为空指针或者 next 为空指针，也就是当前无节点或者只有一个节点，无法进行交换
     * @param head
     * @return
     */
    public ListNode swapPairs2(ListNode head) {
        if(head == null || head.next == null) {
            return head;
        }
        ListNode next = head.next;
        head.next = swapPairs2(next.next);
        next.next = head;
        return next;
    }



    public static void main(String[] args) {
        ListNode list = ListNode.createTestData("[1,2,3,4]");
        SwapNodesInPairs_24 swapNodesInPairs_24 = new SwapNodesInPairs_24();
        ListNode result = swapNodesInPairs_24.swapPairs2(list);
        ListNode.print(result);
    }
}
