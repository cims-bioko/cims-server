package com.github.cimsbioko.server.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "locationhierarchylevel")
public class LocationHierarchyLevel implements Serializable {

    private static final long serialVersionUID = -1070569257732332545L;

    @Id
    private String uuid;

    @Column(name = "keyid")
    private int keyId;

    @NotNull
    private String name;

    public String getUuid() {
        return uuid;
    }

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
