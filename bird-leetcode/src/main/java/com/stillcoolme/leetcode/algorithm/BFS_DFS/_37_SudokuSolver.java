package com.stillcoolme.leetcode.algorithm.BFS_DFS;

/**
 * @author: stillcoolme
 * @date: 2019/7/27 9:44
 * @description:
 *  解决数独
 **/
public class _37_SudokuSolver {

    /**
     * 回溯法
     * 1. 解数独思路：
     *  类似人的思考方式去尝试，行，列，还有 3*3 的方格内数字是 1~9 不能重复。
     *  不断尝试填充，如果发现重复了，那么擦除重新进行新一轮的尝试，直到把整个数组填充完成。
     *
     * 2. 算法步骤:
     *  1）声明布尔数组，表明行列中某个数字是否被使用了， 被用过视为 true，没用过为 false。
     *  2）初始化布尔数组，表明哪些数字已经被使用过了。
     *  3）尝试去填充数组，只要行，列， 还有 3*3 的方格内 出现已经被使用过的数字，我们就不填充，否则尝试填充。
     *  4）如果填充失败，那么我们需要回溯。将原来尝试填充的地方改回来。
     *  5）递归直到数独被填充完成。
     * 3. 代码如下：
     *  代码看着多， 其实逻辑非常清楚，很容易理解。
     *
     * @param board
     */
    public void solveSudoku(char[][] board) {
        // 三个布尔数组 表明 行, 列, 还有 3*3 的方格的数字是否被使用过
        boolean[][] rowUsed = new boolean[9][10];
        boolean[][] colUsed = new boolean[9][10];
        boolean[][][] boxUsed = new boolean[3][3][10];
        // 初始化
        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[0].length; col++) {
                int num = board[row][col] - '0';
                if(1 <= num && num <= 9){
                    rowUsed[row][num] = true;
                    colUsed[col][num] = true;
                    boxUsed[row/3][col/3][num] = true;
                }
            }
        }
        // 递归尝试填充数组
        recusiveSolveSudoku(board, rowUsed, colUsed, boxUsed, 0, 0);

        for(int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                System.out.print(board[row][col] + " ");
            }
            System.out.println("");
        }
    }

    private boolean recusiveSolveSudoku(char[][] board, boolean[][] rowUsed, boolean[][] colUsed, boolean[][][] boxUsed, int row, int col) {
        // 边界校验, 如果已经填充完成, 返回true, 表示一切结束
        if(col == board[row].length) {
            col = 0;
            row ++;
            if(row == board.length) {
                return true;
            }
        }
        // 是空则尝试填充, 否则跳过继续尝试填充下一个位置
        if(board[row][col] == '.') {
            // 尝试填充1~9
            for(int num = 1; num <= 9; num++){
                // 判断该空格能否填这数字
                boolean neverUsed = ! (rowUsed[row][num] || colUsed[col][num] || boxUsed[row / 3][col / 3][num]);
                if(neverUsed) {
                    rowUsed[row][num] = true;
                    colUsed[col][num] = true;
                    boxUsed[row / 3][col / 3][num] = true;
                    board[row][col] = (char) ('0' + num);
                    if(recusiveSolveSudoku(board, rowUsed, colUsed, boxUsed, row, col + 1)) {
                        return true;
                    }
                    // 发现不能填任何数字了，回溯
                    board[row][col] = '.';
                    rowUsed[row][num] = false;
                    colUsed[col][num] = false;
                    boxUsed[row/3][col/3][num] = false;
                }
            }
        } else {
            return recusiveSolveSudoku(board, rowUsed, colUsed, boxUsed, row, col + 1);
        }
        return false;
    }

    public static void main(String[] args) {
        _37_SudokuSolver sudokuSolver = new _37_SudokuSolver();
        char[][] board = {
                {'5', '3', '.', '.', '7', '.', '.', '.', '.'},
                {'6', '.', '.', '1', '9', '5', '.', '.', '.'},
                {'.', '9', '8', '.', '.', '.', '.', '6', '.'},
                {'8', '.', '.', '.', '6', '.', '.', '.', '3'},
                {'4', '.', '.', '8', '.', '3', '.', '.', '1'},
                {'7', '.', '.', '.', '2', '.', '.', '.', '6'},
                {'.', '6', '.', '.', '.', '.', '2', '8', '.'},
                {'.', '.', '.', '4', '1', '9', '.', '.', '5'},
                {'.', '.', '.', '.', '8', '.', '.', '7', '9'},
        };
        sudokuSolver.solveSudoku(board);
    }
}
