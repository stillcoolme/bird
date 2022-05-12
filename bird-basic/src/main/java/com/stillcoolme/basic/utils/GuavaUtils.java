package com.stillcoolme.basic.utils;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * @author: stillcoolme
 * @date: 2020/4/19 15:04
 * Function:
 * 集合
 * 缓存
 * 异步
 * 限流
 */
public class GuavaUtils {

    static HashFunction hf = Hashing.md5();
    public static int murmurHash(String key) {
        // Hasher实例实例不能复用，不然相同key会得到不同的hash值
        int hc = hf.newHasher()
                // .putLong(key)
                .putString(key, Charsets.UTF_8)
                // .putObject(person, personFunnel)
                .hash()
                .asInt();
        return hc;
    }

}
