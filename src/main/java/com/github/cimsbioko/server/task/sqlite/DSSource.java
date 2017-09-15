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
    private int fetchSize;

    public DSSource(DataSource ds) {
        this(ds, 0);
    }

    public DSSource(DataSource ds, int fetchSize) {
        this.ds = ds;
        this.fetchSize = fetchSize;
    }

    @Override
    public Connection createConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public Statement createStatement(Connection c) throws SQLException {
        Statement stmt = c.createStatement();
        stmt.setFetchSize(fetchSize);
        return stmt;
    }
}
