package com.stillcoolme.structure.tree;

import com.stillcoolme.data.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/7/6 16:42
 * @description:
 **/
public class _144_BTPreorderTraversal {

    public List<Integer> preorderTraversal(TreeNode root) {
        List list = new ArrayList();
        inOrder(root, list);
        return list;
    }

    private void inOrder(TreeNode root, List list) {
        if(root == null) return;
        list.add(root.val);
        inOrder(root.left, list);
        inOrder(root.right, list);
    }
}
