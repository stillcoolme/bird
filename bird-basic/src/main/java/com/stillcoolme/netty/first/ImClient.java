package com.stillcoolme.netty.first;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author: stillcoolme
 * @date: 2019/8/19 19:45
 * @description:
 *  客户端连接逻辑
 **/
public class ImClient {

    private Channel channel;
    
    public Channel connect(String host, int port) {
        doConnect(host, port);
        return this.channel;
    }

    private void doConnect(String host, int port) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("decoder", new StringDecoder());
                ch.pipeline().addLast("encoder", new StringEncoder());
                ch.pipeline().addLast(new ClientStringHandler());
            }
        });
        try{
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channel = channelFuture.channel();
        } catch (Exception e) {

        }
    }

    /**
     * 测试步骤如下：
     * 首先启动服务端
     * 启动客户端，发送消息
     * 服务端收到消息，控制台有输出server: stillcoolme
     * 客户端收到服务端回复的消息，控制台有输出client: stillcoolme, 你好
     * @param args
     */
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 2222;
        Channel channel = new ImClient().connect(host, port);
        channel.writeAndFlush("stillcoolme");
    }
}
