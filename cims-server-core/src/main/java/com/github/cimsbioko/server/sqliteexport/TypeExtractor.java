package com.github.cimsbioko.server.sqliteexport;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Extracts type information useful for converting JDBC types as specified in {@link java.sql.Types} to corresponding
 * SQL type names for a database.
 */
public interface TypeExtractor {

    /**
     * Returns a mapping of JDBC type to SQL type for the given database connection.
     *
     * @param dst the {@link Connection} to the target system
     * @return mapping of JDBC type from {@link java.sql.Types} to SQL type as a string
     * @throws SQLException if there was an error extracting type information
     */
    Map<Integer, String> getTypeMap(Connection dst) throws SQLException;

}
