package com.stillcoolme.basic.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author: stillcoolme
 * @date: 2019/11/10 15:34
 * @description: 使用 ServerSocketChannel 实现的服务端
 * 将Socket连接存储在一个list集合中，每次等待客户端消息时都去List轮询，看消息是否准备好，如果准备好则直接打印消息；
 * 从头到尾我们一直没有开启第二个线程，而是一直采用单线程来处理多个客户端的连接；
 * 但是采用了一个轮询的方式来接收消息，假设有1000万连接这种轮询的方式效率是极低的。
 * <p>
 * <p>
 * 运行：启动 NIOServer 后，启动 NIOServer 或者 用 telnet ip port 来测试
 */
public class NIOServer {

    public static void main(String[] args) throws InterruptedException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        List<SocketChannel> socketList = new ArrayList();
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("localhost", 8089));
            //设置为非阻塞，下面 serverSocketChannel.accept() 不会阻塞住，能执行下去
            serverSocketChannel.configureBlocking(false);


            // 没使用 selector，而是 java层面放到list来轮询
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null) {
                    //表示没人连接
                    System.out.println("正在等待客户端请求连接...");
                    Thread.sleep(5000);
                } else {
                    System.out.println("当前接收到客户端请求连接...");
                    // 将客户端请求放到 list中
                    socketList.add(socketChannel);
                }

                for (SocketChannel socket : socketList) {
                    socket.configureBlocking(false);
                    int effective = socket.read(byteBuffer);
                    if (effective != 0) {
                        byteBuffer.flip();//切换模式  写-->读
                        String content = Charset.forName("UTF-8").decode(byteBuffer).toString();
                        System.out.println("接收到消息:" + content);
                        byteBuffer.clear();
                    } else {
                        System.out.println("当前未收到客户端消息");
                    }
                }
            }

            // 使用 selector 来轮询连接
            /*
            Selector selector = Selector.open();
            // 将 serverSocketChannel 注册到 Selector 中，监听 OP_ACCEPT 事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        // 有已经接受的新的到服务端的连接
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        // 有新的连接并不代表这个通道就有数据
                        // 将这个新的 SocketChannel 注册到 Selector，监听 OP_READ 事件，等待数据
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        // 有数据可读
                        // 上面一个 if 分支中注册了监听 OP_READ 事件的 SocketChannel
                        SocketChannel socketChannel  = (SocketChannel) key.channel();
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        int num = socketChannel.read(readBuffer);
                        if (num > 0) {
                            // 处理进来的数据
                            System.out.println("收到数据：" + new String(readBuffer.array()).trim());
                            ByteBuffer buffer = ByteBuffer.wrap("返回给客户端的数据...".getBytes());
                            socketChannel.write(buffer);
                        } else if (num == -1) {
                            // -1 代表连接已经关闭
                            socketChannel.close();
                        }
                    }

                }
            }*/

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
