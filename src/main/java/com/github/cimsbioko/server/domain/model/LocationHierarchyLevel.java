package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Description(description = "The Location Hierarchy Level represents the specific " +
        "part of the Location Hierarchy that a Location resides in. The levels are " +
        "used in the configuration of the Location Hierarchy. Sample levels could be  " +
        "Region, District, Village. ")
@Entity
@Table(name = "locationhierarchylevel")
public class LocationHierarchyLevel implements UuidIdentifiable, Serializable {

    private static final long serialVersionUID = -1070569257732332545L;

    @Id
    String uuid;

    @Description(description = "A key to identify this level, assign 1, 2, 3, ... etc")
    int keyId;

    @NotNull
    @CheckFieldNotBlank
    @Description(description = "The name of this location hierarchy level.")
    String name;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
