package com.stillcoolme.lesson.structure.tree.BFS_DSF;

import com.stillcoolme.data.TreeNode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author: stillcoolme
 * @date: 2019/7/7 17:49
 * @description:
 *  求二叉树的最大深度
 **/
public class _111_MinDepthOfBT {

    /**
     * 广度优先遍历 BFS
     * 判断是不是叶子节点
     * @param root
     * @return
     */
    public int minDepth(TreeNode root) {
        if(root == null) return 0;
        Deque<TreeNode> deque = new LinkedList<>();
        int level = 1;
        deque.offer(root);
        boolean flag = false;
        while (!deque.isEmpty()){
            // 这句很关键
            int currentLevelSize = deque.size();
            for (int i = 0; i < currentLevelSize; i++) {
                TreeNode node = deque.pollFirst();
                if(node.left == null && node.right == null) {
                    return level;
                }
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
    public int minDepth2(TreeNode root) {
        if(root == null) return 0;
        if(root.left == null) return 1 + minDepth2(root.right);
        if(root.right == null) return 1 + minDepth2(root.left);

        int leftMinDepth = minDepth2(root.left);
        int rightMinDepth = minDepth2(root.right);

        return 1 + Math.min(leftMinDepth, rightMinDepth);
    }
    // 奇怪，怎么我做就会得到0？，我没加1？

    public static void main(String[] args) {
        _111_MinDepthOfBT minDepthOfBt = new _111_MinDepthOfBT();
        int depth = minDepthOfBt.minDepth2(TreeNode.createTestData("[1,2,3,4]"));
        System.out.println(depth);
    }
}
