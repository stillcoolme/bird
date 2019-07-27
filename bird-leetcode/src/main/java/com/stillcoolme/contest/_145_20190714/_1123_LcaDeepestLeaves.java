package com.stillcoolme.contest._145_20190714;

import com.stillcoolme.data.TreeNode;

/**
 * @author: stillcoolme
 * @date: 2019/7/27 12:20
 * @description:
 * 最深叶节点的最近公共祖先：
 *    给你一个有根节点的二叉树，找到它最深的叶节点的最近公共祖先
 **/
public class _1123_LcaDeepestLeaves {

    /**
     * DFS
     * @param root
     * @return
     */
    public TreeNode lcaDeepestLeaves(TreeNode root) {
        if(root == null) return null;
        else {
            int left = getHeight(root.left);
            int right = getHeight(root.right);
            if(left > right) {
                return lcaDeepestLeaves(root.left);
            } else if(left < right) {
                return lcaDeepestLeaves(root.right);
            }
            return root;
        }
    }

    private int getHeight(TreeNode node) {
        if(node == null) {
            return 0;
        } else {
            return Math.max(getHeight(node.left), getHeight(node.right)) + 1;
        }
    }

    public static void main(String[] args) {
        _1123_LcaDeepestLeaves lcaDeepestLeaves = new _1123_LcaDeepestLeaves();
        TreeNode treeNode = lcaDeepestLeaves.lcaDeepestLeaves(TreeNode.createTestData("[1,2,3,4]"));
        // 返回4，就是它自己
        System.out.println(treeNode.val);
    }
}
