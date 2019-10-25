package com.stillcoolme.framework.netty.second.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 15:37
 * @description:
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object obj = ByteUtils.bytesToObject(ByteUtils.read(in));
        out.add(obj);
    }

}
