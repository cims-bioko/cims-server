package com.github.cimsbioko.server.search;

import com.github.cimsbioko.server.domain.FormId;
import org.hibernate.search.bridge.TwoWayStringBridge;

public class FormIdBridge implements TwoWayStringBridge {

    private static final String SEPARATOR = "]]~[[";
    private static final String SEPARATOR_PATTERN = "\\]\\]~\\[\\[";

    @Override
    public Object stringToObject(String stringValue) {
       String[] parts = stringValue.split(SEPARATOR_PATTERN);
       return new FormId(parts[0], parts[1]);
    }

    @Override
    public String objectToString(Object object) {
        FormId id = (FormId) object;
        return id.getId() + SEPARATOR + id.getVersion();
    }
}
