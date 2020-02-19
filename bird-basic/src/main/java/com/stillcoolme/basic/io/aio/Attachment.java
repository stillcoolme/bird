package com.stillcoolme.basic.io.aio;

import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * @author: stillcoolme
 * @date: 2020/2/19 13:46
 */
@Data
public class Attachment {
    private AsynchronousServerSocketChannel server;
    private AsynchronousSocketChannel client;
    private Boolean readMode;
    private ByteBuffer buffer;
}
