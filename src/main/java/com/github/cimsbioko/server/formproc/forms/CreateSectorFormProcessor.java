package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.service.LocationHierarchyService;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

public class CreateSectorFormProcessor {

    LocationHierarchyService service;

    public CreateSectorFormProcessor(LocationHierarchyService service) {
        this.service = service;
    }

    public void processForm(Form form) {
        service.createSector(form.mapUuid, form.sectorUuid, form.sectorName);
    }

    @XmlRootElement(name = "createSectorForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "CreateSectorForm";

        private String mapUuid;

        private String sectorUuid;

        private String sectorName;
    }
}
