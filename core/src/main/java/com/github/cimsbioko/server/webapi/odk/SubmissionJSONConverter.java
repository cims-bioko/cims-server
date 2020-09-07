package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.domain.FormId;
import org.jdom2.Document;
import org.json.JSONObject;

public interface SubmissionJSONConverter {
    JSONObject convert(Document xmlDoc, FormId formId);
}
