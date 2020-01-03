## 写某个XX类的 Builder 类

这样外部应用使用的是`XXBuilder.setFiled().setFiled().build()`

具体实现就是包含在`XXBuilder`里面不用暴露给外部。

```java
// 下面示例 XX 就是 ThreadFactory
class ThreadFactoryBuilder {
    // 可以设置的属性
    private Boolean daemon = null;
    private Integer priority = null;
    private UncaughtExceptionHandler uncaughtExceptionHandler = null;
    
    // 上面各种属性的set方法，返回值是 ThreadFactoryBuilder， 外部应用就可以实现链式变成
    public ThreadFactoryBuilder setNameFormat(String nameFormat) {
        String unused = format(nameFormat, 0); // fail fast if the format is bad or null
        this.nameFormat = nameFormat;
        return this;
    }

    public ThreadFactoryBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactory build() {
        return doBuild(this);
    }   
    
    // 真正构造 XX
    private static ThreadFactory doBuild(ThreadFactoryBuilder builder) {
        return new ThreadFactory() {
          @Override
          public Thread newThread(Runnable runnable) {
            Thread thread = backingThreadFactory.newThread(runnable);
            if (daemon != null) {
              thread.setDaemon(daemon);
            }
            if (priority != null) {
              thread.setPriority(priority);
            }
            return thread;
          }
        };
    }

}
```