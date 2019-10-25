package com.stillcoolme.framework.netty.http;

import com.stillcoolme.framework.netty.second.message.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 20:18
 * @description: 通过DefaultFullHttpResponse构建了返回的对象，设置了HTTP版本，返回的状态码，返回的内容。
 */
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            System.out.println("URI: " + request.getUri());
            System.err.println(msg);
        }
        if (msg instanceof HttpContent) {
            LastHttpContent httpContent = (LastHttpContent) msg;
            ByteBuf byteData = httpContent.content();
            if (byteData instanceof EmptyByteBuf) {
                System.out.println("Content：无数据");
            } else {
                String content = new String(ByteUtils.objectToBytes(byteData));
                System.out.println("Content:" + content);
            }
        }

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("hello stillcoolme".getBytes("utf-8"))
        );
        fullHttpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
        fullHttpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
        fullHttpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        ctx.write(fullHttpResponse);
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
