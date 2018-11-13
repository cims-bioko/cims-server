package com.github.cimsbioko.server.sqliteexport;

import java.io.*;
import java.sql.*;

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
             Connection dst = this.dst.createConnection(target, false);
             ResultSet rs = s.executeQuery(query)) {
            ResultSetMetaData md = rs.getMetaData();
            Mapper mapper = mapperFactory.createMapper(md, table, dst);
            try (PreparedStatement d = dst.prepareStatement(mapper.getInsertDml())) {
                while (rs.next()) {
                    mapper.bind(rs, d);
                    d.executeUpdate();
                }
                dst.commit();
            }
        }
    }

    public void scriptTarget(InputStream script, File target) throws SQLException, IOException {
        if (script != null) {
            BufferedReader ddlCmds = new BufferedReader(new InputStreamReader(script));
            try (Connection dst = this.dst.createConnection(target, true);
                 Statement d = this.dst.createStatement(dst)) {
                String ddlCmd;
                while ((ddlCmd = ddlCmds.readLine()) != null && ddlCmd.trim().length() > 0) {
                    d.executeUpdate(ddlCmd);
                }
            }
        }
    }
}


