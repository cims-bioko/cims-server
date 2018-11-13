package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.dao.LocationRepository;
import com.github.cimsbioko.server.domain.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Calendar;

public class SprayingFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(SprayingFormProcessor.class);

    private LocationRepository repo;

    public SprayingFormProcessor(LocationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void processForm(Form form) {
        if (form.entityUuid != null) {
            // FIXME: Use optional rather than null
            Location location = repo.findById(form.entityUuid).orElse(null);
            if (location != null) {
                switch (form.evaluation) {
                    case DESTROYED:
                        location.getAttrsForUpdate().put("status", "destroyed");
                        location.setDeleted(Calendar.getInstance());
                        break;
                    case UNINHABITED:
                        location.getAttrsForUpdate().put("status", "uninhabited");
                        break;
                }
                repo.save(location);
            } else {
                log.info("location {} does not exist, ignoring", form.entityUuid);
            }
        }
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