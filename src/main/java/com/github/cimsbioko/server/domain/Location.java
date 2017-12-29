
package com.github.cimsbioko.server.domain;

import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;
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

@Entity
@Table(name = "location")
@XmlRootElement
public class Location
        extends AuditableCollectedEntity
        implements Serializable {

    public final static long serialVersionUID = 169551578162260199L;

    @NotNull
    @Size(min = 1)
    private String extId;

    @CheckFieldNotBlank
    private String name;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "hierarchy")
    private LocationHierarchy hierarchy = new LocationHierarchy();

    @ExtensionStringConstraint(constraint = "locationTypeConstraint", message = "Invalid Value for location type", allowNull = true)
    @Column(name = "type")
    private String type;

    @Column(name = "global_pos")
    private Point globalPos;

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