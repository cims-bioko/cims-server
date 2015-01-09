package org.openhds.domain.model.bioko;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;


@XmlRootElement(name = "outMigrationForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class OutMigrationForm implements Serializable {

    private static final long serialVersionUID = 4321517330340385847L;

    //core form fields
    @XmlElement(name = "processed_by_mirth")
    private boolean processedByMirth;

    @XmlElement(name = "entity_uuid")
    private String entityUuid;

    @XmlElement(name = "entity_ext_id")
    private String entityExtId;

    @XmlElement(name = "field_worker_ext_id")
    private String fieldWorkerExtId;

    @XmlElement(name = "field_worker_uuid")
    private String fieldWorkerUuid;

    @XmlElement(name = "collection_date_time")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar collectionDateTime;

    //OutMigr form fields
    @XmlElement(name = "out_migration_individual_ext_id")
    private String individualExtId;

    @XmlElement(name = "visit_ext_id")
    private String visitExtId;

    @XmlElement(name = "out_migration_date")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar dateOfMigration;

    @XmlElement(name = "out_migration_name_of_destination")
    private String nameOfDestination;

    @XmlElement(name = "out_migration_reason")
    private String reasonForOutMigration;

    public String getEntityUuid() {
        return entityUuid;
    }

    public void setEntityUuid(String entityUuid) {
        this.entityUuid = entityUuid;
    }

    public String getEntityExtId() {
        return entityExtId;
    }

    public void setEntityExtId(String entityExtId) {
        this.entityExtId = entityExtId;
    }

    public String getFieldWorkerUuid() {
        return fieldWorkerUuid;
    }

    public void setFieldWorkerUuid(String fieldWorkerUuid) {
        this.fieldWorkerUuid = fieldWorkerUuid;
    }

    public Calendar getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(Calendar collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    public boolean isProcessedByMirth() {
        return processedByMirth;
    }

    public void setProcessedByMirth(boolean processedByMirth) {
        this.processedByMirth = processedByMirth;
    }

    public String getIndividualExtId() {
        return individualExtId;
    }

    public void setIndividualExtId(String individualExtId) {
        this.individualExtId = individualExtId;
    }

    public String getFieldWorkerExtId() {
        return fieldWorkerExtId;
    }

    public void setFieldWorkerExtId(String fieldWorkerExtId) {
        this.fieldWorkerExtId = fieldWorkerExtId;
    }

    public String getVisitExtId() {
        return visitExtId;
    }

    public void setVisitExtId(String visitExtId) {
        this.visitExtId = visitExtId;
    }

    public Calendar getDateOfMigration() {
        return dateOfMigration;
    }

    public void setDateOfMigration(Calendar dateOfMigration) {
        this.dateOfMigration = dateOfMigration;
    }

    public String getNameOfDestination() {
        return nameOfDestination;
    }

    public void setNameOfDestination(String nameOfDestination) {
        this.nameOfDestination = nameOfDestination;
    }

    public String getReasonForOutMigration() {
        return reasonForOutMigration;
    }

    public void setReasonForOutMigration(String reasonForOutMigration) {
        this.reasonForOutMigration = reasonForOutMigration;
    }
}
