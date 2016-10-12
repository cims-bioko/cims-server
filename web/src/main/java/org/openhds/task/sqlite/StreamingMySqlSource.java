package org.openhds.task.sqlite;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import static java.lang.Integer.MIN_VALUE;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

/**
 * A {@link Source} implementation that generates streaming {@link Statement}s for MySQL, allowing export of large data
 * sets without filling consuming proportional amounts of memory.
 */
public class StreamingMySqlSource implements Source {

    private DataSource ds;

    public StreamingMySqlSource(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Connection createConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public Statement createStatement(Connection c) throws SQLException {
        Statement s = c.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        s.setFetchSize(MIN_VALUE);
        return s;
    }
}
