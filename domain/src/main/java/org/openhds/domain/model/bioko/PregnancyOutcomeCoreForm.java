package org.openhds.domain.model.bioko;

import org.openhds.domain.annotations.Description;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Calendar;

/**
 * Created by motech on 4/9/15.
 */

@Description(description = "Model data from the PregnancyOutcome form for the Bioko project. Additional Outcome data is contained in PregnancyOutcomeOutcomesForm")
@XmlRootElement(name = "pregnancyOutcomeCoreForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class PregnancyOutcomeCoreForm {

    //core form fields
    @XmlElement(name = "entity_uuid")
    private String uuid;

    @XmlElement(name = "entity_ext_id")
    private String entityExtId;

    @XmlElement(name = "processed_by_mirth")
    private boolean processedByMirth;

    @XmlElement(name = "field_worker_ext_id")
    private String fieldWorkerExtId;

    @XmlElement(name = "field_worker_uuid")
    private String fieldWorkerUuid;

    @XmlElement(name = "collection_date_time")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar collectionDateTime;

    @XmlElement(name = "visit_uuid")
    private String visitUuid;

    @XmlElement(name = "mother_uuid")
    private String motherUuid;

    @XmlElement(name = "father_uuid")
    private String fatherUuid;

    @XmlElement(name = "social_group_uuid")
    private String socialGroupUuid;

    @XmlElement(name = "number_of_outcomes")
    private String numberOfOutcomes;

    @XmlElement(name = "delivery_date")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar deliveryDate;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEntityExtId() {
        return entityExtId;
    }

    public void setEntityExtId(String entityExtId) {
        this.entityExtId = entityExtId;
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

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getMotherUuid() {
        return motherUuid;
    }

    public void setMotherUuid(String motherUuid) {
        this.motherUuid = motherUuid;
    }

    public String getFatherUuid() {
        return fatherUuid;
    }

    public void setFatherUuid(String fatherUuid) {
        this.fatherUuid = fatherUuid;
    }

    public String getSocialGroupUuid() {
        return socialGroupUuid;
    }

    public void setSocialGroupUuid(String socialGroupUuid) {
        this.socialGroupUuid = socialGroupUuid;
    }

    public String getNumberOfOutcomes() {
        return numberOfOutcomes;
    }

    public void setNumberOfOutcomes(String numberOfOutcomes) {
        this.numberOfOutcomes = numberOfOutcomes;
    }

    public Calendar getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Calendar deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
