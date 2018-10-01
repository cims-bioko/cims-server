package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.service.LocationHierarchyService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

public class CreateMapFormProcessor {

    private LocationHierarchyService service;

    public CreateMapFormProcessor(LocationHierarchyService service) {
        this.service = service;
    }

    public void processForm(Form form) {
        service.createMap(form.localityUuid, form.mapUuid, form.mapName);
    }

    @XmlRootElement(name = "createMapForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "CreateMapForm";

        private String localityUuid;

        private String mapUuid;

        private String mapName;
    }
}
