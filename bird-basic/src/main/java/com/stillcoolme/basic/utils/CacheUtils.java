package com.stillcoolme.basic.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-12-27 17:11:00
 * @Description
 */
public class CacheUtils {

    public static void main(String[] args) throws InterruptedException {

        Cache cache = CacheBuilder.newBuilder()
                .maximumSize(10)    // 缓存大小
                //.expireAfterAccess(10, TimeUnit.SECONDS)    // 10s 不访问则 Cache 中的所有数据就会过期
                .expireAfterWrite(3, TimeUnit.SECONDS)     // 10s 不写入或者不修改这个 key 对应的 value，那么这一对 kv 数据就会被删除
                .build();

        cache.put("key", "bobo");
        System.out.println(cache.getIfPresent("key"));
        Thread.sleep(4000);
        System.out.println(cache.getIfPresent("key"));


    }
}
