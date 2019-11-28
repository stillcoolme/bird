package com.stillcoolme.framework.self.threadpool;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: stillcoolme
 * @date: 2019/11/17 17:29
 * @description: 用于存放 线程池中运行的线程，需要线程安全。
 * 因为在 j.u.c 源码中是一个 HashSet 实现的，所以对它所有的操作都是需要加锁。
 *
 */
public class ConcurrentHashSet<T> extends AbstractSet<T> {

    private ConcurrentHashMap<T, Object> map = new ConcurrentHashMap<>();
    private final Object PRESIST = new Object();

    // 由于 ConcurrentHashMap 的 size() 函数并不准确，所以我这里单独利用了一个 AtomicInteger 来统计容器大小
    private AtomicInteger count = new AtomicInteger();

    @Override
    public Iterator iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean add(T t) {
        count.incrementAndGet();
        return map.put(t, PRESIST) == null;
    }

    @Override
    public boolean remove(Object o) {
        count.decrementAndGet();
        return map.remove(o) == PRESIST;
    }

    @Override
    public int size() {
        return count.get();
    }
}
