package com.github.cimsbioko.server.sqliteexport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import static java.lang.String.format;

/**
 * A {@link Mapper} implementation capable for exporting tables to the Sqlite database.
 */
class SqliteMapper implements Mapper {

    public static final String DEFAULT_COL_TYPE = "TEXT";

    private ResultSetMetaData md;
    private String table;
    private Map<Integer, String> typeMap;

    SqliteMapper(ResultSetMetaData md, String table, Map<Integer, String> typeMap) {
        this.md = md;
        this.table = table;
        this.typeMap = typeMap;
    }

    @Override
    public String getDropDdl() {
        return format("drop table if exists %s", table);
    }

    public String getCreateDdl() throws SQLException {
        StringBuilder b = new StringBuilder(format("create table if not exists %s (", table));
        for (int c = 1; c <= md.getColumnCount(); c++) {
            String colName = md.getColumnLabel(c);
            int colType = md.getColumnType(c);
            String typeName = typeMap != null && typeMap.containsKey(colType) ? typeMap.get(colType) : DEFAULT_COL_TYPE;
            b.append(colName);
            b.append(' ');
            b.append(typeName);
            if (c != md.getColumnCount()) {
                b.append(',');
            }
        }
        b.append(")");
        return b.toString();
    }

    public String getInsertDml() throws SQLException {
        StringBuilder b = new StringBuilder("insert into " + table + " (");
        int columnCount = md.getColumnCount();
        for (int c = 1; c <= columnCount; c++) {
            boolean lastColumn = c == columnCount;
            b.append(md.getColumnLabel(c));
            if (!lastColumn) {
                b.append(',');
            }
        }
        b.append(") values (");
        for (int c = 1; c <= columnCount; c++) {
            boolean lastColumn = c == columnCount;
            b.append('?');
            if (!lastColumn) {
                b.append(',');
            }
        }
        b.append(")");
        return b.toString();
    }

    public void bind(ResultSet source, PreparedStatement target) throws SQLException {
        for (int c = 1; c <= md.getColumnCount(); c++) {
            target.setObject(c, source.getObject(c));
        }
    }
}
