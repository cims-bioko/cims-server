package com.github.cimsbioko.server.domain;

import org.hibernate.annotations.Type;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "locationhierarchy")
public class LocationHierarchy implements Serializable {

    private static final long serialVersionUID = -5334850119671675888L;

    @Id
    @Column(length = 32)
    private String uuid;

    @NotNull
    @Column(name = "extid")
    private String extId;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, targetEntity = LocationHierarchy.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    private LocationHierarchy parent;

    @NotNull
    private String name;

    @ManyToOne
    @JoinColumn(name = "level")
    private LocationHierarchyLevel level;

    @Type(type = "json")
    private JSONObject attrs;

    public String getUuid() {
        return uuid;
    }

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

    public JSONObject getAttrs() {
        return attrs;
    }

    public void setAttrs(JSONObject attrs) {
        this.attrs = attrs;
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
        return null != uuid && uuid.equals(otherUuid);
    }
}
