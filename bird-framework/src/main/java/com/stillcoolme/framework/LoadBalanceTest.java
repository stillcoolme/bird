package com.stillcoolme.framework;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.stillcoolme.framework.cassandra.CassandraClient;

/**
 * @author: stillcoolme
 * @date: 2019/12/4 14:27
 * @description:
 */
public class LoadBalanceTest {

    public static void main(String[] args) {

        CqlSession cassandraSession = CassandraClient.getCassandraSession();

//        String SQL = "select * from ks_facedb.ks_face_dbname";
//        ResultSet result = cassandraSession.execute(SQL);

        PreparedStatement pst1 =
                cassandraSession.prepare("SELECT * FROM ks_facedb.ks_face_dbname WHERE db_name = :db_name");
        BoundStatement statement1 = pst1.bind("archive_db");

        String routingKey = statement1.getRoutingKey().toString();
        String routingKeySpace = statement1.getRoutingKeyspace().toString();

        System.out.println(routingKey);

        cassandraSession.execute(statement1);

    }
}
