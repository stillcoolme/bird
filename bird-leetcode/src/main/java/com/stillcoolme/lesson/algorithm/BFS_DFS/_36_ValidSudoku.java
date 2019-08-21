package com.stillcoolme.lesson.algorithm.BFS_DFS;

import java.util.HashMap;

/**
 * Author: stillcoolme
 * Date: 2019/7/18 9:31
 * Description:
 * 验证给定的 9x9 数独格是有效的。
 * 需要符合以下规则：
 * the following rules:
 * 1. Each row must contain the digits 1-9 without repetition.
 * 2. Each column must contain the digits 1-9 without repetition.
 * 3. Each of the 3x3 sub-boxes of the grid must contain the digits 1-9 without repetition.
 */
public class _36_ValidSudoku {

    /**
     * 使用Map数组来记录出现次数，value -> count
     *
     * @param board
     * @return
     */
    public boolean isValidSudoku(char[][] board) {
        // init data
        HashMap<Integer, Integer> [] rows = new HashMap[9];
        HashMap<Integer, Integer> [] columns = new HashMap[9];
        HashMap<Integer, Integer> [] boxes = new HashMap[9];
        for (int i = 0; i < 9; i++) {
            rows[i] = new HashMap<Integer, Integer>();
            columns[i] = new HashMap<Integer, Integer>();
            boxes[i] = new HashMap<Integer, Integer>();
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char num = board[i][j];
                if(num != '.'){
                    int n = num;
                    // 3 * 3 的子数独棋盘索引 0 - 8
                    int boxesIndex = i / 3 * 3 + j / 3;
                    // 记录出现次数
                    rows[i].put(n, rows[i].getOrDefault(n, 0) + 1);
                    columns[j].put(n, columns[j].getOrDefault(n, 0) + 1);
                    boxes[boxesIndex].put(n, boxes[boxesIndex].getOrDefault(n, 0) + 1);

                    if(rows[i].get(n) > 1 || columns[j].get(n) > 1 || boxes[boxesIndex].get(n) > 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {

    }
}
