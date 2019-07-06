package com.stillcoolme.structure.tree;

import com.stillcoolme.data.TreeNode;

/**
 * @author: stillcoolme
 * @date: 2019/7/6 16:52
 * @description:
 *  求二叉搜索树的最近的公共祖先
 **/
public class _235_LowestCommonAncestorOfBST {

    /**
     *  lowestCommonAncestor(root.left, p, q)  找 p 或 找 q
     *  找得到 p 和 q 就返回 root，否则返回 p 或 q
     * @param root
     * @param p
     * @param q
     * @return
     */
    // TODO
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null) return null;
        if(root == p || root == q) return root;
        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);
        // 找得到 left 和 right 就返回 root，否则返回 left 或 right， 牛逼！！！
        return left == null ? right : right == null ? left : root;
    }

}
