package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Component
public class DuplicateLocationFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(DuplicateLocationFormProcessor.class);

    @Autowired
    private LocationService locationService;

    @Transactional
    public void processForm(Form form) {
        if (form.uuid != null) {
            Location location = locationService.getByUuid(form.uuid);
            if (location != null) {
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
                    /* ignore - fall through to return */
                }
            } else {
                log.info("location {} not found, ignoring", form.uuid);
            }
        }
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

        private Action action;

        private String description;

        @XmlElement(name = "global_position_lat")
        private String latitude;

        @XmlElement(name = "global_position_lng")
        private String longitude;

        @XmlElement(name = "global_position_acc")
        private String accuracy;
    }
}
