package org.openhds.domain.model;

import org.openhds.domain.annotations.Description;
import org.openhds.domain.constraint.CheckEndDateNotBeforeStartDate;
import org.openhds.domain.constraint.CheckFieldNotBlank;
import org.openhds.domain.constraint.GenericStartEndDateConstraint;
import org.openhds.domain.constraint.Searchable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Description(description = "A Residency represents a home within the study area. " +
        "It contains information about the Individual who lives at the Residency " +
        "which is tied to a particular Location. It also contains information about " +
        "the date the Residency started and ended as well as the start and end types.")
@Entity
@CheckEndDateNotBeforeStartDate(allowNull = true)
@Table(name = "residency")
@XmlRootElement
public class Residency extends AuditableCollectedEntity implements GenericStartEndDateConstraint, Serializable {
    private static final long serialVersionUID = -4666666231598767965L;

    @Searchable
    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "Individual who resides at this residency, identified by external id.")
    Individual individual;

    @Searchable
    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "Location of the residency, identified by external id.")
    Location location;

    @NotNull
    @Past(message = "Insert date should be in the past")
    @Temporal(javax.persistence.TemporalType.DATE)
    @Description(description = "Residency start date.")
    Calendar startDate;

    @CheckFieldNotBlank
    @Description(description = "Residency start type.")
    String startType;

    @Temporal(javax.persistence.TemporalType.DATE)
    @Description(description = "Residency end date.")
    Calendar endDate;

    @Description(description = "Residency end type.")
    String endType;

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getStartType() {
        return startType;
    }

    public void setStartType(String startType) {
        this.startType = startType;
    }

    public String getEndType() {
        return endType;
    }

    public void setEndType(String endType) {
        this.endType = endType;
    }

    public static Residency makeStub(String uuid, Location location, Individual individual) {

        Residency stub = new Residency();
        stub.setUuid(uuid);
        stub.setLocation(location);
        stub.setIndividual(individual);
        return stub;

    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Residency)) {
            return false;
        }

        final String otherUuid = ((Residency) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }

    @XmlRootElement
    public static class Residencies implements Serializable {

        private static final long serialVersionUID = 1L;

        private List<Residency> residencies;

        @XmlElement(name = "residency")
        public List<Residency> getResidencies() {
            return residencies;
        }

        public void setResidencies(List<Residency> copies) {
            this.residencies = copies;
        }
    }

    public static Comparator<Residency> latestByStartDateAndInsertDate() {
        return new Comparator<Residency>() {
            @Override
            public int compare(Residency o1, Residency o2) {
                int byStartDate = (null == o1.startDate || null == o2.startDate) ? 0 : o1.startDate.compareTo(o2.startDate);
                if (0 == byStartDate) {
                    return (null == o1.insertDate || null == o2.insertDate) ? 0 : o1.insertDate.compareTo(o2.insertDate);
                }
                return byStartDate;
            }
        };
    }

    public static Comparator<Residency> earliestByStartDateAndInsertDate() {
        return Collections.reverseOrder(latestByStartDateAndInsertDate());
    }
}