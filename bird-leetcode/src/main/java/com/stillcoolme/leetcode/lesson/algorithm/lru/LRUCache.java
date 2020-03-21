package com.stillcoolme.leetcode.lesson.algorithm.lru;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2020/3/4 15:14
 * Function: 简单用LinkedHashMap来实现的LRU算法的缓存
 *  LinkedHashMap 是能够记录数据写入顺序的 Map
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private int cacheSize;

    public LRUCache(int cacheSize) {
        super(16, (float) 0.75, true);
        this.cacheSize = cacheSize;
    }

    /**
     * 覆盖父类方法，父方法默认返回 false，返回 true 则允许删除最老的数据
     * @param eldest
     * @return
     */
    @Override
    public boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cacheSize;
    }

    public static void main(String[] args) {
        LRUCache<String, Integer> lruCache = new LRUCache<>(3);
        for (int i = 0; i < 3; i++) {
            lruCache.put("num" + i, i);
        }
        System.out.println(lruCache);

        lruCache.put("num" + 4, 4);

        System.out.println(lruCache);
    }
}
