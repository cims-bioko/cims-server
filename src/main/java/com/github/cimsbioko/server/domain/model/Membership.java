package com.github.cimsbioko.server.domain.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.github.cimsbioko.server.domain.constraint.*;
import com.github.cimsbioko.server.domain.annotations.Description;

@Description(description = "A Membership represents an Individual's association with a " +
        "particular Social Group. Memberships are identified by a uniquely generated " +
        "identifier which the system uses internally. It contains information " +
        "about the date the Membership started and ended, as well as the start and end types. " +
        "It also contains the Individual's relationship to the head of the Social Group.")
@Entity
@Table(name = "membership")
@XmlRootElement(name = "membership")
public class Membership extends AuditableCollectedEntity implements Serializable {

    private static final long serialVersionUID = 6200055042380700627L;

    @Searchable
    @CheckEntityNotVoided
    @CheckIndividualNotUnknown
    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "Individual the membership is associated with, identified by external id.")
    Individual individual;

    @Searchable
    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "The social group of the membership, identified by external id.")
    SocialGroup socialGroup;

    @ExtensionStringConstraint(constraint = "membershipConstraint", message = "Invalid Value for membership relation to head", allowNull = true)
    @Description(description = "Relationship type to the group head.")
    String bIsToA;

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public SocialGroup getSocialGroup() {
        return socialGroup;
    }

    public void setSocialGroup(SocialGroup socialGroup) {
        this.socialGroup = socialGroup;
    }

    public String getbIsToA() {
        return bIsToA;
    }

    public void setbIsToA(String bIsToA) {
        this.bIsToA = bIsToA;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Membership)) {
            return false;
        }

        final String otherUuid = ((Membership) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }
}
