package org.openhds.domain.model;

import org.openhds.domain.annotations.Description;
import org.openhds.domain.constraint.CheckFieldNotBlank;
import org.openhds.domain.constraint.Searchable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@Description(description = "The Location Hierarchy represents the overall structure " +
        "of all Locations within the study area. The levels of the hierarchy are " +
        "specified in a configuration file which may set the levels as follows: " +
        "Region, District, Village. Each record in this hierarchy will have " +
        "a uniquely generated identifier which the system uses internally. " +
        "Every record will also have a parent location name except the root. Finally, " +
        "all records within the hierarchy will have a name which must be unique. " +
        "Note that this is not to be confused with Location. The Location's name " +
        "field must reference a valid location name from this configured hierarchy.")
@Entity
@Table(name = "locationhierarchy")
@XmlRootElement
public class LocationHierarchy implements UuidIdentifiable, Serializable {

    private static final long serialVersionUID = -5334850119671675888L;

    @Id
    @Column(length = 32)
    String uuid;

    @CheckFieldNotBlank
    @NotNull
    @Searchable
    @Description(description = "External Id of the location hierarchy. This id is used internally.")
    String extId;

    @Description(description = "Parent location's name.")
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, targetEntity = LocationHierarchy.class, fetch = FetchType.LAZY)
    LocationHierarchy parent;

    @NotNull
    @CheckFieldNotBlank
    @Searchable
    @Description(description = "The name of this location hierarchy record.")
    String name;

    @Description(description = "Level of the location hierarchy.")
    @ManyToOne
    LocationHierarchyLevel level;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public LocationHierarchy getParent() {
        return parent;
    }

    public void setParent(LocationHierarchy parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationHierarchyLevel getLevel() {
        return level;
    }

    public void setLevel(LocationHierarchyLevel level) {
        this.level = level;
    }

    @Override
    public int hashCode() {
        if (null == uuid) {
            return 0;
        }
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof LocationHierarchy)) {
            return false;
        }

        final String otherUuid = ((LocationHierarchy) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }

    @XmlRootElement
    public static class LocationHierarchies {

        private List<LocationHierarchy> locationHierarchies;

        @XmlElement(name = "hierarchy")
        public List<LocationHierarchy> getLocationHierarchies() {
            return locationHierarchies;
        }

        public void setLocationHierarchies(List<LocationHierarchy> locationHierarchies) {
            this.locationHierarchies = locationHierarchies;
        }

    }

}
