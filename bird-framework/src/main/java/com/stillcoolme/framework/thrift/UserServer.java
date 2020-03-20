package com.stillcoolme.framework.thrift;

import com.stillcoolme.framework.thrift.impl.UserServiceImpl;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;

/**
 * @author: stillcoolme
 * @date: 2020/3/10 15:33
 * Function:
 */
public class UserServer {

    private static final int SERVER_PORT = 8090;

    public void startServer() {
        try {
            System.out.println("HelloWorld TSimpleServer start ....");
            TProcessor tprocessor = new UserService.Processor<>(new UserServiceImpl());

            // 简单的单线程服务模型，一般用于测试
            TServerSocket serverTransport = new TServerSocket(SERVER_PORT);
            TServer.Args tArgs = new TServer.Args(serverTransport);
            tArgs.processor(tprocessor);
            tArgs.protocolFactory(new TBinaryProtocol.Factory());
            TServer server = new TSimpleServer(tArgs);  // Strom drpc 中使用的是 THsHaServer：半同步/半异步Server，多线程处理业务逻辑调用，同样依赖于TFramedTransport；
            server.serve();

        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        UserServer server = new UserServer();
        server.startServer();
    }

}
