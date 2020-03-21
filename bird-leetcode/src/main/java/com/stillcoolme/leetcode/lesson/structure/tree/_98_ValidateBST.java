package com.stillcoolme.leetcode.lesson.structure.tree;

import com.stillcoolme.leetcode.data.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/7/6 14:49
 * @description:
 * 验证二叉树是否是二叉排序树
 **/
public class _98_ValidateBST {

    /**
     * 解法一：对二叉排序树进行中序遍历（左孩子，父，右孩子），得到的是升序数组，说明是二叉排序树
     * 只需要记住前继节点，然后将当前节点和前继节点大小
     *
     * 通过参数传递list！而不是通过返回值！！
     *
     * @param root
     * @return
     */
    List<Integer> list = new ArrayList();
    public boolean isValidBST(TreeNode root) {
        inOrder(root, list);
        for (int i = 0; i < list.size() - 1; i++) {
            // 是 >=
            if(list.get(i) >= list.get(i + 1)){
                return false;
            }
        }
        return true;
    }

    private void inOrder(TreeNode node, List list) {
        if(node == null){
            return;
        }
        inOrder(node.left, list);
        list.add(node.val);
        inOrder(node.right, list);
    }


    /**
     * 解法二：使用递归
     * @param root
     * @return
     */
    // TODO
    public boolean isValidBST2(TreeNode root) {
        return isValidBST2Impl(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     *
     * @param root
     * @param min 树的下界
     * @param max 树的上界
     * @return
     */
    private boolean isValidBST2Impl(TreeNode root, Long min, Long max) {
        if(root == null){
            return true;
        }
        if(min != null && root.val <= min) {
            return false;
        }
        if(max != null && root.val >= max) return false;
        return isValidBST2Impl(root.left, min, (long) root.val) && isValidBST2Impl(root.right, (long) root.val, max);

    }


    public static void main(String[] args) {
        _98_ValidateBST validateBST = new _98_ValidateBST();
        boolean result = validateBST.isValidBST2(TreeNode.createTestData("[2,1,3]"));
        System.out.println(result);
    }
}
