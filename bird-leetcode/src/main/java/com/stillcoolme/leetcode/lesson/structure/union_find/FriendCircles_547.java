package com.stillcoolme.leetcode.lesson.structure.union_find;

/**
 * @author: stillcoolme
 * @date: 2019/8/18 16:50
 * @description:
 * 给定一个 N * N 的矩阵 M，表示班级中学生之间的朋友关系。
 * 如果M[i][j] = 1，表示已知第 i 个和 j 个学生互为朋友关系，否则为不知道。
 * 你必须输出所有学生中的已知的朋友圈总数。
 * 转换成第200题的并查集问题。。
 **/
public class FriendCircles_547 {

    /**
     * 并查集
     * @param M
     * @return
     */
    public int findCircleNum(int[][] M) {
        if(M == null || M.length == 0) {
            return 0;
        }
        int length = M.length;  //二维数组长度，即所有人的个数，肯定是length * length的
        int circleNum = 0;  //统计朋友圈个数
        UnionFind unionFind = new UnionFind(length);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < i; j++) {
                if (M[i][j] == 1) {
                    unionFind.union(i, j);
                }
            }
        }
        return unionFind.getCount();

    }

    class UnionFind{
        // 连通分量的个数
        private int count;
        private int[] parent;
        //以索引为 i 的元素为根结点的树的深度（最深的那个深度）
        private int[] rank;

        public UnionFind(int length){
            this.count = length;
            parent = new int[length];
            rank = new int[length];
            for (int i = 0; i < length; i++) {
                parent[i] = i;
                rank[i] = 1;
            }
        }
        public int getCount() {
            return this.count;
        }

        public int find(int p) {
            if(parent[p] != p) {
                parent[p] = find(parent[p]);
            }
            return parent[p];
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if(rootX != rootY) {
                if (rank[rootX] > rank[rootY]) {
                    parent[rootX] = rootY;
                } else if (rank[rootX] < rank[rootY]) {
                    parent[rootY] = rootX;
                } else {
                    parent[rootY] = rootX;
                    rank[rootX] += 1;
                }
                // 每次 union 以后，连通分量减 1
                count--;
            }
        }
    }

    /**
     * dfs
     * @param M
     * @return
     */
    public int findCircleNum2(int[][] M) {
        if(M == null || M.length == 0) {
            return 0;
        }
        int length = M.length;  //二维数组长度，即所有人的个数，肯定是length * length的
        int circleNum = 0;  //统计朋友圈个数
        boolean[] flag = new boolean[length];//访问标志
        for (int i = 0; i < length; i++) {  //对于每个人 A -> B 与 B -> A 是同一个关系！所以只要一个循环
            if(flag[i] == false) {
                dfs(M, flag, i);
                circleNum ++;
            }
        }
        return circleNum;
    }

    private void dfs(int[][] m, boolean[] flag, int i) {
        flag[i] = true;
        for (int j = 0; j < m.length; j++) {
            if(!flag[j] && m[i][j] == 1) {
                dfs(m, flag, j);
            }
        }
    }


    public static void main(String[] args) {
        int[][] M = {{1,0,0,1},
                     {0,1,1,0},
                     {0,1,1,1},
                     {1,0,1,1}};
        FriendCircles_547 solution = new FriendCircles_547();
        int res = solution.findCircleNum(M);
        System.out.println(res);
    }

}
