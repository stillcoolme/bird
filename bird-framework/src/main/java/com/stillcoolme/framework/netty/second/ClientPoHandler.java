package com.stillcoolme.framework.netty.second;

import com.stillcoolme.framework.netty.second.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 16:12
 * @description:
 */
public class ClientPoHandler extends ChannelInboundHandlerAdapter {

    private ImClient imClient = new ImClient();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Message message = (Message) msg;
        System.out.println("client:" + message.getContent());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 心跳检测，
     * 当客户端20秒没往服务端发送过数据，就会触发IdleState.WRITER_IDLE事件，
     * 这个时候我们就像服务端发送一条心跳数据，跟业务无关，只是心跳。
     *
     * 服务端收到心跳之后就会回复一条消息，表示已经收到了心跳的消息，
     * 只要收到了服务端回复的消息，那么就不会触发IdleState.READER_IDLE事件
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if(idleStateEvent.state().equals(IdleState.READER_IDLE)) {
                System.out.println("长期没收到服务器推送数据");
                //选择重新连接
            } else if(idleStateEvent.state().equals(IdleState.WRITER_IDLE)) {
                System.out.println("长期未向服务器发送数据");
                //发送心跳包
                ctx.writeAndFlush(new Message("1", "test", 1));
            } else if(idleStateEvent.state().equals(IdleState.ALL_IDLE)) {
                System.out.println("ALL");
            }
        }
    }

    /**
     * 处理运行中连接断开时重试
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("掉线了...");
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule((Runnable) () -> {
            System.err.println("服务端链接不上，开始重连操作...");
            imClient.connect(App.HOST, App.PORT);
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }
}
