
package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.constraint.CheckEntityNotVoided;
import com.github.cimsbioko.server.domain.constraint.CheckIndividualNotUnknown;
import com.github.cimsbioko.server.domain.constraint.Searchable;
import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;
import com.github.cimsbioko.server.domain.constraint.CheckHouseholdHeadAge;
import com.github.cimsbioko.server.domain.constraint.ExtensionStringConstraint;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@Description(description = "A Social Group represents a distinct family within the study area. Social Groups are identified by a uniquely generated identifier which the system uses internally. A Social Group has one head of house which all Membership relationships are based on.")
@Entity
@Table(name = "socialgroup")
@XmlRootElement(name = "socialgroup")
public class SocialGroup
        extends AuditableCollectedEntity
        implements Serializable {

    public final static long serialVersionUID = -5592935530217622317L;
    @Searchable
    @Description(description = "External Id of the social group. This id is used internally.")
    private String extId;

    @Searchable
    @CheckFieldNotBlank
    @Description(description = "Name of the social group.")
    private String groupName;

    @Searchable
    @CheckEntityNotVoided
    @CheckIndividualNotUnknown
    @CheckHouseholdHeadAge(allowNull = true, message = "The social group head is younger than the minimum age required in order to be a household head.")
    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @Description(description = "Individual who is head of the social group, identified by the external id.")
    private Individual groupHead;

    @ExtensionStringConstraint(constraint = "socialGroupTypeConstraint", message = "Invalid Value for social group type", allowNull = true)
    @Description(description = "Type of the social group.")
    private String groupType;

    @OneToMany(cascade = {
            CascadeType.ALL
    }, mappedBy = "socialGroup")
    @Description(description = "The set of all memberships of the social group.")
    private Set<Membership> memberships = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @Description(description = "The location associated with this social group")
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String id) {
        extId = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String name) {
        groupName = name;
    }

    public Individual getGroupHead() {
        return groupHead;
    }

    public void setGroupHead(Individual head) {
        groupHead = head;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String group) {
        groupType = group;
    }

    public Set<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(Set<Membership> list) {
        memberships = list;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SocialGroup)) {
            return false;
        }

        final String otherUuid = ((SocialGroup) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }
}
