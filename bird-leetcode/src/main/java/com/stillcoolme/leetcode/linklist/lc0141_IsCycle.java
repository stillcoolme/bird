package com.stillcoolme.leetcode.linklist;

import com.stillcoolme.leetcode.data.ListNode;

/**
 * 注意：走两步的时候，不要把 null 的 next 赋给节点，导致节点是 null， null.next 会报空指针！！
 */
public class lc0141_IsCycle {

    public boolean hasCycle(ListNode head) {
        ListNode step1 = head;
        ListNode step2 = head;
        while (step2 != null) {             // 这里不要用 step2.next 来判断 ！ 不然输入是 null 就挂了。
            if (step2 == null || step2.next == null || step2.next.next == null) {
                return false;
            } else {
                step2 = step2.next.next;
                step1 = step1.next;
                if (step1 == step2) {
                    return true;
                }
            }
        }
        return false;

    }
    
    public static void main(String[] args) {
        ListNode listNode2 = new ListNode(2, null);
        ListNode listNode1 = new ListNode(1, listNode2);
        lc0141_IsCycle lc0203removeElements = new lc0141_IsCycle();
        System.out.println(lc0203removeElements.hasCycle(listNode1));

    }

}
