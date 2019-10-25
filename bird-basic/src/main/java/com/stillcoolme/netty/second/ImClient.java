package com.stillcoolme.netty.second;

import com.stillcoolme.netty.second.message.Message;
import com.stillcoolme.netty.second.message.MessageDecoder;
import com.stillcoolme.netty.second.message.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * @author: stillcoolme
 * @date: 2019/10/24 16:07
 * @description:
 */
public class ImClient {

    private Channel channel;

    public Channel connect(String host, int port) {
        doConnect(host, port);
        return this.channel;
    }

    private void doConnect(String host, int port) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // IdleStateHandler用于心跳检测，20秒没往服务端发送过数据就发送一个心跳
                            ch.pipeline().addLast("ping", new IdleStateHandler(60, 20, 60 * 10, TimeUnit.SECONDS));
                            ch.pipeline().addLast("decoder", new MessageDecoder());
                            ch.pipeline().addLast("encoder", new MessageEncoder());
                            ch.pipeline().addLast(new ClientPoHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.addListener(new ConnectionListener());
            channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
