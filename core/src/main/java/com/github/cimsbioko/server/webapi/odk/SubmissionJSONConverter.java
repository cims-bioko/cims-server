package com.github.cimsbioko.server.webapi.odk;

import org.jdom2.Document;
import org.json.JSONObject;

public interface SubmissionJSONConverter {
    JSONObject convert(Document xmlDoc);
}
