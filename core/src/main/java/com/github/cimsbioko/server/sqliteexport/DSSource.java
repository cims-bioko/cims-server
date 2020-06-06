package com.github.cimsbioko.server.sqliteexport;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A {@link Source} implementation that simply delegates to the underlying {@link DataSource}.
 */
public class DSSource implements Source {

    private final DataSource ds;
    private final int fetchSize;

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
