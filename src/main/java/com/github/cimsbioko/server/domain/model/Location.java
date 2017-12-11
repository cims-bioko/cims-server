
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
import java.util.ArrayList;
import java.util.List;

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
    private String name;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "hierarchy")
    private LocationHierarchy hierarchy = new LocationHierarchy();

    @ExtensionStringConstraint(constraint = "locationTypeConstraint", message = "Invalid Value for location type", allowNull = true)
    @Description(description = "The type of Location.")
    @Column(name = "type")
    private String type;

    @Description(description = "the global position represented as longitude, latitude, altitude")
    @Column(name = "global_pos")
    private Point globalPos;

    @Description(description = "A description of the observable features of a location")
    private String description;

    @OneToMany(mappedBy = "home")
    private List<Individual> residents = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationHierarchy getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(LocationHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Point getGlobalPos() {
        return globalPos;
    }

    public void setGlobalPos(Point globalPos) {
        this.globalPos = globalPos;
    }

    public List<Individual> getResidents() {
        return residents;
    }

    public void addResident(Individual resident) {
        residents.add(resident);
        resident.setHome(this);
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
