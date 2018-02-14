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
public class LocationEvalFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(LocationEvalFormProcessor.class);

    @Autowired
    private LocationService locationService;

    @Transactional
    public void processForm(Form form) throws ConstraintViolations {
        if (form.entityUuid != null) {
            Location location = locationService.getByUuid(form.entityUuid);
            if (location != null) {
                switch (form.evaluation) {
                    case NOT_FOUND:
                        location.getAttrsForUpdate().put("status", "not-found");
                        break;
                    case DESTROYED:
                        location.getAttrsForUpdate().put("status", "destroyed");
                        location.setDeleted(true);
                        break;
                    case UNINHABITED:
                        location.getAttrsForUpdate().put("status", "uninhabited");
                        break;
                }
                locationService.save(location);
            } else {
                log.info("location {} does not exist, ignoring", form.entityUuid);
            }
        }
    }

    @XmlEnum
    @XmlType
    public enum LocationEvaluation {
        @XmlEnumValue("notfound") NOT_FOUND,
        @XmlEnumValue("destroyed") DESTROYED,
        @XmlEnumValue("uninhabited") UNINHABITED,
        @XmlEnumValue("unavailable") UNAVAILABLE,
        @XmlEnumValue("refused") REFUSED
    }

    @XmlRootElement(name = "locationEvalForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "LocationEvalForm";

        private String entityUuid;

        private LocationEvaluation evaluation;
    }
}
