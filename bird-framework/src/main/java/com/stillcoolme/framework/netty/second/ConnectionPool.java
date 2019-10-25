package com.stillcoolme.framework.netty.second;

import io.netty.channel.ChannelHandlerContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: stillcoolme
 * @date: 2019/10/25 10:11
 * @description:
 */
public class ConnectionPool {

    private static Map<String, ChannelHandlerContext> connectPool = new ConcurrentHashMap<>();

    public static ChannelHandlerContext getChannel(String clientId) {
        if (clientId == null) {
            return null;
        }
        return connectPool.get(clientId);
    }

    public static ChannelHandlerContext putChannel(String clientId, ChannelHandlerContext channel) {
        return connectPool.put(clientId, channel);
    }

    public static Set<String> getClients() {
        return connectPool.keySet();
    }

    public static List<ChannelHandlerContext> getChannels() {
        List<ChannelHandlerContext> channels = new ArrayList<>();
        for (Map.Entry<String, ChannelHandlerContext> entry : connectPool.entrySet()) {
            channels.add(entry.getValue());
        }
        return channels;
    }


}
