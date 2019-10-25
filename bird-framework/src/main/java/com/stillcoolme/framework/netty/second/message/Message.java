package com.stillcoolme.framework.netty.second.message;

import java.io.Serializable;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 15:16
 * @description:
 *  传输对象
 */
public class Message implements Serializable {

    private static final long serialVersionUID = -7543514952950971498L;
    // 标识客户端
    private String id;
    // 内容
    private String content;
    // 消息类型（0：业务消息  1：心跳消息）
    private int type;

    public Message() {
    }

    public Message(String id, String content, int type) {
        this.id = id;
        this.content = content;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
