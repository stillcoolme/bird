package com.stillcoolme.lesson.structure.tree;

import com.stillcoolme.data.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/7/6 16:45
 * @description:
 **/
public class _145_BTPostorderTraversal {

    public List<Integer> postorderTraversal(TreeNode root) {
        List list = new ArrayList();
        inOrder(root, list);
        return list;
    }

    private void inOrder(TreeNode root, List list) {
        if(root == null) return;
        inOrder(root.left, list);
        inOrder(root.right, list);
        list.add(root.val);
    }
}
