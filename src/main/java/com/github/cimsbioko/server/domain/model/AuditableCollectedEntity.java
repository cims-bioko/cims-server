package com.github.cimsbioko.server.domain.model;

import java.io.Serializable;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

/**
 * An AuditableCollectedEntity is any entity that is recorded or collected by a Field Worker
 * that needs to be audited
 */
@MappedSuperclass
public abstract class AuditableCollectedEntity extends AuditableEntity implements Serializable {

    private static final long serialVersionUID = 3558979775991767767L;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = FieldWorker.class)
    @JoinColumn(name = "collector")
    protected FieldWorker collector;

    public FieldWorker getCollector() {
        return collector;
    }

    public void setCollector(FieldWorker collector) {
        this.collector = collector;
    }
}
