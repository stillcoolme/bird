package com.stillcoolme.leetcode.lesson.structure.tree;

import com.stillcoolme.leetcode.data.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/7/6 15:33
 * @description:
 *  二叉树中序遍历
 **/
public class _94_BTInorderTraversal {

    public List<Integer> inorderTraversal(TreeNode root) {
        List list = new ArrayList();
        inOrder(root, list);
        return list;
    }

    private void inOrder(TreeNode root, List list) {
        if(root == null) return;
        inOrder(root.left, list);
        list.add(root.val);
        inOrder(root.right, list);
    }

    public static void main(String[] args) {
        _94_BTInorderTraversal binaryTreeInorderTraversal = new _94_BTInorderTraversal();
        List<Integer> list = binaryTreeInorderTraversal.inorderTraversal(TreeNode.createTestData("[1,2,3]"));
        for (Integer integer : list) {
            System.out.println(integer + " ");
        }
    }
}
