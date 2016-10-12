package org.openhds.task.sqlite;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Responsible for creating a {@link Mapper} for the given exporter context.
 */
public interface MapperFactory {

    /**
     * Yields a {@link Mapper} implementation usable for the specified export operation.
     *
     * @param md    the metadata from the export's source query
     * @param table the name of the target table of the export
     * @param dst   the {@link Connection} object for the target database
     * @return a {@link Mapper} usable for performing the export operation
     * @throws SQLException
     */
    Mapper createMapper(ResultSetMetaData md, String table, Connection dst) throws SQLException;

}
