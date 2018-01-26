package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.service.LocationHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Component
public class CreateSectorFormProcessor {

    @Autowired
    LocationHierarchyService hierService;

    @Transactional
    public void processForm(Form form) {
        hierService.createSector(form.mapUuid, form.sectorUuid, form.sectorName);
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
