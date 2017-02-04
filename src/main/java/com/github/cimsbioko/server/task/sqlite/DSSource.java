package com.github.cimsbioko.server.task.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * A {@link Source} implementation that simply delegates to the underlying {@link DataSource}.
 */
public class DSSource implements Source {

    private DataSource ds;

    public DSSource(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Connection createConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public Statement createStatement(Connection c) throws SQLException {
        return c.createStatement();
    }
}
