package com.github.cimsbioko.server.formproc.forms;

import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

public class CreateMapFormProcessor {

    @Transactional
    public void processForm(Form form) {
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
