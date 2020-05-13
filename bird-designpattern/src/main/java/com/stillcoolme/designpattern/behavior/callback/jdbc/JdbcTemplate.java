package com.stillcoolme.designpattern.behavior.callback.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 17:32
 * Function:
 *  JdbcTemplate 通过回调的机制将不变的流程抽离出来，放到模板方法 execute(),
 * 再将可变的部分设计成回调 StatementCallback，由用户来定制。
 */
public class JdbcTemplate<T> {

    public <T> T query(String sql, JdbcTemplateDemo.ClientRowMapper clientRowMapper) throws SQLException {

        class QueryStatementCallback implements StatementCallback<T> {
            @Override
            public T doInStatement(Statement stmt) throws SQLException {
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery(sql);
                    // 从 rs 中 提取出实体的代码。。。

                } finally {

                }
                return null;
            }
        }

        return execute(new QueryStatementCallback());

    }

    private <T> T execute(StatementCallback<T> callback) throws SQLException {
        // 构造 Connection
        // 构造 Statement
        Statement statement = null;
        T result = callback.doInStatement(statement);
        return result;
    }
}
