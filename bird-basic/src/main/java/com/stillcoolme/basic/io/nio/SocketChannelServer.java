package com.stillcoolme.basic.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Author: stillcoolme
 * Date: 2019/6/26 19:31
 * Description:
 *  使用 ServerSocketChannel 实现的阻塞服务端，每来一个请求就开一个SocketHandler线程来处理。
 */
public class SocketChannelServer {

    Integer listenPort;
    ServerSocketChannel serverSocketChannel;

    public SocketChannelServer(Integer listenPort) {
        this.listenPort = listenPort;
    }

    public String ProcessMessage() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        // 监听端口进来的 TCP 链接
        serverSocketChannel.socket().bind(new InetSocketAddress(listenPort));
        while (true){
            // 这里会阻塞，直到有一个请求的连接进来
            SocketChannel socketChannel = serverSocketChannel.accept();
            // 开启一个新的线程来处理这个请求，然后在 while 循环中继续监听端口
            SocketHandler handler = new SocketHandler(socketChannel);
            new Thread(handler).start();
        }
    }

    class SocketHandler implements Runnable{
        private SocketChannel socketChannel;

        public SocketHandler(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            try{
                int num;
                while ((num = socketChannel.read(byteBuffer)) > 0){
                    byteBuffer.flip();
                    // 提取 Buffer 中的数据
                    byte[] bytes = new byte[num];
                    byteBuffer.get(bytes);
                    String re = new String(bytes, "UTF-8");
                    System.out.println("收到请求：" + re);

                    // 将响应返回给客户端
                    ByteBuffer writeBuffer = ByteBuffer.wrap(("我已经收到你的请求，你的请求内容是：" + re).getBytes());
                    socketChannel.write(writeBuffer);
                    byteBuffer.clear();
                }
            } catch (IOException e){
                e.printStackTrace();
                //IOUtils.closeQuietly(socketChannel);
            }

        }
    }


    public static void main(String[] args) {
        SocketChannelServer socketChannelServer = new SocketChannelServer(8081);
        try {
            socketChannelServer.ProcessMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
