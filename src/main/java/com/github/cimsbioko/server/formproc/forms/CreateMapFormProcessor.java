package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.service.LocationHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Component
public class CreateMapFormProcessor {

    @Autowired
    LocationHierarchyService hierService;

    @Transactional
    public void processForm(Form form) {
        hierService.createOrFindMap(form.localityUuid, form.mapName);
    }

    @XmlRootElement(name = "createMapForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "CreateMapForm";

        private String localityUuid;

        private String mapName;
    }
}
