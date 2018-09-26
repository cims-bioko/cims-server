package com.github.cimsbioko.server.task.sqlite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"/exporter-test-context.xml"})
public class ExporterTest {

    @Autowired
    private DataSource ds;

    @Autowired
    private Exporter exporter;

    @Autowired
    private SqliteTarget target;

    private File file = new File("exported.db");

    private String destTable = "dest_table";


    @Before
    public void setUp() throws SQLException, IOException {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("create table source_table (f1 INT, f2 BOOLEAN, f3 VARCHAR, f4 DATE, f5 TIMESTAMP)");
            s.executeUpdate("insert into source_table VALUES ('1', 'true', 'Hello', current_date(), current_timestamp())");
            s.executeUpdate("insert into source_table VALUES ('2', 'false', 'Goodbye', current_date(), current_time())");
        }
        try (Connection c = target.createConnection(file, true); Statement s = c.createStatement()) {
            s.executeUpdate(String.format("create table %s (f1 INT, f2 BOOLEAN, f3 VARCHAR, f4 DATE, f5 TIMESTAMP)", destTable));
        }
    }

    @After
    public void tearDown() throws SQLException {
        assertTrue("failed to delete generated database", file.delete());
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("drop table source_table");
        }
    }

    @Test
    public void testExporter() throws SQLException, IOException {
        exporter.export("select * from source_table", destTable, file);
        assertTrue(file.exists());
        assertDatabase(file, destTable);
    }

    private void assertDatabase(File file, String destTable) throws IOException, SQLException {
        try (Connection c = target.createConnection(file, true); Statement s = target.createStatement(c);
             ResultSet results = s.executeQuery("select * from " + destTable)) {
            ResultSetMetaData md = results.getMetaData();
            int rowCount = 0;
            assertEquals(5, md.getColumnCount());
            while (results.next()) {
                for (int col = 1; col <= md.getColumnCount(); col++) {
                    assertNotNull(results.getObject(col));
                }
                rowCount++;
            }
            assertEquals(2, rowCount);
        }
    }
}
