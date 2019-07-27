package com.stillcoolme.algorithm.dp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/7/27 16:12
 * @description:
 **/
public class _120_Triangle {

    /**
     * DP: 从最底层往前面推
     * @param triangle
     * @return
     */
    public int minimumTotal(List<List<Integer>> triangle) {
        // 先得到triangleSize节省了150ms。。
        int triangleSize = triangle.size();

        int[][] mem = new int[triangleSize][triangle.get(triangleSize - 1).size()];
        for (int i = triangleSize - 1; i >= 0; i --) {
            for (int j = triangle.get(i).size() - 1; j >= 0; j--) {
                if(i == triangleSize - 1) {
                    mem[i][j] = triangle.get(i).get(j);
                } else {
                    // 状态转移方程
                    mem[i][j] = Math.min(mem[i + 1][j], mem[i + 1][j + 1]) + triangle.get(i).get(j);
                }
            }
        }
        return mem[0][0];
    }

    public static void main(String[] args) {
        _120_Triangle triangle = new _120_Triangle();
        /**
         * [
         *      [2],
         *     [3,4],
         *    [6,5,7],
         *   [4,1,8,3]
         * ]
         * The minimum path sum from top to bottom is 11 (i.e., 2 + 3 + 5 + 1 = 11).
         */
        List list1 = new ArrayList();
        list1.add(2);
        List list2 = new ArrayList();
        list2.add(3);list2.add(4);
        List list3 = new ArrayList();
        list3.add(6);list3.add(5);list3.add(7);
        List list4 = new ArrayList();
        list4.add(4);list4.add(1);list4.add(8);list4.add(3);
        List allList = new ArrayList();
        allList.add(list1);allList.add(list2);allList.add(list3);allList.add(list4);
        System.out.println(triangle.minimumTotal(allList));
    }

}
