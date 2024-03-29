package com.github.cimsbioko.server.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(name = "error")
@DynamicInsert
public class Error implements Serializable {

    private static final long serialVersionUID = 2447734552586256198L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.hibernate.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar created;

    @Column(length = 65535)
    private String payload;

    @Column(name = "entitytype")
    private String entityType;

    private String message;

    @ManyToOne
    @JoinColumn(name = "submission")
    private FormSubmission submission;

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public FormSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(FormSubmission submission) {
        this.submission = submission;
    }
}
