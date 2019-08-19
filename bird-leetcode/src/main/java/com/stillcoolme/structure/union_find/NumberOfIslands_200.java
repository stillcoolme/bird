package com.stillcoolme.structure.union_find;

/**
 * @author: stillcoolme
 * @date: 2019/8/18 13:45
 * @description:
 * 给定一个由 '1'（陆地）和 '0'（水）组成的的二维网格，计算岛屿的数量。
 * 一个岛被水包围，并且它是通过水平方向或垂直方向上相邻的陆地连接而成的。
 * 你可以假设网格的四个边均被水包围。
 **/
public class NumberOfIslands_200 {

    /**
     * 解法一：遍历二维网格，将竖直或水平相邻的陆地联结。
     * 最终，返回并查集数据结构中相连部分的数量。
     * 路径压缩。。
     * @param grid
     * @return
     */
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }
        int nr = grid.length;
        int nc = grid[0].length;
        UnionFind unionFind = new UnionFind(grid);
        for (int r = 0; r < nr; ++r) {
            for (int c = 0; c < nc; ++c) {
                if (grid[r][c] == '1') {
                    grid[r][c] = '0';
                    // 对 右边 和 下边 的两个进行并查就行了，不用对四面八方的。
//                    if(r - 1 >= 0 && grid[r - 1][c] == '1') {
//                        unionFind.union(r * nc + c, (r-1) * nc + c);
//                    }
                    if (r + 1 < nr && grid[r+1][c] == '1') {
                        unionFind.union(r * nc + c, (r+1) * nc + c);
                    }
//                    if (c - 1 >= 0 && grid[r][c-1] == '1') {
//                        unionFind.union(r * nc + c, r * nc + c - 1);
//                    }
                    if (c + 1 < nc && grid[r][c+1] == '1') {
                        unionFind.union(r * nc + c, r * nc + c + 1);
                    }
                }
            }
        }
        return unionFind.getCount();
    }

    class UnionFind {
        int count; // # of connected components
        int[] parent;
        int[] rank;

        public UnionFind(char[][] grid) { // for problem 200
            // 记录有多少个节点
            count = 0;
            int m = grid.length;
            int n = grid[0].length;
            // 记录每个节点的老大是什么
            parent = new int[m * n];
            // 记录并查集的深度，这是一个优化点，让深度较小的并查集 合并 到 深度较大的并查集中
            rank = new int[m * n];
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                    // 初始化，自己的老大就是自己，所以指向自己
                    if(grid[i][j] == '1') {
                        parent[i * n + j] = i * n + j;
                        ++count;
                    }
                    // 初始化时，所有的元素只包含它自己，只有一个元素，所以 rank[i] = 1
                    rank[i * n + j] = 1;
                }
            }
        }

        /**
         * 查找自己指向的老大，一开始是指向自己的，后面不是指向自己了就递归继续查找.
         * 同时指向新的老大，也叫 path compression
         * @param i
         * @return
         */
        public int find(int i) {
            if(parent[i] != i){
                parent[i] =  find(parent[i]);
            }
            return parent[i];
        }

        /**
         * 合并 x 和 y， 如果
         * union with rank
         * @param x
         * @param y
         */
        public void union(int x, int y) {
            int rootx = find(x);
            int rooty = find(y);
            if(rootx != rooty) {
                // 让深度较小的并查集 合并 到 深度较大的并查集中
                if(rank[rootx] > rank[rooty]) {
                    parent[rooty] = rootx;
                }else if(rank[rootx] < rank[rooty]) {
                    parent[rootx] = rooty;
                } else {
                    parent[rooty] = rootx; rank[rootx] += 1;
                }
                count -= 1;
            }
        }

        public int getCount() {
            return count;
        }
    }


    /**
     * 解法二：深度优先搜索，Flood fill 算法就是相当于扫雷
     *
     * @param grid
     * @return
     */
    public int numIslands2(char[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }
        int nr = grid.length;
        int nc = grid[0].length;
        int num_islands = 0;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                if(grid[i][j] == '1') {
                    num_islands += 1;
                    dfs(grid, i, j, nr, nc);
                }
            }
        }
        return num_islands;
    }

    private void dfs(char[][] grid, int i, int j, int nr, int nc) {
        if (i < 0 || j < 0 || i >= nr || j >= nc || grid[i][j] == '0') {
            return;
        }
        grid[i][j] = '0';
        dfs(grid, i - 1, j, nr, nc);
        dfs(grid, i + 1, j, nr, nc);
        dfs(grid, i, j - 1, nr, nc);
        dfs(grid, i, j + 1, nr, nc);
    }


    public static void main(String[] args) {
        NumberOfIslands_200 numberOfIslands_200 = new NumberOfIslands_200();
        char[][] chars = {
                {'1','1','0','0','0'},
                {'1','1','0','0','0'},
                {'0','0','1','0','0'},
                {'0','0','0','1','1'}
        };
        System.out.println(numberOfIslands_200.numIslands(chars));
    }
}

