package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.domain.FormSubmission;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultOpenRosaResponseBuilder implements OpenRosaResponseBuilder {

    @Autowired
    DateFormatter dateFormatter;

    @Override
    public String response(String message) {
        return String.format(
                "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\"><message>%s</message></OpenRosaResponse>",
                message);
    }

    @Override
    public String submissionResponse(FormSubmission fs) {
        return String.format(
                "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" +
                        "<message>full submission upload was successful!</message>" +
                        "<submissionMetadata xmlns=\"http://www.opendatakit.org/xforms\"" +
                        "id=\"%s\" instanceID=\"%s\" version=\"%s\" submissionDate=\"%s\"/>" +
                        "</OpenRosaResponse>",
                fs.getFormId(), fs.getInstanceId(), fs.getFormVersion(), dateFormatter.formatSubmitDate(fs.getSubmitted()));
    }
}
