package com.stillcoolme.framework.netty.second;

import com.stillcoolme.framework.netty.second.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 16:00
 * @description:
 */
public class ServerPoHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        System.err.println("server message id:" + message.getId());

        // 将连接上来的客户端添加到连接池，后面服务端就可以主动给大家发信息了。
        if (ConnectionPool.getChannel(message.getId()) == null) {
            System.err.println("将连接上来的客户端添加到连接池...");
            ConnectionPool.putChannel(message.getId(), ctx);
        }

        ctx.writeAndFlush(message);

        // 服务端收到客户端发送的心跳类型消息后，回复一条信息
        if(message.getType() == 1) {
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
