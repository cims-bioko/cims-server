package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.exception.ConstraintViolations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Component
public class LocationFormProcessor extends AbstractFormProcessor {

    @Transactional
    public void processForm(Form form) throws ConstraintViolations {

    }

    @XmlRootElement(name = "locationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "LocationForm";

        private String entityUuid;

        private String fieldWorkerUuid;

        private String hierarchyExtId;

        private String hierarchyUuid;

        private String hierarchyParentUuid;

        private String locationExtId;

        private String locationName;

        private String locationType;

        private String housingType;

        private String housingTypeOther;

        private String sectorName;

        private String description;

        private String longitude;

        private String latitude;
    }
}
