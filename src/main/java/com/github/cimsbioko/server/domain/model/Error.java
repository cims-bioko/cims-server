package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.annotations.Description;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Calendar;

@Description(description = "An error log")
@Entity
@Table(name = "error")
@XmlRootElement(name = "error")
public class Error implements Serializable {

    private static final long serialVersionUID = 2447734552586256198L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.domain.util.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    @Temporal(javax.persistence.TemporalType.DATE)
    @Description(description = "Date of insertion.")
    private Calendar insertDate;

    @Column(length = 65535)
    private String dataPayload;

    private String errorMessage;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = FieldWorker.class)
    @Description(description = "The field worker who collected the data, identified by external id.")
    private FieldWorker fieldWorker;

    @Description(description = "Indicator for signaling some data to be deleted.")
    protected boolean deleted = false;

    private String assignedTo;

    @Column
    private String entityType;

    private String resolutionStatus;

    private Calendar dateOfResolution;

    @XmlTransient
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Calendar getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Calendar insertDate) {
        this.insertDate = insertDate;
    }

    public String getDataPayload() {
        return dataPayload;
    }

    public void setDataPayload(String dataPayload) {
        this.dataPayload = dataPayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public FieldWorker getFieldWorker() {
        return fieldWorker;
    }

    public void setFieldWorker(FieldWorker fieldWorker) {
        this.fieldWorker = fieldWorker;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(String resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public Calendar getDateOfResolution() {
        return dateOfResolution;
    }

    public void setDateOfResolution(Calendar dateOfResolution) {
        this.dateOfResolution = dateOfResolution;
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
}
