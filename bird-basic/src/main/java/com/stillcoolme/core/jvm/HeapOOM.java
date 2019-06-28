package com.stillcoolme.core.jvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 测试内存占用导致的OOM
 * 启动参数如下：
 *  -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=D://log
 * 为了更快的突出内存问题将堆的最大内存固定在 20M，
 * 同时在 JVM 出现 OOM 的时候自动 dump 内存到 D://log (不配路径则会生成在当前目录)。
 * 将dump文件上传到 https://heaphero.io/index.jsp
 */
public class HeapOOM {

    public static void main(String[] args) {
        List list = new ArrayList(10);
        while (true){
            list.add(1);
        }

    }
}
