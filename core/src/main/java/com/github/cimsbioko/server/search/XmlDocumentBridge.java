package com.github.cimsbioko.server.search;

import com.github.cimsbioko.server.util.JDOMUtil;
import org.hibernate.search.bridge.StringBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class XmlDocumentBridge implements StringBridge {

    private static final Logger log = LoggerFactory.getLogger(XmlDocumentBridge.class);

    @Override
    public String objectToString(Object object) {
        try {
            return JDOMUtil.stringFromObject(object);
        } catch (SQLException e) {
            log.warn("failed to convert document to string", e);
            return "";
        }
    }
}
