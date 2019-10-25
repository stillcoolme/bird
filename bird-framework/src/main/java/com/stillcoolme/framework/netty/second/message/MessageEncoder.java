package com.stillcoolme.framework.netty.second.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 15:18
 * @description:
 *  将对象序列化成字节，通过ByteBuf形式进行传输，ByteBuf是一个byte存放的缓冲区，提供了读写操作。
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] bytes = ByteUtils.objectToBytes(msg);
        out.writeBytes(bytes);
        ctx.flush();
    }
}
