package com.github.cimsbioko.server.webapi.rest;

import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.LocationService;
import com.github.cimsbioko.server.domain.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

import static com.github.cimsbioko.server.webapi.rest.SprayingFormResource.SPRAYING_FORM_PATH;

@Controller
@RequestMapping(SPRAYING_FORM_PATH)
public class SprayingFormResource extends AbstractFormResource {

    public static final String SPRAYING_FORM_PATH = "/rest/sprayingForm";

    @Autowired
    private LocationService locationService;

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
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