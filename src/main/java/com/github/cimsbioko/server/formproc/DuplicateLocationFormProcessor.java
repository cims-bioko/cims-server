package com.github.cimsbioko.server.formproc;

import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Component
public class DuplicateLocationFormProcessor extends AbstractFormProcessor {

    @Autowired
    private LocationService locationService;

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
