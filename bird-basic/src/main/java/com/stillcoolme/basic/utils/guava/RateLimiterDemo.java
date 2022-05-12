package com.stillcoolme.basic.utils.guava;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.TimeUnit;

/**
 * 如何面对高并发大流量控制？
 * 限流的常用处理手段有：计数器、滑动窗口、漏桶、令牌桶
 */
public class RateLimiterDemo {

    public static void main(String[] args) {

        // 单机的限流，创建一个基于令牌桶算法的限流器，参数是QPS大小
        // RateLimiter将以这个速度往桶里面放入令牌，然后请求的时候，通过tryAcquire()方法向RateLimiter获取许可（令牌）
        RateLimiter rateLimiter = RateLimiter.create(100);

        for (int i = 0; i < 5000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (rateLimiter.tryAcquire(10, TimeUnit.SECONDS)) {
                        System.out.println("执行业务逻辑");
                    } else {
                        System.out.println("限流");
                    }
                }
            }).start();
        }

    }
}
