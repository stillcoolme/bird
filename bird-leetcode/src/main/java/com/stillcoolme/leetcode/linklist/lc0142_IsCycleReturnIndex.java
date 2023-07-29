package com.stillcoolme.leetcode.linklist;

import com.stillcoolme.leetcode.data.ListNode;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> 这个如果用双指针法，要想的挺深的
 * </p>
 *
 * @author zhangjianhua
 * @version V1.0.0
 * @date 2023/7/29 23:24 周六
 */
public class lc0142_IsCycleReturnIndex {

    /**
     * @param head
     * @return
     */
    public ListNode detectCycle(ListNode head) {
        Map<ListNode, Integer> nodeMap = new HashMap();
        ListNode curr = head;
        Integer index = 0;
        while (curr != null) {
            nodeMap.put(curr, index ++);
            curr = curr.next;

            // 通过查询map看 之前是否走过该节点
            Integer repeatIndex = nodeMap.get(curr);
            if (repeatIndex != null) {
                return curr;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        ListNode listNode2 = new ListNode(2, null);
        ListNode listNode1 = new ListNode(1, listNode2);
        listNode2.next = listNode1;
        lc0142_IsCycleReturnIndex lc0203removeElements = new lc0142_IsCycleReturnIndex();
        ListNode listNode = lc0203removeElements.detectCycle(listNode1);
        System.out.println(listNode);


    }
}
