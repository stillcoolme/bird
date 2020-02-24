package com.stillcoolme.designpattern.init.builder;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * @author: stillcoolme
 * @date: 2020/2/24 9:05
 * <p>
 * XX类的 Builder 类 直接一个类，不作为 XX 类的内部类
 * XX类就可以是接口，具体 build 的是 T
 * 调用的时候
 * ThreadFactoryBuilder.newBuilder().set.....
 * <p>
 * 本来以为用的时候还是要`new XXBuilder`啊，本来就是为了不要`new XX()`出现`new`。其实加个静态的 newBuilder() 就解决了
 */
public class ThreadFactoryBuilder<T extends ThreadFactory> {

    // 可以设置的属性
    private Boolean daemon = null;
    private Integer priority = null;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;

    public ThreadFactoryBuilder() {

    }

    public static ThreadFactoryBuilder<ThreadFactory> newBuilder() {
        return new ThreadFactoryBuilder<ThreadFactory>();
    }

    public ThreadFactoryBuilder setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public ThreadFactoryBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public T build() {
        if (check()) {
            throw new RuntimeException("参数不符合要求");
        }
        return doBuild(this);
    }

    // 检查参数是否符合要求
    private boolean check() {
        return true;
    }

    // 真正构造 XX 的，是私有方法！
    private T doBuild(ThreadFactoryBuilder builder) {
        return (T) new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new DefaultThreadFactory("test").newThread(runnable);
                if (builder.daemon != null) {
                    thread.setDaemon(builder.daemon);
                }
                if (builder.priority != null) {
                    thread.setPriority(builder.priority);
                }
                return thread;
            }
        };
    }
}
