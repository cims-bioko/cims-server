package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.domain.FormId;
import com.github.cimsbioko.server.service.FormService;
import org.jdom2.JDOMException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static com.github.cimsbioko.server.util.JDOMUtil.docFromObj;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJSONSubmissionConverterTest {

    private static final FormId DEFAULT_FORM_ID = new FormId("default", "1");

    private SubmissionJSONConverter converter;

    @Mock
    private FormService formService;

    @Before
    public void setup() {
        converter = new DefaultSubmissionJSONConverter(formService);
        when(formService.getRepeatPaths(DEFAULT_FORM_ID)).thenReturn(emptyList());
    }

    private JSONObject convert(String s) throws JDOMException, SQLException, IOException {
        return converter.convert(docFromObj(s), DEFAULT_FORM_ID);
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

    @Test
    public void singleRepeat() throws JDOMException, SQLException, IOException {
        String[] repeatPath = {"data", "field1"};
        List<String[]> repeatPaths = Collections.singletonList(repeatPath);
        when(formService.getRepeatPaths(DEFAULT_FORM_ID)).thenReturn(repeatPaths);
        JSONObject result = convert("<data><field1>value1</field1><field2>value2</field2></data>");
        JSONArray field1s = result.getJSONObject("data").getJSONArray("field1");
        assertEquals(1, field1s.length());
        assertEquals("value1", field1s.getString(0));
        assertEquals("value2", result.getJSONObject("data").getString("field2"));
    }

    @Test
    public void nestedRepeat() throws JDOMException, SQLException, IOException {
        String[] repeat1 = {"data", "house", "person"};
        String[] repeat2 = {"data", "house"};
        List<String[]> repeatPaths = asList(repeat1, repeat2);
        when(formService.getRepeatPaths(DEFAULT_FORM_ID)).thenReturn(repeatPaths);
        JSONObject result = convert("<data><house><person>value1</person></house></data>");
        JSONArray houses = result.getJSONObject("data").getJSONArray("house");
        assertEquals(1, houses.length());
        JSONArray people = houses.getJSONObject(0).getJSONArray("person");
        assertEquals(1, people.length());
        assertEquals("value1", people.getString(0));
    }

    @Test
    public void repeatUnderArray() throws JDOMException, SQLException, IOException {
        String[] repeat1 = {"data", "house", "person"};
        List<String[]> repeatPaths = Collections.singletonList(repeat1);
        when(formService.getRepeatPaths(DEFAULT_FORM_ID)).thenReturn(repeatPaths);
        JSONObject result = convert("<data>" +
                "<house><person>value1</person><person>value2</person></house>" +
                "<house><person>value3</person></house>" +
                "</data>");
        JSONArray houses = result.getJSONObject("data").getJSONArray("house");
        assertEquals(2, houses.length());
        JSONArray people1 = houses.getJSONObject(0).getJSONArray("person");
        assertEquals(2, people1.length());
        assertEquals("value1", people1.getString(0));
        JSONArray people2 = houses.getJSONObject(1).getJSONArray("person");
        assertEquals(1, people2.length());
        assertEquals("value3", people2.getString(0));
    }
}