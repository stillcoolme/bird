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
 * 将dump文件上传到 https://heaphero.io/index.jsp 观察
 */
public class HeapOOM {

    public static void main(String[] args) {
        List list = new ArrayList(10);
        while (true){
            list.add(1);
        }

    }
}

/**
 * 例子：
 * 我在一个ArrayList中连续插入1千万条数据，结果耗时不一样，分别是 2346ms 797ms 没搞明白"
 * 别人看了一眼，就知道这小伙底盘不稳。
 * "你加个 -XX:+PrintGCDetails -XX:+PrintGCDateStamps，看下第一次是不是有Full GC！！"
 * 果然是！！！
 *
 * 但是调大了堆空间后，这次没有GC了，但是每次运行，前一个都比后一个耗时多点，这是怎么回事？"
 *
 * "你试试放在不同线程中运行？"
 * "在不同线程中执行，两者耗时几乎一致，这是为什么？"
 * "你知道OSR(On-Stack Replacement )吗？是一种在运行时替换正在运行的函数/方法的栈帧的技术。"
 *
 * 在现代的主流JVM中，都具备了多层编译的能力，一开始以解释的方式进行执行，这种性能相对来说（和c++比）会慢一点
 * 但是一旦发现某一个函数执行很频繁的时候，就会采用JIT编译，提高函数执行性能（大部分比c++还快）。
 * 但是，如果以函数为单位进行JIT编译，那么就无法应对main函数中包含循环体的情况，这个时候，OSR就派上了用场。
 * 与其编译整个方法，我们可以在发现某个方法里有循环很热的时候，选择只编译方法里的某个循环，
 * 当循环体执行到 i = 5000 的时候，循环计数器达到了触发OSR编译的阈值，等编译完成之后，就可以执行编译后生成的代码。
 * 所以在上面例子中，当我们第二次执行循环体的时候，已经在执行OSR编译后的代码，那么在性能上会比前一次会快那么一点点。
 *
 */
