package org.openhds.domain.model;

import org.hibernate.annotations.GenericGenerator;
import org.openhds.domain.annotations.Description;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Description(description = "An individual error")
@Entity
@Table(name = "error")
@XmlRootElement(name = "error")
public class Error implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length=32)
    private String uuid;

    private static final long serialVersionUID = 1L;

    private String errorMessage;

    public Error() { }

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
