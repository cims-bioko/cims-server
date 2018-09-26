package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.exception.ConstraintViolations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Controller
public class SprayingFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(SprayingFormProcessor.class);

    @Transactional
    public void processForm(Form form) throws ConstraintViolations {

    }

    @XmlEnum
    @XmlType
    public enum SprayingEvaluation {
        @XmlEnumValue("1") SPRAYED,
        @XmlEnumValue("2") REFUSED,
        @XmlEnumValue("3") CLOSED,
        @XmlEnumValue("4") UNINHABITED,
        @XmlEnumValue("5") DESTROYED
    }

    @XmlRootElement(name = "sprayingForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "SprayingForm";

        private String entityUuid;

        private SprayingEvaluation evaluation;
    }
}