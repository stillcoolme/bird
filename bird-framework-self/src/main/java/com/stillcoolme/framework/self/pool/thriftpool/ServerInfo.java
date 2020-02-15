package com.stillcoolme.framework.self.pool.thriftpool;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Author: stillcoolme
 * Date: 2020/2/14 10:16
 * Description: 封装服务端的基本信息，主机名+端口号，连接时需要用到
 */
@Data
@AllArgsConstructor
public class ServerInfo {

    private String host;
    private int port;

}
