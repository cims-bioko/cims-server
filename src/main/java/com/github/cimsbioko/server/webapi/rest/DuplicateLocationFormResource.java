package com.github.cimsbioko.server.webapi.rest;

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

import javax.xml.bind.annotation.*;
import java.io.Serializable;

import static com.github.cimsbioko.server.webapi.rest.DuplicateLocationFormResource.DUPLICATE_LOCATION_FORM_PATH;

@Controller
@RequestMapping(DUPLICATE_LOCATION_FORM_PATH)
public class DuplicateLocationFormResource extends AbstractFormResource {

    public static final String DUPLICATE_LOCATION_FORM_PATH = "/rest/duplicateLocationForm";

    @Autowired
    private LocationService locationService;

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) {

        if (form.uuid != null) {
            Location location = locationService.getByUuid(form.uuid);
            switch (form.action) {
                case GPS_ONLY:
                    if (form.latitude != null && form.longitude != null) {
                        location.setGlobalPos(makePoint(form.longitude, form.latitude));
                    }
                    break;
                case REMOVE:
                    location.setDeleted(true);
                    break;
            }
            try {
                // Always attempt to update description if it's provided
                if (form.description != null) {
                    location.setDescription(form.description);
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
    public enum Action {
        @XmlEnumValue("renumber")RENUMBER,
        @XmlEnumValue("merge")MERGE,
        @XmlEnumValue("gps-only")GPS_ONLY,
        @XmlEnumValue("not-found")NOT_FOUND,
        @XmlEnumValue("remove")REMOVE
    }

    @XmlRootElement(name = "duplicateLocationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "DuplicateLocationForm";

        @XmlElement(name = "entity_uuid")
        private String uuid;

        @XmlElement(name = "action")
        private Action action;

        @XmlElement(name = "description")
        private String description;

        @XmlElement(name = "global_position_lat")
        private String latitude;

        @XmlElement(name = "global_position_lng")
        private String longitude;

        @XmlElement(name = "global_position_acc")
        private String accuracy;
    }
}
