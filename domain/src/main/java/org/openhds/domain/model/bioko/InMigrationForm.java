package org.openhds.domain.model.bioko;

import org.openhds.domain.annotations.Description;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;

@Description(description = "Model data from the InMigration xform for the Bioko island project.")
@XmlRootElement(name = "inMigrationForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class InMigrationForm implements Serializable {

    private static final long serialVersionUID = 1L;

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

    //InMigr form fields
    @XmlElement(name = "visit_ext_id")
    private String visitExtId;

    @XmlElement(name = "visit_uuid")
    private String visitUuid;


    @XmlElement(name = "location_ext_id")
    private String locationExtId;

    @XmlElement(name = "location_uuid")
    private String locationUuid;

    @XmlElement(name = "individual_ext_id")
    private String individualExtId;

    @XmlElement(name = "individual_uuid")
    private String individualUuid;

    @XmlElement(name = "migration_type")
    private String migrationType;

    @XmlElement(name = "migration_date")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar migrationDate;

    @XmlElement(name = "migration_origin")
    private String migrationOrigin;

    @XmlElement(name = "migration_reason")
    private String migrationReason;

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

    public boolean isProcessedByMirth() {
        return processedByMirth;
    }

    public void setProcessedByMirth(boolean processedByMirth) {
        this.processedByMirth = processedByMirth;
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

    public String getMigrationType() {
        return migrationType;
    }

    public void setMigrationType(String migrationType) {
        this.migrationType = migrationType;
    }

    public String getIndividualExtId() {
        return individualExtId;
    }

    public void setIndividualExtId(String individualExtId) {
        this.individualExtId = individualExtId;
    }

    public Calendar getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(Calendar collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    public String getLocationExtId() {
        return locationExtId;
    }

    public void setLocationExtId(String locationExtId) {
        this.locationExtId = locationExtId;
    }

    public Calendar getMigrationDate() {
        return migrationDate;
    }

    public void setMigrationDate(Calendar migrationDate) {
        this.migrationDate = migrationDate;
    }

    public String getMigrationOrigin() {
        return migrationOrigin;
    }

    public void setMigrationOrigin(String migrationOrigin) {
        this.migrationOrigin = migrationOrigin;
    }

    public String getMigrationReason() {
        return migrationReason;
    }

    public void setMigrationReason(String migrationReason) {
        this.migrationReason = migrationReason;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getIndividualUuid() {
        return individualUuid;
    }

    public void setIndividualUuid(String individualUuid) {
        this.individualUuid = individualUuid;
    }
}
