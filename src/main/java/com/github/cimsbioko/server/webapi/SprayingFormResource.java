package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.refactor.LocationService;
import com.github.cimsbioko.server.domain.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Controller
@RequestMapping("/sprayingForm")
public class SprayingFormResource extends AbstractFormResource {

    @Autowired
    private LocationService locationService;

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) throws JAXBException {
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

    @XmlEnum(String.class)
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