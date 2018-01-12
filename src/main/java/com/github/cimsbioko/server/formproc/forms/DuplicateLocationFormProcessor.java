package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.service.LocationHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@Component
public class DuplicateLocationFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(DuplicateLocationFormProcessor.class);

    @Autowired
    private GenericDao dao;

    @Autowired
    private LocationHierarchyService hierarchyService;

    @Transactional
    public void processForm(Form form) {
        if (form.entityUuid != null) {

            Location location = dao.findByProperty(Location.class, "uuid", form.entityUuid, true);

            if (location != null) {

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
                }

                if (form.description != null) {
                    location.setDescription(form.description);
                }

            } else {
                log.info("location {} does not exist, ignoring", form.entityUuid);
            }
        }
    }

    private void handleRenumber(Form form, Location location) {
        LocationHierarchy locality = location.getHierarchy().getParent().getParent(),
        map = hierarchyService.createOrFindMap(locality.getUuid(), form.mapAreaName),
        sector = hierarchyService.createOrFindSector(map.getUuid(), form.sectorName);
        location.setExtId(form.newExtId);
        location.setHierarchy(sector);
        if (hasCoordinates(form)) {
            updatePosition(location, form.latitude, form.longitude);
        }
    }

    private void handleRemove(Location location) {
        location.setDeleted(true);
    }

    private void handleMerge(Form form, Location src) {
        List<Location> candidates = dao.findListByProperty(
                Location.class, "extId", form.mergeToExtId, true);
        if (candidates.size() <= 0) {
            log.info("keeping location {}, location at {} does not exist", src.getUuid(), form.mergeToExtId);
        } else {
            Location dst = candidates.get(0);
            if (src != dst) {
                src.setDeleted(true);
                if (hasCoordinates(form)) {
                    updatePosition(dst, form.latitude, form.longitude);
                } else if (dst.getGlobalPos() == null ||
                        (dst.getGlobalPos().getX() == 0 && dst.getGlobalPos().getY() == 0)) {
                    dst.setGlobalPos(src.getGlobalPos());
                }
            }
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

        private String entityUuid;

        private String entityExtId;

        private Action action;

        private String description;

        @XmlElement(name = "global_position_lat")
        private String latitude;

        @XmlElement(name = "global_position_lng")
        private String longitude;

        @XmlElement(name = "global_position_acc")
        private String accuracy;

        private String mapAreaName;

        private String sectorName;

        private String mergeToExtId;

        private String newExtId;
    }
}
