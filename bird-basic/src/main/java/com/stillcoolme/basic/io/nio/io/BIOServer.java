package com.stillcoolme.basic.io.nio.io;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: stillcoolme
 * @date: 2019/11/9 20:33
 * @description:
 *
 *
 */
public class BIOServer {

    public static void main(String[] args) {
        byte[] buffer = new byte[1024];
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("服务器已启动并监听8080端口");
            while (true) {
                System.out.println("服务器正在等待连接...");
                Socket socket = serverSocket.accept();

                /* 多线程版
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("服务器已接收到连接请求...");
                        System.out.println();
                        System.out.println("服务器正在等待数据...");
                        try {
                            socket.getInputStream().read(buffer);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        System.out.println("服务器已经接收到数据");
                        System.out.println();
                        String content = new String(buffer);
                        System.out.println("接收到的数据:" + content);
                    }
                }).start();
                */

                System.out.println("服务器已接收到连接请求...");
                System.out.println("服务器正在等待数据...");
                socket.getInputStream().read(buffer);
                System.out.println("服务器已经接收到数据");
                String content = new String(buffer);
                System.out.println("接收到的数据:" + content);
            }
        } catch (Exception e) {

        }



    }
}
