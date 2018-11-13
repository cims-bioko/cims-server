package com.github.cimsbioko.server;

import geodb.GeoDB;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class GeoDBTestDataSource extends SingleConnectionDataSource {

    public static final String DRIVER_CLASS = "org.h2.Driver";
    public static final String DATABASE_URL = "jdbc:h2:mem:test";

    public GeoDBTestDataSource() {
        setDriverClassName(DRIVER_CLASS);
        setUrl(DATABASE_URL);
        setSuppressClose(true);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();
        GeoDB.InitGeoDB(conn);
        return conn;
    }
}
