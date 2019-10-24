package com.stillcoolme.netty.first;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author: stillcoolme
 * @date: 2019/8/19 19:41
 * @description:
 *  消息处理
 **/
public class ServerStringHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.err.println("server receive msg: " + msg.toString());
        ctx.writeAndFlush(msg.toString() + " 你好");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
