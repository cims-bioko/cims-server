package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.constraint.Searchable;
import com.github.cimsbioko.server.domain.annotations.Description;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;

@Description(description = "A Residency represents a home within the study area. " +
        "It contains information about the Individual who lives at the Residency " +
        "which is tied to a particular Location. It also contains information about " +
        "the date the Residency started and ended as well as the start and end types.")
@Entity
@Table(name = "residency")
@XmlRootElement
public class Residency extends AuditableCollectedEntity implements Serializable {
    private static final long serialVersionUID = -4666666231598767965L;

    @Searchable
    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "Individual who resides at this residency, identified by external id.")
    Individual individual;

    @Searchable
    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "Location of the residency, identified by external id.")
    Location location;

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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

    public static Comparator<Residency> latestByInsertDate() {
        return (o1, o2) -> (null == o1.insertDate || null == o2.insertDate) ? 0 : o1.insertDate.compareTo(o2.insertDate);
    }

    public static Comparator<Residency> earliestByInsertDate() {
        return Collections.reverseOrder(latestByInsertDate());
    }
}