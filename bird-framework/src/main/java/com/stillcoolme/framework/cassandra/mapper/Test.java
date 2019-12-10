package com.stillcoolme.framework.cassandra.mapper;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.stillcoolme.framework.cassandra.CassandraClient;

import java.util.UUID;

/**
 * @author: stillcoolme
 * @date: 2019/11/28 14:29
 * @description:
 */
public class Test {

    public static void main(String[] args) {

        CqlSession cassandraSession = CassandraClient.getCassandraSession();

        // KeySpaceMapperBuilder 这个类要编译以后才会出现， 根据 KeySpaceMapper 来自动生成
        // KeySpaceMapper 和 session 的 lifecycle 相同，而且线程安全。
        KeySpaceMapper inventoryMapper = new KeySpaceMapperBuilder(cassandraSession).build();

        // fromCql("cjh_test") 传入 keyspace名
        // ProductDao dao = inventoryMapper.productDao(CqlIdentifier.fromCql("cjh_test"));
        ProductDao dao = inventoryMapper.productDao(CqlIdentifier.fromCql("cjh_test"),
                CqlIdentifier.fromCql("product"));
        UUID s = UUID.randomUUID();
        dao.save(new Product(s, "Apple"));

        Product product = dao.findById(s);
        System.out.println(product.getDescription());

        cassandraSession.close();
    }
}
