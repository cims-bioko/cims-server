
package org.openhds.domain.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.openhds.domain.annotations.Description;
import org.openhds.domain.constraint.CheckDropdownMenuItemSelected;
import org.openhds.domain.constraint.CheckFieldNotBlank;
import org.openhds.domain.constraint.CheckInMigrationAfterDob;
import org.openhds.domain.constraint.CheckIndividualNotUnknown;
import org.openhds.domain.constraint.Searchable;


/**
 * Generated by JCodeModel
 */
@Description(description = "An InMigration represents a migration into the study area. It contains information about the Individual who is in-migrating to a particular Residency. It also contains information about the origin, date, and reason the Indiviudal is migrating as well as the Visit that is associated with the migration.")
@Entity
@CheckDropdownMenuItemSelected
@CheckInMigrationAfterDob
@Table(name = "inmigration")
@XmlRootElement(name = "inmigration")
public class InMigration
        extends AuditableCollectedEntity
        implements Serializable {

    public final static long serialVersionUID = 7889700709284952892L;
    @NotNull
    @Searchable
    @ManyToOne
    @CheckIndividualNotUnknown
    @Description(description = "Individual who is inmigrating, identified by external id.")
    private Individual individual;
    @OneToOne(cascade = {CascadeType.ALL})
    @NotNull
    @Description(description = "The residency the individual is inmigrating to.")
    private Residency residency = new Residency();
    @Searchable
    @CheckFieldNotBlank
    @Description(description = "Name origin of the inmigration.")
    private String origin;
    @Searchable
    @CheckFieldNotBlank
    @Description(description = "The reason for inmigrating.")
    private String reason;
    @NotNull
    @Temporal(TemporalType.DATE)
    @Description(description = "Recorded date of the inmigration.")
    private Calendar recordedDate;
    @Searchable
    @NotNull
    @ManyToOne(cascade = CascadeType.MERGE)
    @Description(description = "The visit associated with the inmigration, identified by external id.")
    private Visit visit;
    @Enumerated(EnumType.STRING)
    @Description(description = "The migration type.")
    private MigrationType migType = MigrationType.INTERNAL_INMIGRATION;
    @Description(description = "Flag for indicating if the individual who is inmigrating is known or not.")
    private Boolean unknownIndividual = false;

    public Individual getIndividual() {
        //String extId=individual.getExtId();
        return individual;
    }

    public void setIndividual(Individual indiv) {
        individual = indiv;
    }

    public Residency getResidency() {
        return residency;
    }

    public void setResidency(Residency res) {
        residency = res;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String name) {
        origin = name;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String name) {
        reason = name;
    }

    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    public Calendar getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Calendar date) {
        recordedDate = date;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit vis) {
        visit = vis;
    }

    public MigrationType getMigType() {
        return migType;
    }

    public void setMigType(MigrationType mig) {
        migType = mig;
    }

    public Boolean isUnknownIndividual() {
        return unknownIndividual;
    }

    public void setUnknownIndividual(Boolean flag) {
        unknownIndividual = flag;
    }

    public void setMigTypeInternal() {
        setMigType(MigrationType.INTERNAL_INMIGRATION);
    }

    public void setMigTypeExternal() {
        setMigType(MigrationType.EXTERNAL_INMIGRATION);
    }

}