package com.stillcoolme.framework.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * @author: stillcoolme
 * @date: 2020/3/10 15:02
 * Function:
 */
public class UserClient {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8090;
    private static final int TIMEOUT = 30000;

    public void startClient() {
        TTransport transport = null;
        try {
            transport = new TSocket(SERVER_IP, SERVER_PORT);
            // 协议要和服务端一致
            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            // TProtocol protocol = new TCompactProtocol(transport);
            // TProtocol protocol = new TJSONProtocol(transport);
            // 拿到客户端的引用
            UserService.Client client = new UserService.Client(protocol);
            transport.open();
            User user = new User();
            user.username = "bobo";
            user.age = 26;
            String result = client.sayHello(user);
            System.out.println("Thrift client get result: " + result);

        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        UserClient client = new UserClient();
        client.startClient();

    }

}
