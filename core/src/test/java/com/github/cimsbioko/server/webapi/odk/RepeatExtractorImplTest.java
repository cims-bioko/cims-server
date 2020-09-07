package com.github.cimsbioko.server.webapi.odk;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static com.github.cimsbioko.server.util.JDOMUtil.docFromObj;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RepeatExtractorImplTest {

    private static RepeatExtractor extractor;

    @BeforeClass
    public static void setupOnce() {
        extractor = new RepeatExtractorImpl();
    }

    @Test
    public void textExtract() throws JDOMException, SQLException, IOException {
        Document doc = docFromObj("<h:html " +
                "xmlns:h=\"http://www.w3.org/1999/xhtml\" " +
                "xmlns=\"http://www.w3.org/2002/xforms\" " +
                "xmlns:ev=\"http://www.w3.org/2001/xml-events\" " +
                "xmlns:jr=\"http://openrosa.org/javarosa\" " +
                "xmlns:odk=\"http://www.opendatakit.org/xforms\" " +
                "xmlns:orx=\"http://openrosa.org/xforms\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                "<h:body>" +
                "<group ref=\"/data/individual\">" +
                "<label ref=\"jr:itext('/data/individual:label')\" />" +
                "<repeat jr:count=\" /data/individual_count \" nodeset=\"/data/individual\">" +
                "</repeat>" +
                "</group>" +
                "</h:body>" +
                "</h:html>");
        List<String[]> repeatPaths = extractor.extractRepeats(doc);
        assertEquals(1, repeatPaths.size());
        assertArrayEquals(new String[]{"data", "individual"}, repeatPaths.get(0));
    }

}
