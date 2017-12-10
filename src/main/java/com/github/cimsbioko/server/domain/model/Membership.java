package com.github.cimsbioko.server.domain.model;

import java.io.Serializable;

import javax.persistence.*;
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
    @JoinColumn(name = "member")
    Individual member;

    @Searchable
    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "The social group of the membership, identified by external id.")
    @JoinColumn(name = "\"group\"")
    SocialGroup group;

    @ExtensionStringConstraint(constraint = "membershipConstraint", message = "Invalid Value for membership relation to head", allowNull = true)
    @Description(description = "Relationship type to the group head.")
    String role;

    public Individual getMember() {
        return member;
    }

    public void setMember(Individual member) {
        this.member = member;
    }

    public SocialGroup getGroup() {
        return group;
    }

    public void setGroup(SocialGroup group) {
        this.group = group;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
