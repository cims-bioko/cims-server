package org.openhds.domain.model;

import org.hibernate.annotations.GenericGenerator;
import org.openhds.domain.annotations.Description;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

@Description(description = "An individual error")
@Entity
@Table(name = "error")
@XmlRootElement(name = "error")
public class Error implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.openhds.domain.util.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    private static final long serialVersionUID = 1L;

    private String errorMessage;

    @Description(description = "Indicator for signaling some data to be deleted.")
    protected boolean deleted = false;

    public Error() {
    }

    @XmlTransient
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Error(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
