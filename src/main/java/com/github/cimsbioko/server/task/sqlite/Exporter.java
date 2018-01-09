package com.github.cimsbioko.server.task.sqlite;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Exporter {

    private Source src;
    private Target dst;
    private MapperFactory mapperFactory;


    public Exporter(Source source, Target target, MapperFactory mapperFactory) {
        this.src = source;
        this.dst = target;
        this.mapperFactory = mapperFactory;
    }

    public void export(String query, String table, File target) throws SQLException, IOException {
        try (Connection src = this.src.createConnection(); Statement s = this.src.createStatement(src);
             Connection dst = this.dst.createConnection(target, false); Statement d = this.dst.createStatement(dst);
             ResultSet rs = s.executeQuery(query)) {
            ResultSetMetaData md = rs.getMetaData();
            Mapper mapper = mapperFactory.createMapper(md, table, dst);
            try (PreparedStatement di = dst.prepareStatement(mapper.getInsertDml())) {
                while (rs.next()) {
                    mapper.bind(rs, di);
                    di.executeUpdate();
                }
                dst.commit();
            }
        }
    }

    public void scriptTarget(InputStream script, File target) throws SQLException, IOException {
        if (script != null) {
            BufferedReader ddlCmds = new BufferedReader(new InputStreamReader(script));
            try (Connection dst = this.dst.createConnection(target, true); Statement d = this.dst.createStatement(dst)) {
                String ddlCmd;
                while ((ddlCmd = ddlCmds.readLine()) != null && ddlCmd.trim().length() > 0) {
                    d.executeUpdate(ddlCmd);
                }
            }
        }
    }
}


