## 写法一：将 Builder 类 直接写在 某个XX类里面

> 特点
1. 设置每个属性，然后return this;
2. 最后通过 build() 来 set 各属性，然后 return 构造好的主类实例；
3. 最后要验证各属性是否符合要求，必set的有没有set，就 throw new RuntimeException(");


## 写法二：直接写一个某个XX类的 Builder 类

这样外部应用使用的是`XXBuilder.newBuilder().setFiled().setFiled().build()`

例子:
* quartz 的 TriggerBuilder，JobBuilder