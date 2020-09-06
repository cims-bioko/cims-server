package com.github.cimsbioko.server.webapi.odk;

import org.jdom2.JDOMException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static com.github.cimsbioko.server.util.JDOMUtil.docFromObj;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultJSONSubmissionConverterTest {

    private static SubmissionJSONConverter converter;

    @BeforeClass
    public static void oneTimeSetup() {
        converter = new DefaultSubmissionJSONConverter();
    }

    private JSONObject convert(String s) throws JDOMException, SQLException, IOException {
        return converter.convert(docFromObj(s));
    }

    @Test
    public void emptyConversion() throws JDOMException, SQLException, IOException {
        assertNotNull(convert("<data></data>"));
    }

    @Test
    public void basicConversion() throws JDOMException, SQLException, IOException {
        JSONObject result = convert("<field>name</field>");
        assertEquals("name", result.getString("field"));
    }

    @Test
    public void multipleFieldConversion() throws JDOMException, SQLException, IOException {
        JSONObject result = convert("<data><field1>value1</field1><field2>value2</field2></data>");
        JSONObject data = result.getJSONObject("data");
        assertEquals("value1", data.getString("field1"));
        assertEquals("value2", data.getString("field2"));
    }

    @Test
    public void multipleFieldsNestedConversion() throws JDOMException, SQLException, IOException {
        JSONObject result = convert("<data><field1>value1</field1><field2>value2</field2></data>");
        JSONObject data = result.getJSONObject("data");
        assertEquals("value1", data.getString("field1"));
        assertEquals("value2", data.getString("field2"));
    }

    @Test
    public void repeatedFieldsNestedConversion() throws JDOMException, SQLException, IOException {
        JSONObject result = convert("<data>" +
                "<field1>value1</field1><field2>value2</field2>" +
                "<field1>value3</field1><field2>value4</field2>" +
                "</data>");
        JSONObject data = result.getJSONObject("data");
        JSONArray field1 = data.getJSONArray("field1");
        JSONArray field2 = data.getJSONArray("field2");
        assertEquals("value1", field1.getString(0));
        assertEquals("value3", field1.getString(1));
        assertEquals("value2", field2.getString(0));
        assertEquals("value4", field2.getString(1));
    }
}
