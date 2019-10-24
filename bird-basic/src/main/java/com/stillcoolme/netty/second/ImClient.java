package com.stillcoolme.netty.second;

import com.stillcoolme.netty.second.message.Message;
import com.stillcoolme.netty.second.message.MessageDecoder;
import com.stillcoolme.netty.second.message.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.UUID;


/**
 * @author: stillcoolme
 * @date: 2019/10/24 16:07
 * @description:
 */
public class ImClient {

    private Channel channel;

    private Channel connect(String host, int port) {
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
                            ch.pipeline().addLast("decoder", new MessageDecoder());
                            ch.pipeline().addLast("encoder", new MessageEncoder());
                            ch.pipeline().addLast(new ClientPoHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 2222;
        Channel channel = new ImClient().connect(host, port);
        //对象传输数据
        Message message = new Message();
        message.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setContent("hello stillcoolme");
        channel.writeAndFlush(message);
        //字符串传输数据
        //channel.writeAndFlush("stillcoolme");
    }
}
