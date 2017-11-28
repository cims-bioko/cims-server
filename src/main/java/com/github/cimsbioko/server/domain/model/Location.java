
package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;
import com.github.cimsbioko.server.domain.constraint.Searchable;
import com.vividsolutions.jts.geom.Point;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import com.github.cimsbioko.server.domain.constraint.ExtensionStringConstraint;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Description(description = "All distinct Locations within the area of study are represented here. A Location is identified by a uniquely generated identifier that the system uses internally. Each Location has a name associated with it and resides at a particular hierarchy within the Location Hierarchy.")
@Entity
@Table(name = "location")
@XmlRootElement
public class Location
        extends AuditableCollectedEntity
        implements Serializable {

    public final static long serialVersionUID = 169551578162260199L;

    @NotNull
    @Size(min = 1)
    @Searchable
    @Description(description = "External Id of the location. This id is used internally.")
    private String extId;

    @CheckFieldNotBlank
    @Searchable
    @Description(description = "Name of the location.")
    private String locationName;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    private LocationHierarchy locationHierarchy = new LocationHierarchy();

    @ExtensionStringConstraint(constraint = "locationTypeConstraint", message = "Invalid Value for location type", allowNull = true)
    @Description(description = "The type of Location.")
    private String locationType;

    @Description(description = "the global position represented as longitude, latitude, altitude")
    @Column(name = "global_pos")
    private Point globalPos;

    @Description(description = "A description of the observable features of a location")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String id) {
        extId = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String name) {
        locationName = name;
    }

    public LocationHierarchy getLocationHierarchy() {
        return locationHierarchy;
    }

    public void setLocationHierarchy(LocationHierarchy hierarchy) {
        locationHierarchy = hierarchy;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String type) {
        locationType = type;
    }

    public Point getGlobalPos() {
        return globalPos;
    }

    public void setGlobalPos(Point globalPos) {
        this.globalPos = globalPos;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Location)) {
            return false;
        }

        final String otherUuid = ((Location) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }
}
