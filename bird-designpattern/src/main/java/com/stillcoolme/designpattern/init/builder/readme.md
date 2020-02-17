## 写法一：将 Builder 类 直接写在 某个XX类里面

> 特点
1. 设置每个属性，然后return this;
2. 最后通过 build() 来 set 各属性，然后 return 构造好的主类实例；
3. 最后要验证各属性是否符合要求，必set的有没有set，就 throw new RuntimeException(");


## 写法二：写一个某个XX类的 Builder 类

这样外部应用使用的是`XXBuilder.setFiled().setFiled().build()`

具体实现就是包含在`XXBuilder`里面不用暴露给外部。

```java
// 下面示例 XX 就是 ThreadFactory， XX 应该是 接口，后面所有实现都能用这个 XXBuilder
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
    
    // 真正构造 XX 的，是私有方法！
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

> 特点

第二种方法，用的时候还是要`new XXBuilder`啊，本来就是为了不要`new XX()`出现`new`。