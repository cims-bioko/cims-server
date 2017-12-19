package com.github.cimsbioko.server.domain;

import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

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
    String extId;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, targetEntity = LocationHierarchy.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    LocationHierarchy parent;

    @NotNull
    @CheckFieldNotBlank
    String name;

    @ManyToOne
    @JoinColumn(name = "level")
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
}
