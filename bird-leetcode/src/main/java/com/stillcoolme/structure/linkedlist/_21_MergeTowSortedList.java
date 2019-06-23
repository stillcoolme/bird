package com.stillcoolme.structure.linkedlist;

/**
 * @author: create by stillcoolme
 * @description: com.stillcoolme.structure.linkedlist
 * @date:2019/6/23
 * 将两个有序链表合并为一个新的有序链表并返回。新链表是通过拼接给定的两个链表的所有节点组成的。
 * **示例:
 *
 * ```
 * 输入：1->2->4, 1->3->4
 * 输出：1->1->2->3->4->4
 * ```
 **/
public class _21_MergeTowSortedList {

    /**
     * 普通解题思路：
     * 1. 使用头结点root进行辅助操作，创建一个头结点，再使用两个引用指向两个链表的头结点；
     * 2. 对空ListNode进行处理：对空链表存在的情况进行处理，假如 l1 为空则返回 l2 ，l2 为空则返回 l1。（两个都为空此情况在l1为空已经被拦截）
     * 3. 在两个节点不为空时确定第一个结点：比较链表1和链表2的第一个结点的值，将值小的结点保存为合并后的第一个结点。并且把第一个结点为最小的链表向后移动一个元素。
     * 4. 直到有链表为空，此时需要把另外一个链表剩下的元素都连接到第一个结点的后面。
     * 5. 最后返回root的下一个结点，其就为新的链表头。
     * @param l1
     * @param l2
     * @return
     */
    public ListNode mergeTwoOrderedLists(ListNode l1, ListNode l2){
        // 创建一个头结点，最后还要删除掉
        ListNode head = new ListNode(0);
        ListNode tail = head;
        /*
        // 这样写真难看啊。。。
        if(l1 == null){
            return null;
        } else if(l2 == null){
            return l1;
        } else {
            while(l1.next != null || l2.next != null) {
            }
        }
        */
        while (l1 != null && l2 != null){
            if(l1.val <= l2.val){
                tail.next = l1;
                l1 = l1.next;
            } else {
                tail.next = l2;
                l2 = l2.next;
            }
            // 向后移动，移动到新的尾结点
            tail = tail.next;
        }
        tail.next = l1 == null ? l2 : l1;
        // head的下一个节点是第一个数据结点
        return head.next;
    }

    public class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
            next = null;
        }
    }
}
