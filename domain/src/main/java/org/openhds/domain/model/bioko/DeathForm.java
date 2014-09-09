package org.openhds.domain.model.bioko;

import org.openhds.domain.annotations.Description;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;

@Description(description = "Model data from the Death xform for the Bioko island project.")
@XmlRootElement(name = "deathForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeathForm implements Serializable{

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "processed_by_mirth")
    private boolean processedByMirth;

    @XmlElement(name = "field_worker_ext_id")
    private String fieldWorkerExtId;

    @XmlElement(name = "collection_date_time")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar collectionDateTime;

    @XmlElement(name = "individual_ext_id")
    private String individualExtId;

    @XmlElement(name = "visit_ext_id")
    private String visitExtId;

    @XmlElement(name = "date_of_death")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar dateOfDeath;

    @XmlElement(name = "place_of_death")
    private String placeOfDeath;

    @XmlElement(name = "cause_of_death")
    private String causeOfDeath;

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

    public Calendar getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(Calendar collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
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

    public Calendar getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(Calendar dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public String getPlaceOfDeath() {
        return placeOfDeath;
    }

    public void setPlaceOfDeath(String placeOfDeath) {
        this.placeOfDeath = placeOfDeath;
    }

    public String getCauseOfDeath() {
        return causeOfDeath;
    }

    public void setCauseOfDeath(String causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
    }
}
