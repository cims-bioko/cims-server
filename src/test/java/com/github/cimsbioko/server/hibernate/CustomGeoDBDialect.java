package com.github.cimsbioko.server.hibernate;

import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;

import java.sql.Types;

public class CustomGeoDBDialect extends GeoDBDialect {
    public CustomGeoDBDialect() {
        registerColumnType(Types.OTHER, "other");
        registerColumnType(Types.SQLXML, "other");
    }
}
