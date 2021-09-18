package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.domain.FormId;
import com.github.cimsbioko.server.service.FormMetadataService;
import org.jdom2.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.Arrays;

import static com.github.cimsbioko.server.util.JDOMUtil.stringFromDoc;

public class DefaultSubmissionJSONConverter implements SubmissionJSONConverter {

    private final FormMetadataService metadataService;

    public DefaultSubmissionJSONConverter(FormMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public JSONObject convert(Document xmlDoc, FormId formId) {
        JSONObject converted = XML.toJSONObject(stringFromDoc(xmlDoc));
        for (String[] path : metadataService.getRepeats(formId)) {
            fixRepeats(converted, path);
        }
        return converted;
    }

    private void fixRepeats(Object obj, String[] path) {
        if (path.length >= 1 && obj instanceof JSONObject) {
            String key = path[0];
            JSONObject parent = (JSONObject) obj;
            if (parent.has(key)) {
                Object value = parent.get(key);
                if (path.length == 1) {
                    if (!(value instanceof JSONArray)) {
                        JSONArray newValue = new JSONArray();
                        newValue.put(0, value);
                        parent.put(key, newValue);
                    }
                } else {
                    if (value instanceof JSONObject) {
                        fixRepeats(value, Arrays.copyOfRange(path, 1, path.length));
                    } else if (value instanceof JSONArray) {
                        JSONArray children = (JSONArray) value;
                        children.forEach(c -> fixRepeats(c, Arrays.copyOfRange(path, 1, path.length)));
                    }
                }
            }
        }
    }
}

