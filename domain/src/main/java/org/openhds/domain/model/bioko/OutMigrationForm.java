package org.openhds.domain.model.bioko;

import org.openhds.domain.annotations.Description;

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


    @XmlElement(name = "processed_by_mirth")
    private boolean processedByMirth;

    @XmlElement(name = "individual_ext_id")
    private String individualExtId;


    @XmlElement(name = "field_worker_ext_id")
    private String fieldWorkerExtId;

    @XmlElement(name = "visit_ext_id")
    private String visitExtId;

    @XmlElement(name = "date_of_migration")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar dateOfMigration;

    @XmlElement(name = "name_of_destination")
    private String nameOfDestination;

    @XmlElement(name = "reason_for_out_migration")
    private String reasonForOutMigration;


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
