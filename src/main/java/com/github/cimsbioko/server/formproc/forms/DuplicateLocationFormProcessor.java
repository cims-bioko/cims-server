package com.github.cimsbioko.server.formproc.forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Component
public class DuplicateLocationFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(DuplicateLocationFormProcessor.class);

    @Transactional
    public void processForm(Form form) {

    }

    @XmlEnum
    @XmlType
    public enum Action {
        @XmlEnumValue("renumber") RENUMBER,
        @XmlEnumValue("merge") MERGE,
        @XmlEnumValue("gps-only") GPS_ONLY,
        @XmlEnumValue("not-found") NOT_FOUND,
        @XmlEnumValue("remove") REMOVE
    }

    @XmlRootElement(name = "duplicateLocationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "DuplicateLocationForm";

        private String entityUuid;

        private String entityExtId;

        private Action action;

        private String description;

        private String latitude;

        private String longitude;

        private String accuracy;

        private String mapAreaName;

        private String sectorName;

        private String mergeToExtId;

        private String mergeToUuid;

        private String newExtId;
    }
}
