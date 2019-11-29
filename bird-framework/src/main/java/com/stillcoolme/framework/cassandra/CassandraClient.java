package com.stillcoolme.framework.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.net.InetSocketAddress;

/**
 * @author: stillcoolme
 * @date: 2019/11/27 14:36
 * @description:
 */
public class CassandraClient {
//    private static final Logger logger = LoggerFactory.getLogger(CassandraClient.class);

    private static class Holder {
        /**
         * CqlSession is the main entry point of the driver.
         *  It holds the known state of the actual Cassandra cluster,
         *  and is what you use to execute queries.
         *  It is thread-safe, you should create a single instance (per target Cassandra cluster),
         *  and share it throughout your application;

         * Always close the CqlSession once you’re done with it,
         * in order to free underlying resources (TCP connections, thread pools…).
         */
        private static CqlSession session;
        static {
             session = CqlSession
                        .builder()
                        .addContactPoint(new InetSocketAddress("10.2.5.3", 9042))
                        .withLocalDatacenter("dc1")
                        .build();
        }

    }

    public static CqlSession getCassandraSession() {
        return Holder.session;
    }

    public static void main(String[] args) {

        CqlSession cqlSession = CassandraClient.getCassandraSession();
        ResultSet rs = cqlSession.execute("select release_version from system.local");
        Row row = rs.one();
        System.out.println(row.getString("release_version"));
        System.out.println(row.getString(0));

        ColumnDefinitions definition = row.getColumnDefinitions();
        definition.forEach(x -> {
            System.out.println(x.getName());
            System.out.println(x.getTable());
        });

        cqlSession.close();
    }




}

