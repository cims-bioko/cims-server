package com.github.cimsbioko.server.sqliteexport;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.DriverManager.getConnection;

/**
 * A {@link Target} implementation that generates non-auto-commit connections for export to Sqlite.
 */
public class SqliteTarget implements Target {

    public final String DRIVER_CLASS = "org.sqlite.JDBC";

    public void loadDriver() throws ClassNotFoundException {
        Class.forName(DRIVER_CLASS);
    }

    protected String getUrl(File target) throws IOException {
        return String.format("jdbc:sqlite:%s", target.getCanonicalPath());
    }

    @Override
    public Connection createConnection(File target, boolean autoCommit) throws IOException, SQLException {
        Connection c = getConnection(getUrl(target));
        c.setAutoCommit(autoCommit);
        return c;
    }

    @Override
    public Statement createStatement(Connection c) throws SQLException {
        return c.createStatement();
    }
}
