package com.stillcoolme.core.io.nio.io;

import java.io.IOException;
import java.net.Socket;

/**
 * @author: stillcoolme
 * @date: 2019/11/9 20:38
 * @description:
 */
public class BIOClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1",8080);
            Thread.sleep(2000);
            socket.getOutputStream().write("向服务器发数据".getBytes());
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
