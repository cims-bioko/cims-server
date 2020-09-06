package com.github.cimsbioko.server.webapi.odk;

import org.jdom2.Document;
import org.json.JSONObject;
import org.json.XML;

import static com.github.cimsbioko.server.util.JDOMUtil.stringFromDoc;

public class DefaultSubmissionJSONConverter implements SubmissionJSONConverter {
    @Override
    public JSONObject convert(Document xmlDoc) {
        return XML.toJSONObject(stringFromDoc(xmlDoc));
    }
}
