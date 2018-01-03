package com.github.cimsbioko.server.formproc;

import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Controller
public class SprayingFormProcessor extends AbstractFormProcessor {

    @Autowired
    private LocationService locationService;

    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) {
        if (form.uuid != null) {
            try {
                Location location = locationService.getByUuid(form.uuid);
                switch (form.evaluation) {
                    case DESTROYED:
                        location.setDeleted(true);
                        break;
                }
                locationService.save(location);
            } catch (ConstraintViolations cv) {
                return requestError(cv);
            }
        }
        return new ResponseEntity<>(form, HttpStatus.CREATED);
    }

    @XmlEnum
    @XmlType
    public enum Evaluation {
        @XmlEnumValue("1")SPRAYED,
        @XmlEnumValue("2")REFUSED,
        @XmlEnumValue("3")CLOSED,
        @XmlEnumValue("4")UNINHABITED,
        @XmlEnumValue("5")DESTROYED
    }

    @XmlRootElement(name = "sprayingForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "SprayingForm";

        @XmlElement(name = "entity_uuid")
        private String uuid;

        @XmlElement(name = "evaluation")
        private Evaluation evaluation;
    }
}