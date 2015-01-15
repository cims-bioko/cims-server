package org.openhds.domain.model.bioko;

import org.openhds.domain.annotations.Description;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;

@Description(description = "Model data from the PregnancyObservation xform for the Bioko island project.")
@XmlRootElement(name = "pregnancyObservationForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class PregnancyObservationForm implements Serializable{

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

    //PregObs form fields
    @XmlElement(name = "individual_ext_id")
    private String individualExtId;

    @XmlElement(name = "individual_uuid")
    private String individualUuid;

    @XmlElement(name = "visit_ext_id")
    private String visitExtId;

    @XmlElement(name = "visit_uuid")
    private String visitUuid;

    @XmlElement(name = "expected_delivery_date")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar expectedDeliveryDate;

    @XmlElement(name = "recorded_date")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar recordedDate;

    public String getIndividualUuid() {
        return individualUuid;
    }

    public void setIndividualUuid(String individualUuid) {
        this.individualUuid = individualUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getEntityExtId() {
        return entityExtId;
    }

    public void setEntityExtId(String entityExtId) {
        this.entityExtId = entityExtId;
    }

    public Calendar getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(Calendar collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    public String getEntityUuid() {
        return entityUuid;
    }

    public void setEntityUuid(String entityUuid) {
        this.entityUuid = entityUuid;
    }

    public String getFieldWorkerUuid() {
        return fieldWorkerUuid;
    }

    public void setFieldWorkerUuid(String fieldWorkerUuid) {
        this.fieldWorkerUuid = fieldWorkerUuid;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public String getIndividualExtId() {
        return individualExtId;
    }

    public void setIndividualExtId(String individualExtId) {
        this.individualExtId = individualExtId;
    }

    public String getVisitExtId() {
        return visitExtId;
    }

    public void setVisitExtId(String visitExtId) {
        this.visitExtId = visitExtId;
    }

    public Calendar getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Calendar expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public Calendar getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Calendar recordedDate) {
        this.recordedDate = recordedDate;
    }
}
