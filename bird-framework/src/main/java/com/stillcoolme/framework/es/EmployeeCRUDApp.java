package com.stillcoolme.framework.es;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;


/**
 * @author: stillcoolme
 * @date: 2019/10/30 14:52
 * @description:
 */
public class EmployeeCRUDApp {

    public static void main(String[] args) throws IOException {

        // 先构建client
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
                .build();

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.2.5.2"), 9300));

        createEmployee(client);

    }

    /**
     * 创建员工信息（创建一个document）
     * @param client
     */
    private static void createEmployee(TransportClient client) throws IOException {
        IndexResponse response = client.prepareIndex("company", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "jack")
                        .field("age", 27)
                        .field("position", "technique")
                        .field("country", "china")
                        .field("join_date", "2017-01-01")
                        .field("salary", 10000)
                        .endObject())
                .get();
        System.out.println(response.getResult());
    }


}
