package com.github.cimsbioko.server.domain;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FormId implements Serializable, Comparable<FormId> {

    @NotNull
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "version")
    private String version;

    FormId() {
    }

    public FormId(@NotNull String id, @NotNull String version) {
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormId formId = (FormId) o;
        return Objects.equals(id, formId.id) &&
                Objects.equals(version, formId.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public int compareTo(@NotNull FormId o) {
        if (o == this) return 0;
        int idCompare = getId().compareTo(o.getId());
        if (idCompare != 0) return idCompare;
        return getVersion().compareTo(o.getVersion());
    }
}
