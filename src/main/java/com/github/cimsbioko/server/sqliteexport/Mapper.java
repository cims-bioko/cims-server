package com.github.cimsbioko.server.sqliteexport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Specifies a method of mapping data from a {@link Source} to a {@link Target} for an {@link Exporter}.
 */
interface Mapper {

    /**
     * Yields a statement for dropping the exported table in the target database.
     *
     * @return executable DDL for dropping the table exported by this mapper
     */
    String getDropDdl();

    /**
     * Yields a statement for creating the exported table in the target database.
     *
     * @return executable DDL for creating the table exported by this mapper
     * @throws SQLException
     */
    String getCreateDdl() throws SQLException;

    /**
     * Yields a statement for inserting data into the exported table in the target database.
     *
     * @return executable DML for inserting the table exported by this mapper
     * @throws SQLException
     */
    String getInsertDml() throws SQLException;

    /**
     * Binds values from the current record of the source query to the target insert statement. After this method
     * completes, the target insert statement can be executed.
     *
     * @param source the {@link ResultSet} to copy values from, cued to current record
     * @param target the prepared insert statement to bind values to
     * @throws SQLException upon error binding values
     */
    void bind(ResultSet source, PreparedStatement target) throws SQLException;
}
