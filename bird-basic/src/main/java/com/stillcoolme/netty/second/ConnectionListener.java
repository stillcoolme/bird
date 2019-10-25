package com.stillcoolme.netty.second;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 19:48
 * @description: 将ConnectionListener添加到客户端就可以实现连接重试
 */
public class ConnectionListener implements ChannelFutureListener {

    private ImClient imClient = new ImClient();

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        // 如果失败了我们就启动一个单独的线程来执行重新连接的操作。
        if (!channelFuture.isSuccess()) {
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule((Runnable) () -> {
                System.err.println("服务端链接不上，开始重连操作...");
                imClient.connect(App.HOST, App.PORT);
            }, 1L, TimeUnit.SECONDS);
        } else {
            System.out.println("服务端连接成功");
        }
    }
}
