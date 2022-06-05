package com.github.cimsbioko.server.util;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.sql.SQLXML;

public class JDOMUtil {

    private static final ThreadLocal<SAXBuilder> builderForThread = new ThreadLocal<>();
    private static final ThreadLocal<XMLOutputter> outputterForThread = new ThreadLocal<>();
    private static final ThreadLocal<XMLOutputter> prettyOutputterForThread = new ThreadLocal<>();

    public static Document docFromObj(Object value) throws JDOMException, IOException, SQLException {
        return value == null ? null : getBuilder().build(new StringReader(stringFromObject(value)));
    }

    public static String stringFromObject(Object value) throws SQLException {
        String stringValue;
        if (value instanceof Document) {
            stringValue = stringFromDoc((Document) value);
        } else if (value instanceof SQLXML) {
            stringValue = ((SQLXML) value).getString();
        } else {
            stringValue = value.toString();
        }
        return stringValue;
    }

    public static String stringFromDoc(Document value) {
        return value == null ? null : getOutputter().outputString(value);
    }

    public static String stringFromDoc(Document value, boolean makePretty) {
        return value == null ? null : getOutputter(makePretty, false).outputString(value);
    }

    public static String stringFromDoc(Document value, boolean makePretty, boolean omitDeclaration) {
        return value == null? null : getOutputter(makePretty, omitDeclaration).outputString(value);
    }

    public static SAXBuilder getBuilder() {
        SAXBuilder result = builderForThread.get();
        if (result == null) {
            result = new SAXBuilder();
            builderForThread.set(result);
        }
        return result;
    }

    public static XMLOutputter getOutputter() {
        return getOutputter(false, false);
    }

    public static XMLOutputter getOutputter(boolean makePretty, boolean omitDeclaration) {
        XMLOutputter o;
        if (makePretty) {
            o = prettyOutputterForThread.get();
            if (o == null) {
                prettyOutputterForThread.set(o = new XMLOutputter(Format.getPrettyFormat().setOmitDeclaration(omitDeclaration)));
            }
        } else {
            o = outputterForThread.get();
            if (o == null) {
                outputterForThread.set(o = new XMLOutputter());
            }
        }
        return o;
    }
}
