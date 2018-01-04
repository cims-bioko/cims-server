package com.github.cimsbioko.server.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "locationhierarchylevel")
public class LocationHierarchyLevel implements UuidIdentifiable, Serializable {

    private static final long serialVersionUID = -1070569257732332545L;

    @Id
    String uuid;

    int keyId;

    @NotNull
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
