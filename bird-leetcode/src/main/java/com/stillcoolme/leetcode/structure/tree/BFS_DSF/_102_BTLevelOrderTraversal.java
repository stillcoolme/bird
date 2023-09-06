package com.stillcoolme.leetcode.structure.tree.BFS_DSF;

import com.stillcoolme.leetcode.data.TreeNode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/7/6 17:57
 * @description:
 * 二叉树的层次遍历
 **/
public class _102_BTLevelOrderTraversal {

    /**
     * 通过广度优先搜索BFS来做
     * 使用deque，每次存储当前层的各节点
     * @param root
     * @return
     */
    public List<List<Integer>> levelOrder(TreeNode root) {
        Deque<TreeNode> deque = new LinkedList<>();
        List<List<Integer>> levels = new ArrayList<List<Integer>>();
        if(root == null) return levels;
        deque.offer(root);

        while (!deque.isEmpty()){
            int currentLevel = deque.size();
            List<Integer> level = new ArrayList<>();
            for (int i = 0; i < currentLevel; i++) {
                TreeNode treeNode = deque.pollFirst();
                level.add(treeNode.val);
                if(treeNode.left != null) deque.offer(treeNode.left);
                if(treeNode.right != null) deque.offer(treeNode.right);
            }
            levels.add(level);
        }
        return levels;
    }


    /**
     * 通过深度搜索DFS来做
     * 本质就是前序遍历啊
     * DFS理论上会比BFS慢一些，因为存在函数调用与调用栈创建等开销。此外，如果树的层级过高，会有“爆栈”的风险。时间复杂度O(n)，空间复杂度O(n)。
     * @param root
     * @return
     */
    // TODO
    List<List<Integer>> levels = new ArrayList<List<Integer>>();
    public List<List<Integer>> levelOrder2(TreeNode root) {
        if(root == null) return levels;
        hepler(root, 0);
        return levels;
    }

    private void hepler(TreeNode root, int i) {
        if(levels.size() == i){
            levels.add(new ArrayList<>());
        }
        levels.get(i).add(root.val);
        if(root.left != null) hepler(root.left, i + 1);
        if(root.right != null) hepler(root.right, i + 1);
    }

    public static void main(String[] args) {
        _102_BTLevelOrderTraversal btLevelOrderTraversal = new _102_BTLevelOrderTraversal();
        btLevelOrderTraversal.levelOrder(TreeNode.createTestData("[1,2,3]"));
    }


}
