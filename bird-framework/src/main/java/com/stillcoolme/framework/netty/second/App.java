package com.stillcoolme.framework.netty.second;

import com.stillcoolme.framework.netty.second.message.Message;
import io.netty.channel.Channel;

import java.util.UUID;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 19:52
 * @description:
 */
public class App {

    static String HOST = "127.0.0.1";
    static int PORT = 2222;

    public static void main(String[] args) {
        Channel channel = new ImClient().connect(HOST, PORT);
        //对象传输数据
        Message message = new Message();
        message.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setContent("hello stillcoolme");
        channel.writeAndFlush(message);
        //字符串传输数据
        //channel.writeAndFlush("stillcoolme");
    }
}
