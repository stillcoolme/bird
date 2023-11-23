package com.stillcoolme.leetcode.structure.tree.shenDu_guangDu_youXian;

import com.stillcoolme.leetcode.data.TreeNode;

/**
 * <p>给你一棵二叉树的根节点 root ，请你返回 层数最深的叶子节点的和 。 </p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/10/24 18:56
 */
public class _1302_deepestLeavesSum {

    int sum = 0;
    // 层级
    int max_k = 0;

    public int deepestLeavesSum(TreeNode root) {
        bianLi(root, 0);
        return sum;
    }

    public void bianLi(TreeNode root, int k) {
        if (root == null) return;

        if (k == max_k) {   // 达到最大深度，将 节点值加起来
            sum += root.val;
        } else if (k > max_k) {  // 还没达到最大深度， 更新最大深度、总和
            max_k = k;
            sum = root.val;
        }
        bianLi(root.left, k + 1);
        bianLi(root.right, k + 1);

    }

    public static void main(String[] args) {
        _1302_deepestLeavesSum deepestLeavesSum = new _1302_deepestLeavesSum();
        TreeNode root = TreeNode.createTestData("[1,2,3]");
        int i = deepestLeavesSum.deepestLeavesSum(root);
        System.out.println("sum: " + i);
    }
}
