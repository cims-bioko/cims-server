package org.openhds.task.sqlite;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements a type extractor that simply uses the {@link DatabaseMetaData} for the given connection.
 */
public class DBTypeExtractor implements TypeExtractor {

    public Map<Integer, String> getTypeMap(Connection dst) throws SQLException {
        Map<Integer, String> typeMap = new LinkedHashMap<>();
        DatabaseMetaData metadata = dst.getMetaData();
        try (ResultSet results = metadata.getTypeInfo()) {
            while (results.next()) {
                typeMap.put(results.getInt("DATA_TYPE"), results.getString("TYPE_NAME"));
            }
        }
        return typeMap;
    }

}
