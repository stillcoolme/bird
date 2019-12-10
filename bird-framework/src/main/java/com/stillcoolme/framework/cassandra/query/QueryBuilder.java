package com.stillcoolme.framework.cassandra.query;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.stillcoolme.framework.cassandra.CassandraClient;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;

/**
 * @author: stillcoolme
 * @date: 2019/11/29 9:19
 * @description:
 */
public class QueryBuilder {

    public static void main(String[] args) {

        try (CqlSession session = CassandraClient.getCassandraSession()) {

/*            Select query = selectFrom("system", "local").column("release_version"); // SELECT release_version FROM system.local
            SimpleStatement statement = query.build();

            ResultSet rs = session.execute(statement);
            Row row = rs.one();
            System.out.println(row.getString("release_version"));*/

            // ====================

            Select query1 = selectFrom("ks_facedb", "ks_face_counter")
                    .columns("table_name", "id");
            SimpleStatement build = query1.build();

            ResultSet set = session.execute(build);
            Row row1 = set.one();
            System.out.println(row1.getString(0) + " " + row1.getLong(1));

            Update updateQuery = update("ks_facedb", "ks_face_counter")
                    // UPDATE ks_face_counter SET id+=1...
                    .increment("id")
                    .whereColumn("table_name").isEqualTo(bindMarker());

            PreparedStatement prepare = session.prepare(updateQuery.build());
            BoundStatement bound = prepare.bind("test1");
            ResultSet updateResult = session.execute(bound);
            Row one = updateResult.one();

            ResultSet set2 = session.execute(build);
            Row row2 = set2.one();
            System.out.println(row2.getString(0) + " " + row2.getLong(1));
        }
    }
}
