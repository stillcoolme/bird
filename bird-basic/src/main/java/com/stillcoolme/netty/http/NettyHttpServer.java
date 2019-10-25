package com.stillcoolme.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 20:14
 * @description:
 * 程序演示如何使用Netty来构建一个简单的Http服务；
 * Netty采用NIO的网络模型来实现HTTP服务，以此提高性能和吞吐量，
 * Netty除了开发网络应用非常方便，还内置了HTTP相关的编解码器，让用户可以很方便的开发出高性能的HTTP协议的服务
 * <p>
 * 启动服务，在浏览器中输入 http://localhost:2222/?name=stillcoolme 就可以看到页面中显示的内容
 */
public class NettyHttpServer {

    public void run(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                // 服务端往客户端发送数据的行为是Response，所以使用HttpResponseEncoder将数据进行编码操作
                                new HttpResponseEncoder(),
                                // 服务端接收到数据的行为是Request，所以使用HttpRequestDecoder进行解码操作
                                new HttpRequestDecoder(),
                                // 自定义的数据处理类
                                new NettyHttpServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        int port = 2222;
        new NettyHttpServer().run(port);
    }
}
