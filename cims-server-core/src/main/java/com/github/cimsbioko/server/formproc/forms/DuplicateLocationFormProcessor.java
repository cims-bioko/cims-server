package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.dao.LocationRepository;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.service.LocationHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Optional;

public class DuplicateLocationFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(DuplicateLocationFormProcessor.class);

    private LocationRepository locRepo;
    private LocationHierarchyService hierSvc;

    public DuplicateLocationFormProcessor(LocationRepository locationRepo, LocationHierarchyService hierService) {
        this.locRepo = locationRepo;
        this.hierSvc = hierService;
    }

    @Transactional
    public void processForm(Form form) {
        if (form.entityUuid != null) {

            // FIXME: after spring-data upgrade, we can and should use Optional
            Location location = locRepo.findById(form.entityUuid).orElse(null); // don't find if location is deleted? or just ignore?

            if (location != null) {

                if (location.getDeleted() != null) {
                    log.warn("location {} was deleted, ignoring", location.getUuid());
                    return;
                }

                if (!location.getExtId().equals(form.entityExtId)) {
                    log.warn("location {} extid mismatch: form={} db={}, ignoring",
                            location.getUuid(), form.entityExtId, location.getExtId());
                    return;
                }

                switch (form.action) {
                    case GPS_ONLY:
                        handleGPSOnly(form, location);
                        break;
                    case REMOVE:
                        handleRemove(location);
                        break;
                    case MERGE:
                        handleMerge(form, location);
                        break;
                    case RENUMBER:
                        handleRenumber(form, location);
                        break;
                    case NOT_FOUND:
                        handleNotFound(location);
                        break;
                }

                if (form.description != null) {
                    location.setDescription(form.description);
                }

            } else {
                log.info("location {} does not exist, ignoring", form.entityUuid);
            }
        }
    }

    private void handleNotFound(Location location) {
        location.getAttrsForUpdate().put("status", "not-found");
    }

    private void handleRenumber(Form form, Location location) {
        // assumes hierarchy is complete: locality/community->map->sector->location
        LocationHierarchy locality = location.getHierarchy().getParent().getParent(),
                map = hierSvc.createOrFindMap(locality.getUuid(), form.mapAreaName),
                sector = hierSvc.createOrFindSector(map.getUuid(), form.sectorName);
        location.setExtId(form.newExtId);
        location.setHierarchy(sector);
        if (hasCoordinates(form)) {
            updatePosition(location, form.latitude, form.longitude);
        }
    }

    private void handleRemove(Location location) {
        location.setDeleted(Calendar.getInstance());
        location.getAttrsForUpdate().put("status", "voided");
    }

    private void handleMerge(Form form, Location src) {

        // could improve on this, but it's likely moving into campaign configuration
        Location dst;
        if (form.mergeToUuid != null) {
            dst = locRepo.findById(form.mergeToUuid)
                    .filter(l -> l.getDeleted() == null && !form.mergeToExtId.equals(l.getExtId()))
                    .orElse(null);
        } else {
            dst = locRepo.findByExtIdAndDeletedIsNull(form.mergeToExtId)
                    .stream()
                    .findFirst().orElse(null);
        }

        if (dst == null) {
            log.info("failed to find an acceptable merge candidate");
            return;
        }

        if (src == dst) {
            log.info("source and destination are the same");
            return;
        }

        // do the merge
        src.setDeleted(Calendar.getInstance());
        src.getAttrsForUpdate().put("status", "merged");

        // also update coordinates if provided in form
        if (hasCoordinates(form)) {
            updatePosition(dst, form.latitude, form.longitude);
        } else if (dst.getGlobalPos() == null ||
                (dst.getGlobalPos().getX() == 0 && dst.getGlobalPos().getY() == 0)) {
            dst.setGlobalPos(src.getGlobalPos());
        }
    }

    private void handleGPSOnly(Form form, Location location) {
        if (hasCoordinates(form)) {
            updatePosition(location, form.latitude, form.longitude);
        }
    }

    private void updatePosition(Location location, String latitude, String longitude) {
        location.setGlobalPos(makePoint(longitude, latitude));
    }

    private boolean hasCoordinates(Form form) {
        return form.latitude != null && form.longitude != null;
    }

    @XmlEnum
    @XmlType
    public enum Action {
        @XmlEnumValue("renumber") RENUMBER,
        @XmlEnumValue("merge") MERGE,
        @XmlEnumValue("gps-only") GPS_ONLY,
        @XmlEnumValue("not-found") NOT_FOUND,
        @XmlEnumValue("remove") REMOVE
    }

    @XmlRootElement(name = "duplicateLocationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "DuplicateLocationForm";

        private String entityUuid;

        private String entityExtId;

        private Action action;

        private String description;

        private String latitude;

        private String longitude;

        private String accuracy;

        private String mapAreaName;

        private String sectorName;

        private String mergeToExtId;

        private String mergeToUuid;

        private String newExtId;
    }
}
