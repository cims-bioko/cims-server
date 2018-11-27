package com.github.cimsbioko.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.jdom2.Document;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "form")
@DynamicInsert
public class Form {

    @EmbeddedId
    private FormId formId;

    @Type(type = "xml")
    @Column(name = "as_xml")
    @JsonIgnore
    private Document xml;

    private boolean downloads = true;

    private boolean submissions = true;

    private Timestamp uploaded;

    @Column(name = "last_submission")
    private Timestamp lastSubmission;

    public Form() {
    }

    public Form(FormId formId, Document xml) {
        this.formId = formId;
        this.xml = xml;
    }

    public FormId getFormId() {
        return formId;
    }

    public void setFormId(FormId formId) {
        this.formId = formId;
    }

    public Document getXml() {
        return xml;
    }

    public void setXml(Document xml) {
        this.xml = xml;
    }

    public boolean isDownloads() {
        return downloads;
    }

    public void setDownloads(boolean downloads) {
        this.downloads = downloads;
    }

    public boolean isSubmissions() {
        return submissions;
    }

    public void setSubmissions(boolean submissions) {
        this.submissions = submissions;
    }

    public Timestamp getUploaded() {
        return uploaded;
    }

    public void setUploaded(Timestamp uploaded) {
        this.uploaded = uploaded;
    }

    public void setLastSubmission(Timestamp lastSubmission) {
        this.lastSubmission = lastSubmission;
    }

    public Timestamp getLastSubmission() {
        return lastSubmission;
    }
}
