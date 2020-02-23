package com.stillcoolme.basic.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author: stillcoolme
 * @date: 2020/2/19 8:45
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8089));
        // 发送请求
        ByteBuffer buffer = ByteBuffer.wrap("123456".getBytes());
        socketChannel.write(buffer);

        // 读取响应
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int num;
        if((num = socketChannel.read(readBuffer)) > 0) {
            readBuffer.flip();
            byte[] re = new byte[num];
            readBuffer.get(re);
            String result = new String(re, "UTF-8");
            System.out.println("返回值：" + result);
        }

    }
}
