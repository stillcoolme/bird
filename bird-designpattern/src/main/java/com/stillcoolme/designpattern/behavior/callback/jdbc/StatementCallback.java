package com.stillcoolme.designpattern.behavior.callback.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

public interface StatementCallback<T> {
    public T doInStatement(Statement stmt) throws SQLException;

}
