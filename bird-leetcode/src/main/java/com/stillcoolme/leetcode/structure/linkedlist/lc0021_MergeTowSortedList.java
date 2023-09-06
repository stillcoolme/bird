package com.stillcoolme.leetcode.structure.linkedlist;

import com.stillcoolme.leetcode.data.ListNode;

/**
 * @author: create by stillcoolme
 * @description: com.stillcoolme.leetcode.lesson.structure.linkedlist
 * @date:2019/6/23
 * 将两个有序链表合并为一个新的有序链表并返回。（easy）
 * **示例:
 *
 * ```
 * 输入：1->2->4, 1->3->4
 * 输出：1->1->2->3->4->4
 * ```
 **/
public class lc0021_MergeTowSortedList {

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
    public ListNode mergeTwoLists(ListNode l1, ListNode l2){
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

    /**
     * 递归解题思路
     *（1）对空链表存在的情况进行处理，假如 pHead1 为空则返回 pHead2 ，pHead2 为空则返回 pHead1。
     *（2）比较两个链表第一个结点的大小，确定头结点的位置
     *（3）头结点确定后，继续在剩下的结点中选出下一个结点去链接到第二步选出的结点后面，然后在继续重复（2 ）（3） 步，直到有链表为空。
     * @param l1
     * @param l2
     * @return
     */
    public ListNode mergeTwoLists2(ListNode l1, ListNode l2){
        ListNode head;
        if(l1 == null){
            return l2;
        } else if(l2 == null){
            return l1;
        } else {
            if(l1.val < l2.val){
                head = l1;
                head.next = mergeTwoLists2(l1.next, l2);
            } else {
                head = l2;
                head.next = mergeTwoLists2(l1, l2.next);
            }
            return head;
        }
    }

    public static void main(String[] args) {
        lc0021_MergeTowSortedList solution = new lc0021_MergeTowSortedList();
        ListNode listNode0 = ListNode.createTestData("[1,3,5,7,9]");
        ListNode listNode1 = ListNode.createTestData("[2,3,6,8,10]");
        ListNode.print(listNode0);
        ListNode.print(listNode1);
        ListNode.print(solution.mergeTwoLists(listNode0, listNode1));
    }
}
