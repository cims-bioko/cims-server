package com.github.cimsbioko.server.sqliteexport;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The operations required of the source side of an export.
 */
public interface Source {

    Connection createConnection() throws SQLException;

    Statement createStatement(Connection c) throws SQLException;

}
