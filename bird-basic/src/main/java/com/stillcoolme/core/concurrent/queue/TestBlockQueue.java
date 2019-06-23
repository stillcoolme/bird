package com.stillcoolme.core.concurrent.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class TestBlockQueue {
    public static void main(String[] args) {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);

        ReentrantLock lock = new ReentrantLock();
        lock.lock();
    }
}
