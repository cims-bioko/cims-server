package com.github.cimsbioko.server.formproc.forms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Component
public class CreateSectorFormProcessor {

    @Transactional
    public void processForm(Form form) {

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
