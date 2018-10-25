package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.dao.LocationRepository;
import com.github.cimsbioko.server.domain.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Calendar;

public class LocationEvalFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(LocationEvalFormProcessor.class);

    LocationRepository repo;

    public LocationEvalFormProcessor(LocationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void processForm(Form form) {
        if (form.entityUuid != null) {
            // FIXME: use optional rather than null
            Location location = repo.findById(form.entityUuid).orElse(null);
            if (location != null) {
                switch (form.evaluation) {
                    case NOT_FOUND:
                        location.getAttrsForUpdate().put("status", "not-found");
                        break;
                    case DESTROYED:
                        location.getAttrsForUpdate().put("status", "destroyed");
                        location.setDeleted(Calendar.getInstance());
                        break;
                    case UNINHABITED:
                        location.getAttrsForUpdate().put("status", "uninhabited");
                        break;
                    case BAD_DESC:
                        location.setDescription(form.description);
                }
                repo.save(location);
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
        @XmlEnumValue("refused") REFUSED,
        @XmlEnumValue("baddesc") BAD_DESC
    }

    @XmlRootElement(name = "locationEvalForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "LocationEvalForm";

        private String entityUuid;

        private String description;

        private LocationEvaluation evaluation;
    }
}
