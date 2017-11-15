package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.annotations.Description;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Calendar;

/**
 * An AuditableEntity can be any entity stored in the database that needs to be audited
 */
@MappedSuperclass
public abstract class AuditableEntity implements UuidIdentifiable, Serializable {

    private static final long serialVersionUID = -4703049354466276068L;

    @Id
    @Column(length = 32)
    String uuid;

    @Description(description = "Indicator for signaling some data to be deleted.")
    protected boolean deleted = false;

    @Description(description = "Date of insertion.")
    protected Calendar insertDate;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @XmlTransient
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Calendar getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Calendar insertDate) {
        this.insertDate = insertDate;
    }

    @Override
    public int hashCode() {
        if (null == uuid) {
            return 0;
        }
        return uuid.hashCode();
    }
}
