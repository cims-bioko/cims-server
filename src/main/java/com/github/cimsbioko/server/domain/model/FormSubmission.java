package com.github.cimsbioko.server.domain.model;

import java.sql.Timestamp;

public class FormSubmission {

    private String instanceId;
    private String xml;
    private String json;
    private String formId;
    private String formVersion;
    private String formBinding;
    private String deviceId;
    private Timestamp submitted;

    public FormSubmission(String instanceId, String xml, String json, String formId, String formVersion, String formBinding, String deviceId, Timestamp submitted) {
        this.instanceId = instanceId;
        this.xml = xml;
        this.json = json;
        this.formId = formId;
        this.formVersion = formVersion;
        this.formBinding = formBinding;
        this.deviceId = deviceId;
        this.submitted = submitted;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getXml() {
        return xml;
    }

    public String getJson() {
        return json;
    }

    public String getFormId() {
        return formId;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public String getFormBinding() {
        return formBinding;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Timestamp getSubmitted() {
        return submitted;
    }
}
