package com.stillcoolme.framework.flink.asyncio;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.concurrent.TimeUnit;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-12-27 10:30:00
 * @Description
 *
 *  用 asyncio 查询 mysql side table 维表
 *
 *  简单的来说，使用 Async I/O 对应到 Flink 的 API 就是 RichAsyncFunction 这个抽象类，
 *  继承这个抽象类实现里面的open（初始化），asyncInvoke（数据异步调用），close（停止的一些操作）方法，
 *  最主要的是实现 asyncInvoke 里面的方法。
 *
 *  使用异步函数访问外部数据系统，一般是外部系统有异步访问客户端，如果没有的话，可以自己使用线程池异步访问外部系统。
 *
 *  还可以参考 袋鼠云的实现 https://blog.csdn.net/yidan7063/article/details/89920935
 */
public class App {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> src = env.addSource(new RichSourceFunction<String>() {
            @Override
            public void run(SourceContext<String> ctx) throws Exception {
                String[] inputData = {"admin", "bobo"};
                for (String input : inputData) {
                    ctx.collect(input);
                }
            }

            @Override
            public void cancel() {

            }
        });

        // 1. 支撑异步的mysql客户端实现  异步IO
        // 1.1 unorderedWait: 异步请求一结束就马上输出结果, 因为异步请求完成时间的不确定性, 结果输出的顺序可能和输入不同
//        AsyncDataStream.unorderedWait(src, new MysqlAsyncFunction(), 10, TimeUnit.SECONDS, 10).print();
        // 1.2 orderedWait: 结果输出的顺序和输入的顺序是一样的, 后输入的数据异步请求先完成了缓存在一个指定的结果中,
        //        直到在此之前的记录全部完成异步请求并输出后,才能输出,因此带来了一些额外的延迟和checkpoint开销
//        AsyncDataStream.orderedWait(src, new MysqlAsyncFunction(), 1000, TimeUnit.SECONDS, 10).print();

        // 2. mysql客户端连接池 实现  异步IO
        SingleOutputStreamOperator<String> joinStream =
                AsyncDataStream.unorderedWait(src, new MysqlAsyncByThreadPoolFunction(), 10, TimeUnit.SECONDS, 10);

        joinStream.print();

        env.execute();

    }
}
