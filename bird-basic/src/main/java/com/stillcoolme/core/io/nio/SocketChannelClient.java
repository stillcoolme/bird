package com.stillcoolme.core.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Author: stillcoolme
 * Date: 2019/6/26 16:39
 * Description:
 */
public class SocketChannelClient {

    String server;
    Integer port;
    SocketChannel socketChannel;

    public SocketChannelClient(String server, Integer port) {
        this.server = server;
        this.port = port;
    }

    public String sendMessage(String message) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(server, port));
        //socketChannel.bind(new InetSocketAddress(server, port));
        // 发送请求
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(byteBuffer);

        // 读取响应数据
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int num;
        if((num = socketChannel.read(readBuffer)) > 0){
            readBuffer.flip();
            byte[] destByte = new byte[1024];
            readBuffer.get(destByte);
            return new String(destByte, "UTF-8");
        }
        return null;
    }


    public static void main(String[] args) {
        SocketChannelClient socketChannelClient = new SocketChannelClient("localhost", 8081);
        try {
            socketChannelClient.sendMessage("tcp request data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
