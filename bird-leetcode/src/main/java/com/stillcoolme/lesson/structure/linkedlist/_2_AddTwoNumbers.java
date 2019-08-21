package com.stillcoolme.lesson.structure.linkedlist;

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
        // 求和运算最后可能出现额外的进位，这一点很容易被遗忘。
        if(addOne){
            tail.next = new ListNode(1);
        }
        return head.next;
    }

    /**
     * 官方解答：
     * 1. 将当前结点初始化为返回列表的哑结点。
     * 2. 将进位 carrycarry 初始化为 00。
     * 3. 将 pp 和 qq 分别初始化为列表 l1l1 和 l2l2 的头部。
     * 4. 遍历列表 l1l1 和 l2l2 直至到达它们的尾端。
     *  将 xx 设为结点 pp 的值。如果 pp 已经到达 l1l1 的末尾，则将其值设置为 00。
     *  将 yy 设为结点 qq 的值。如果 qq 已经到达 l2l2 的末尾，则将其值设置为 00。
     *  设定 sum = x + y + carrysum=x+y+carry。
     *  更新进位的值，carry = sum / 10carry=sum/10。
     *  创建一个数值为 (sum \bmod 10)(summod10) 的新结点，并将其设置为当前结点的下一个结点，然后将当前结点前进到下一个结点。
     *  同时，将 pp 和 qq 前进到下一个结点。
     * 5. 检查 carry = 1carry=1 是否成立，如果成立，则向返回列表追加一个含有数字 11 的新结点。
     * 6. 返回哑结点的下一个结点。
     * @param l1
     * @param l2
     * @return
     */
    public ListNode addTwoNumbers2(ListNode l1, ListNode l2) {
        ListNode dummyHead = new ListNode(0);
        ListNode p = l1, q = l2, curr = dummyHead;
        int carry = 0;
        while(p != null || q != null){
            // 看人家的多简洁
            int x = (p != null) ? p.val : 0;
            int y = (q != null) ? q.val : 0;
            // 厉害
            int sum = x + y + carry;
            // 用除来获取进位、用%来得到这一位的结果！
            carry = sum / 10;
            curr.next = new ListNode(sum % 10);
            curr = curr.next;
            if(p != null) p = p.next;
            if(q != null) q = q.next;
        }
        if(carry > 0){
            curr.next = new ListNode(carry);
        }
        return dummyHead.next;
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
        ListNode.print(addTwoNumbers.addTwoNumbers2(listNode0, listNode1));
    }
}
