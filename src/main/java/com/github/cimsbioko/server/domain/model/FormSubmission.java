package com.github.cimsbioko.server.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

public class FormSubmission {

    private String instanceId;
    private String formId;
    private String formVersion;
    private String formBinding;
    private String deviceId;

    @JsonIgnore
    private String xml, json;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Timestamp collected, submitted, processed;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean processedOk;

    public FormSubmission(String instanceId, String xml, String json, String formId, String formVersion,
                          String formBinding, String deviceId, Timestamp collected, Timestamp submitted, Timestamp processed,
                          Boolean processedOk) {
        this.instanceId = instanceId;
        this.xml = xml;
        this.json = json;
        this.formId = formId;
        this.formVersion = formVersion;
        this.formBinding = formBinding;
        this.deviceId = deviceId;
        this.collected = collected;
        this.submitted = submitted;
        this.processed = processed;
        this.processedOk = processedOk;
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

    public Timestamp getCollected() {
        return collected;
    }

    public Timestamp getSubmitted() {
        return submitted;
    }

    public Timestamp getProcessed() {
        return processed;
    }

    public Boolean getProcessedOk() {
        return processedOk;
    }
}
