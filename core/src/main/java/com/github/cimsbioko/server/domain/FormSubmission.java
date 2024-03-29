package com.github.cimsbioko.server.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.cimsbioko.server.search.XmlDocumentBridge;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.jdom2.Document;
import org.json.JSONObject;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "form_submission")
@DynamicInsert
@Indexed
public class FormSubmission {

    @Id
    @Column(name = "instanceid")
    private String instanceId;

    @Column(name = "form_id")
    private String formId;

    @Column(name = "form_version")
    private String formVersion;

    @Column(name = "form_binding")
    private String formBinding;

    @Column(name = "campaign")
    private String campaignId;

    @Column(name = "from_device")
    private String deviceId;

    @JsonIgnore
    @Type(type = "xml")
    @Column(name = "as_xml")
    @Field
    @FieldBridge(impl = XmlDocumentBridge.class)
    private Document xml;

    @JsonIgnore
    @Type(type = "json")
    @Column(name = "as_json")
    private JSONObject json;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Timestamp collected, submitted, processed;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(name = "processed_ok")
    private Boolean processedOk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deprecated_by")
    private FormSubmission deprecatedBy;

    public FormSubmission() {
    }

    public FormSubmission(String instanceId, Document xml, JSONObject json, String formId, String formVersion,
                          String formBinding, String campaignId, String deviceId, Timestamp collected, Timestamp submitted, Timestamp processed,
                          Boolean processedOk, FormSubmission deprecatedBy) {
        this.instanceId = instanceId;
        this.xml = xml;
        this.json = json;
        this.formId = formId;
        this.formVersion = formVersion;
        this.formBinding = formBinding;
        this.campaignId = campaignId;
        this.deviceId = deviceId;
        this.collected = collected;
        this.submitted = submitted;
        this.processed = processed;
        this.processedOk = processedOk;
        this.deprecatedBy = deprecatedBy;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Document getXml() {
        return xml;
    }

    public JSONObject getJson() {
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

    public String getCampaignId() {
        return campaignId;
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

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    public void setFormBinding(String formBinding) {
        this.formBinding = formBinding;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setXml(Document xml) {
        this.xml = xml;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public void setCollected(Timestamp collected) {
        this.collected = collected;
    }

    public void setSubmitted(Timestamp submitted) {
        this.submitted = submitted;
    }

    public void setProcessed(Timestamp processed) {
        this.processed = processed;
    }

    public void setProcessedOk(Boolean processedOk) {
        this.processedOk = processedOk;
    }

    public FormSubmission getDeprecatedBy() {
        return deprecatedBy;
    }

    public void setDeprecatedBy(FormSubmission deprecatedBy) {
        this.deprecatedBy = deprecatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormSubmission that = (FormSubmission) o;
        return getInstanceId().equals(that.getInstanceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstanceId());
    }
}
