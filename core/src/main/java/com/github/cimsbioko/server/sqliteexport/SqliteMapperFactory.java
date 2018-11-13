package com.github.cimsbioko.server.sqliteexport;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * A {@link MapperFactory} that generates {@link Mapper} objects for exporting to an Sqlite database.
 */
public class SqliteMapperFactory implements MapperFactory {

    private TypeExtractor typeExtractor;

    public SqliteMapperFactory(TypeExtractor extractor) {
        this.typeExtractor = extractor;
    }

    @Override
    public Mapper createMapper(ResultSetMetaData md, String table, Connection dst) throws SQLException {
        return new SqliteMapper(md, table, typeExtractor.getTypeMap(dst));
    }

}
