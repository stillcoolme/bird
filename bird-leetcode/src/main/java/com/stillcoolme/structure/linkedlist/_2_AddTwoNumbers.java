package com.stillcoolme.structure.linkedlist;

import com.stillcoolme.data.ListNode;

public class _2_AddTwoNumbers {

    /**
     * 我的解答
     * 1. 有进位就将addOne设为true，然后在下一轮加1
     * 2. 其实看官方的示例，进位就是 new ListNode(1); 啊
     * @param l1
     * @param l2
     * @return
     */
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        boolean addOne = false;
        ListNode head = new ListNode(0);
        ListNode tail = head;
        // 用 || 就将其中一个链表遍历到最后了的情况一起处理
        while(l1 != null || l2 != null){
            if(l1 == null){
                l1 = new ListNode(0);
            }
            if(l2 == null){
                l2 = new ListNode(0);
            }
            int tempCount = l1.val + l2.val;
            // 相加超过10的要设置下个位数加1
            if(tempCount >= 10){
                tail.next = new ListNode((tempCount - 10));
                if(addOne){
                    tail.next.val += 1;
                }
                addOne = true;
            } else {
                tail.next = new ListNode(tempCount);
                if(addOne){
                    tail.next.val += 1;
                }
                addOne = false;
                // 有一种情况是 等于9，然后上面加了1就又要进位了。
                if(tail.next.val == 10){
                    tail.next.val = 0;
                    addOne = true;
                }
            }
            tail = tail.next;
            l1 = l1.next;
            l2 = l2.next;
        }
        if(addOne){
            tail.next = new ListNode(1);
        }
        return head.next;
    }

    public static void main(String[] args) {
        _2_AddTwoNumbers addTwoNumbers = new _2_AddTwoNumbers();

//        ListNode listNode0 = ListNode.createTestData("[5]");
//        ListNode listNode1 = ListNode.createTestData("[5]");

//        ListNode listNode0 = ListNode.createTestData("[1,8]");
//        ListNode listNode1 = ListNode.createTestData("[0]");

//        ListNode listNode0 = ListNode.createTestData("[9,8]");
//        ListNode listNode1 = ListNode.createTestData("[1]");

          ListNode listNode0 = ListNode.createTestData("[9,9]");
          ListNode listNode1 = ListNode.createTestData("[1]");

        ListNode.print(listNode0);
        ListNode.print(listNode1);
        ListNode.print(addTwoNumbers.addTwoNumbers(listNode0, listNode1));
    }
}
