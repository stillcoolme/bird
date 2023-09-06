package com.stillcoolme.leetcode.all;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * <p>一开始用的两个map key value 都是 int，没有用 TreeSet 导致逻辑复杂，而且出错不好排查，
 *  而且如果 用 priorityQueue 还超时，还是用 TreeSet 吧 </p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/5/20 17:30
 */
public class lc2349NumberContainers {

    HashMap<Integer, Integer> container = new HashMap<>();  //  index: number
    private Map<Integer, TreeSet<Integer>> valIndexMap = new HashMap<>();  // number: TreeSet<index, index>

    public lc2349NumberContainers() {
    }

    public void change(int index, int number) {
        if (container.containsKey(index)) {
            Integer value = container.get(index);
            valIndexMap.remove(value);
        }

        container.put(index, number);

    }

    public int find(int number) {
        TreeSet<Integer> queue = valIndexMap.get(number);
        if (queue == null || queue.isEmpty()) {
            return -1;
        }
        return queue.first();
    }


    public static void main(String[] args) {
        lc2349NumberContainers nc = new lc2349NumberContainers();
         // 没有数字 10 ，所以返回 -1 。
        nc.change(1, 10); // 容器中下标为 1 处填入数字 10 。
        nc.change(1, 20); // 容器中下标为 1 处填入数字 20 。注意，下标 1 处之前为 10 ，现在被替换为 20 。
        nc.find(10);
        nc.find(20); // 数字 10 所在的下标为 1 ，2 ，3 和 5 。因为最小下标为 1 ，所以返回 1 。
        int xx = nc.find(20); // 数字 10 所在下标为 2 ，3 和 5 。最小下标为 2 ，所以返回 2
        System.out.println(xx);
    }
}