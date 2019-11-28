package com.stillcoolme.framework.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author: stillcoolme
 * @date: 2019/11/27 17:12
 * @description:
 */
public class AsynchronousTest {

    public static void main(String[] args) {

        CompletionStage<CqlSession> sessionStage = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("10.2.5.3", 9042))
                .withLocalDatacenter("dc1")
                .buildAsync();

        // Chain one async operation after another:
        CompletionStage<AsyncResultSet> responseStage =
                sessionStage.thenCompose(session -> {
                    System.out.println(Thread.currentThread().getName());
                    return session.executeAsync("SELECT release_version FROM system.local");
                });

        // Apply a synchronous computation:
        CompletionStage<String> resultStage =
                responseStage.thenApply(asyncResultSet -> {
                    System.out.println(Thread.currentThread().getName());
                    return asyncResultSet.one().getString(0);
                });

        // Perform an action once a stage is complete:
        resultStage.whenComplete((version, error) -> {
            if (error != null) {
                System.out.printf("Failed to retrieve the version: %s%n", error.getMessage());
            } else {
                System.out.printf("Server version: %s%n", version);
            }
            sessionStage.thenAccept(CqlSession::closeAsync);
        });


        // ===================
        /**
         * As long as you use the asynchronous API,
         * the driver never blocks. You can safely call a driver method from inside a callback:
         */
        CompletionStage<AsyncResultSet> idStage  = sessionStage.thenCompose(seeesion -> {
            return seeesion.executeAsync("SELECT department_id FROM user WHERE id = 1");
        });
        idStage.thenCompose(asyncResultSet -> {
            UUID departMentId = asyncResultSet.one().getUuid(0);
            // Once we have the id, query the details of that department:
            return sessionStage.thenCompose(session -> {
               return session.executeAsync(
                       SimpleStatement.newInstance("SELECT * FROM department WHERE id = ?", departMentId));
            });
        });


    }
}
