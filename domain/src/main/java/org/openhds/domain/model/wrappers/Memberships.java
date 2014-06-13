package org.openhds.domain.model.wrappers;

import org.openhds.domain.model.Membership;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class Memberships implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Membership> memberships;

    @XmlElement(name = "membership")
    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> copies) {
        this.memberships = copies;
    }
}
