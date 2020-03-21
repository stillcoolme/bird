package com.stillcoolme.leetcode.lesson.algorithm.lru;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2019/8/26 18:59
 * @description:
 *  双向链表 + Map 结合实现 LRUCahce
 *
 *  使用双向链表的一个好处是不需要额外信息删除一个节点，同时可以在常数时间内从头部或尾部插入删除节点。
 **/
public class LRUCache_146 {

    class DLinkedNode {
        public int key;
        public int value;
        public DLinkedNode pre;
        public DLinkedNode next;

        public DLinkedNode(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private void addNode(DLinkedNode node) {
        // 双向链表添加需要操作四个点
        // 将节点更新到 链表最前面
        node.pre = head;
        node.next = head.next;
        head.next.pre = node;
        head.next = node;
    }

    private void removeNode(DLinkedNode node) {
        // 将一个存在的节点删除
        node.pre.next = node.next;
        node.next.pre = node.pre;
    }

    private DLinkedNode popTail() {
        DLinkedNode node = tail.pre;
        removeNode(node);
        return node;
    }

    private void moveToHead(DLinkedNode node) {
        removeNode(node);
        addNode(node);
    }

    // 以上是双向链表相关数据结构及方法
    // ----------------------------

    Map<Integer, DLinkedNode> cache = new Hashtable<>();
    private int size;
    private int capacity;
    DLinkedNode head;
    DLinkedNode tail;

    public LRUCache_146(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        // 一个伪头部和伪尾部标记界限，这样在更新的时候就不需要检查是否是 null 节点
        this.head = new DLinkedNode(0, 0);
        this.tail = new DLinkedNode(0, 0);
        head.next = tail;
        tail.pre = head;
    }


    public int get(int key) {
        // 从 hashtable中获取数据
        DLinkedNode node = cache.get(key);
        if(node == null) return -1;
        // 将使用过的节点更新到链表最前面
        moveToHead(node);
        return node.value;
    }

    public void put(int key, int value) {
        DLinkedNode node = cache.get(key);
        if(node == null) {
            node = new DLinkedNode(key, value);
            // 将数据放到hashtable中
            cache.put(key, node);
            // 将数据放到双向链表中，数据更新到 链表最前面
            addNode(node);
            size += 1;
            if(size > capacity) {
                DLinkedNode tail = popTail();
                cache.remove(tail.key);
            }
        } else {
            node.value = value;
            moveToHead(node);
        }
    }

    public static void main(String[] args) {
        LRUCache_146 cache = new LRUCache_146(2);
        cache.put(1, 1);
        cache.put(2, 2);
        System.out.println(cache.get(1));       // 返回  1
        cache.put(3, 3);    // 该操作会使得密钥 2 作废
        System.out.println(cache.get(2));       // 返回 -1 (未找到)
        cache.put(4, 4);    // 该操作会使得密钥 1 作废
        System.out.println(cache.get(1));       // 返回 -1 (未找到)
        System.out.println(cache.get(3));       // 返回  3
        System.out.println(cache.get(4));       // 返回  4
    }
}
