package com.stillcoolme.framework.netty.second;

import com.stillcoolme.framework.netty.second.message.MessageDecoder;
import com.stillcoolme.framework.netty.second.message.MessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 15:50
 * @description:
 */
public class ImServer {

    public void run(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("decoder", new MessageDecoder());
                        socketChannel.pipeline().addLast("encoder", new MessageEncoder());
                        // 改成 实体类传输数据，jdk序列化
                        socketChannel.pipeline().addLast(new ServerPoHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        // 启动服务,指定端口为2222
        int port = 2222;
        new Thread(() -> {
            new ImServer().run(port);
        }).start();
    }
}
