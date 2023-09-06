package com.stillcoolme.leetcode.algorithm.divideconquer;

import java.util.*;

/**
 * @author: stillcoolme
 * @date: 2019/7/10 19:15
 * @description:
 * 在 N * N 的棋盘放置N个棋子
 * 任意两个棋子都不能处于同一行、同一列或同一斜线上，问有多少种摆法。
 **/
public class _51_NQueens {

    /**
     * 在建立算法之前，我们来考虑两个有用的细节。
     *  1. 一行只可能有一个皇后且一列也只可能有一个皇后。 这意味着没有必要再棋盘上考虑所有的方格。只需要按列循环即可。
     *  2. 对于所有的撇对角线有 行号 + 列号 = 同一常数，对于所有的捺对角线有 行号 - 列号 = 同一常数。 这可以让我们标记已经在攻击范围下的对角线并且检查一个方格 (行号, 列号) 是否处在攻击位置。
     *
     * @param n
     * @return
     */
    //TODO
    List<List<String>> list = new ArrayList();
    static Set column = new HashSet<Integer>();
    static Set pie = new HashSet<Integer>();
    static Set na = new HashSet<Integer>();
    public List<List<String>> solveNQueens(int n) {
        if (n < 1) {
            return list;
        }
        dfs(n, 0, new ArrayList());
        return list;
    }

    private void dfs(int n, int row, List<String> sublist) {
        // 到最后一层了，终止递归
        if(row >= n){
            list.add(new ArrayList<>(sublist));
            return;
        }
        // 对于每一层，遍历每一列
        // 先看这一列是不是已经在 之前皇后占过的位置和皇后的攻击范围内
        for (int col = 0; col < n; col++) {
            if(column.contains(col) || pie.contains(col + row) || na.contains(row - col)) {
                continue;
            }
            // 在该列放置皇后。将皇后波及的返回放到三个set当中
            column.add(col);
            pie.add(col + row);
            na.add(row - col);

            // 构造 皇后的放置位置
            char[] CC = new char[n];
            Arrays.fill(CC, '.');
            CC[col] = 'Q';
            sublist.add(new String(CC));

            // 继续递归
            dfs(n, row + 1, sublist);

            // 去掉这次放的皇后，相当于回溯到上一步，看走其他地方可不可以
            // 如果 column pie na是放在方法的参数上的就不用清除？因为局部变量会重新复制一份
            sublist.remove(sublist.size() - 1);
            column.remove(col);
            pie.remove(col + row);
            na.remove(row - col);
        }

    }

    public static void main(String[] args) {
        _51_NQueens nQueens = new _51_NQueens();
        List<List<String>> list = nQueens.solveNQueens(8);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                System.out.println(list.get(i).get(j));
            }
            System.out.println("================");
        }
    }

}
