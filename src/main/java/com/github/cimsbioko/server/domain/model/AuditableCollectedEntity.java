package com.github.cimsbioko.server.domain.model;

import java.io.Serializable;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import com.github.cimsbioko.server.domain.annotations.Description;

/**
 * An AuditableCollectedEntity is any entity that is recorded or collected by a Field Worker
 * that needs to be audited
 */
@MappedSuperclass
public abstract class AuditableCollectedEntity extends AuditableEntity implements Serializable {

    private static final long serialVersionUID = 3558979775991767767L;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = FieldWorker.class)
    @Description(description = "The field worker who collected the data, identified by external id.")
    protected FieldWorker collectedBy;

    public FieldWorker getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(FieldWorker collectedBy) {
        this.collectedBy = collectedBy;
    }
}
