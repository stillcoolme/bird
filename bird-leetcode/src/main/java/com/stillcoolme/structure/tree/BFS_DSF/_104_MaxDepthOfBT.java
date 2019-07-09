package com.stillcoolme.structure.tree.BFS_DSF;

import com.stillcoolme.data.TreeNode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author: stillcoolme
 * @date: 2019/7/7 17:25
 * @description:
 *  求二叉树的最大深度
 **/
public class _104_MaxDepthOfBT {

    /**
     * 广度优先遍历 BFS
     *
     * @param root
     * @return
     */
    public int maxDepth(TreeNode root) {
        if(root == null) return 0;
        Deque<TreeNode> deque = new LinkedList<>();
        int level = 1;
        deque.offer(root);
        boolean flag = false;
        while (!deque.isEmpty()){
            // 将当前层的节点数拿出来！！ 这句很关键
            int currentLevelCount = deque.size();
            for (int i = 0; i < currentLevelCount; i++) {
                // 出队
                TreeNode node = deque.pollFirst();
                if(node.left != null){
                    deque.offer(node.left);
                    flag = true;
                }
                if(node.right != null){
                    deque.offer(node.right);
                    flag = true;
                }
            }
            if(flag){
                flag = false;
                level ++;
            }
        }
        return level;
    }

    /**
     * 深度优先遍历 DFS
     * @param root
     * @return
     */
    public int maxDepth2(TreeNode root) {
        return root == null ? 0 : 1 + Math.max(maxDepth2(root.left), maxDepth2(root.right));
    }

/*
    唉，我居然还去弄这hepler，要77ms。。。本身就可以递归了啊，不用额外参数。
    private int helper(TreeNode node, int level) {
        // 如果是叶子节点，直接返回层数
        if(node.left == null && node.right == null){
            return level;
        }
        int left = 0;
        int right = 0;
        if(node.left != null) {
            // 用 level ++ 居然没加到
            left = helper(node.left, level + 1);
        }
        if(node.right != null) {
            right = helper(node.right, level + 1);
        }
        return left > right ? left : right;
    }*/


    public static void main(String[] args) {
        _104_MaxDepthOfBT btLevelOrderTraversal = new _104_MaxDepthOfBT();
        int depth = btLevelOrderTraversal.maxDepth2(TreeNode.createTestData("[1,2,3,4]"));
        System.out.println(depth);
    }

}
