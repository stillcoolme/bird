package com.stillcoolme.netty.first;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author: stillcoolme
 * @date: 2019/8/19 19:17
 * @description:
 **/
public class ImServer{

    public void run(int port) {
        // 通过group方法关联了两个线程组，NioEventLoopGroup是用来处理I/O操作的线程池，
        // 第一个称为“boss”，用来accept客户端连接，
        // 第二个称为“worker”，处理客户端数据的读写操作。
        // 当然你也可以只用一个NioEventLoopGroup同时来处理连接和读写，bootstrap.group()方法支持一个参数。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup  workerGroup = new NioEventLoopGroup();

        // 通过ServerBootstrap 进行服务的配置，和socket的参数可以通过ServerBootstrap进行设置。
        ServerBootstrap bootstrap = new ServerBootstrap ();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // childHandler用来配置具体的数据处理方式，可以指定编解码器，处理数据的Handler
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("decoder", new StringDecoder());
                        socketChannel.pipeline().addLast("encoder", new StringEncoder());
                        socketChannel.pipeline().addLast(new ServerStringHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            // 绑定端口启动服务
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
