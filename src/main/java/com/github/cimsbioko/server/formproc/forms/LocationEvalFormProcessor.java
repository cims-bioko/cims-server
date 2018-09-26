package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.exception.ConstraintViolations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Component
public class LocationEvalFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(LocationEvalFormProcessor.class);

    @Transactional
    public void processForm(Form form) throws ConstraintViolations {

    }

    @XmlEnum
    @XmlType
    public enum LocationEvaluation {
        @XmlEnumValue("notfound") NOT_FOUND,
        @XmlEnumValue("destroyed") DESTROYED,
        @XmlEnumValue("uninhabited") UNINHABITED,
        @XmlEnumValue("unavailable") UNAVAILABLE,
        @XmlEnumValue("refused") REFUSED,
        @XmlEnumValue("baddesc") BAD_DESC
    }

    @XmlRootElement(name = "locationEvalForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "LocationEvalForm";

        private String entityUuid;

        private String description;

        private LocationEvaluation evaluation;
    }
}
