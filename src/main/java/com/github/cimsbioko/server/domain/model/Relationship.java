package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.constraint.CheckIndividualNotUnknown;
import com.github.cimsbioko.server.domain.constraint.Searchable;
import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.constraint.CheckEntityNotVoided;
import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;
import com.github.cimsbioko.server.domain.constraint.ExtensionStringConstraint;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Description(description = "A Relationship is used to associate an Individual "
        + "with another Indivual in some way. It can be identified by a uniquely "
        + "generated identifier which the system uses internally. It contains "
        + "information about the two Indivuals involved, the start and end dates, "
        + "and the start and end types of the Relationship.")
@Entity
@Table(name = "relationship")
@XmlRootElement
public class Relationship extends AuditableCollectedEntity implements Serializable {

    static final long serialVersionUID = 19L;

    @Searchable
    @CheckEntityNotVoided
    @CheckIndividualNotUnknown
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Description(description = "One of the individuals participating in the relationship, identified by external id.")
    Individual individualA;

    @Searchable
    @CheckEntityNotVoided
    @CheckIndividualNotUnknown
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Description(description = "One of the individuals participating in the relationship, identified by external id.")
    Individual individualB;

    @CheckFieldNotBlank
    @ExtensionStringConstraint(constraint = "relationshipTypeConstraint", message = "Invalid value for relationship type", allowNull = false)
    @Description(description = "Relationship type.")
    String aIsToB;

    public Individual getIndividualA() {
        return individualA;
    }

    public Individual getIndividualB() {
        return individualB;
    }

    public void setIndividualB(Individual individualB) {
        this.individualB = individualB;
    }

    public void setIndividualA(Individual individualA) {
        this.individualA = individualA;
    }

    public String getaIsToB() {
        return aIsToB;
    }

    public void setaIsToB(String aIsToB) {
        this.aIsToB = aIsToB;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Relationship)) {
            return false;
        }

        final String otherUuid = ((Relationship) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }
}
