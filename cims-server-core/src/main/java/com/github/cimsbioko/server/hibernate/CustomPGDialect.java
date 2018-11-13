package com.github.cimsbioko.server.hibernate;

import org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect;

import java.sql.Types;

public class CustomPGDialect extends PostgisPG95Dialect {
    public CustomPGDialect() {
        registerColumnType(Types.OTHER, "jsonb");
    }
}
