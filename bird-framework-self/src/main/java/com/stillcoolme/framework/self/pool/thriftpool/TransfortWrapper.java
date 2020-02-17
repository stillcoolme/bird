package com.stillcoolme.framework.self.pool.thriftpool;

import lombok.Data;
import org.apache.thrift.transport.TTransport;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: stillcoolme
 * Date: 2020/2/14 10:18
 * Description: 对TTransport的封装，主要增加了一些辅助信息
 */
@Data
public class TransfortWrapper {

    private TTransport transport;

    /**
     * 是否正忙
     */
    private boolean isBusy = false;

    /**
     * 是否已经挂
     */
    private boolean isDead = false;

    /**
     * 最后使用时间
     */
    private Date lastUseTime;

    /**
     * 服务端Server主机名或IP
     */
    private String host;

    /**
     * 服务端Port
     */
    private int port;

    public TransfortWrapper(TTransport transport, String host, int port) {
        this(transport, host, port, false);
    }

    public TransfortWrapper(TTransport transport, String host, int port, boolean isOpen) {
        this.lastUseTime = new Date();
        this.transport = transport;
        this.host = host;
        this.port = port;
        if (isOpen) {
            try {
                transport.open();
            } catch (Exception e) {
                //e.printStackTrace();
                System.err.println(host + ":" + port + " " + e.getMessage());
                isDead = true;
            }
        }
    }

    public boolean isBusy() {
        return isBusy;
    }

    public boolean isDead() {
        return isDead;
    }

    /**
     * 当前transport是否可用
     *
     * @return
     */
    public boolean isAvailable() {
        return !isBusy && !isDead && transport.isOpen();
    }

    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "hashCode:" + hashCode() + "," +
                host + ":" + port + ",isBusy:" + isBusy + ",isDead:" + isDead + ",isOpen:" +
                transport.isOpen() + ", isAvailable:" + isAvailable() + ", lastUseTime:" + format.format(lastUseTime);
    }




}
