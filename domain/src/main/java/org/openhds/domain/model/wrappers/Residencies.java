package org.openhds.domain.model.wrappers;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhds.domain.model.Residency;

@XmlRootElement
public class Residencies implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Residency> residencies;

    @XmlElement(name = "residency")
    public List<Residency> getResidencies() {
        return residencies;
    }

    public void setResidencies(List<Residency> copies) {
        this.residencies = copies;
    }
}
