package org.openhds.domain.model.bioko;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.openhds.domain.annotations.Description;

@Description(description = "Model data from the Visit xform for the Bioko island project.")
@XmlRootElement(name = "visitForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisitForm implements Serializable {

	private static final long serialVersionUID = 6052940190094850124L;

    //core form fields
	@XmlElement(name = "processed_by_mirth")
    private boolean processedByMirth;

    @XmlElement(name = "entity_uuid")
    private String uuid;

    @XmlElement(name = "entity_ext_id")
    private String entityExtId;
      
    @XmlElement(name = "field_worker_ext_id")
    private String fieldworkerExtId;

    @XmlElement(name = "field_worker_uuid")
    private String fieldWorkerUuid;

    @XmlElement(name = "collection_date_time")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar collectionDateTime;

    //visit form fields
    @XmlElement(name = "visit_ext_id")
    private String visitExtId;

    @XmlElement(name = "location_ext_id")
    private String locationExtId;

    @XmlElement(name = "visit_date")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar visitDate;

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

	public String getVisitExtId() {
		return visitExtId;
	}

	public void setVisitExtId(String visitExtId) {
		this.visitExtId = visitExtId;
	}

	public String getFieldworkerExtId() {
		return fieldworkerExtId;
	}

	public void setFieldworkerExtId(String fieldworkerExtId) {
		this.fieldworkerExtId = fieldworkerExtId;
	}

	public String getLocationExtId() {
		return locationExtId;
	}

	public void setLocationExtId(String locationExtId) {
		this.locationExtId = locationExtId;
	}

	public Calendar getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Calendar visitDate) {
		this.visitDate = visitDate;
	}
	
}
