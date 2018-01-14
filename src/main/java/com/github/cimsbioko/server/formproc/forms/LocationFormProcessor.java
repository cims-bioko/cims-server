package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.domain.FieldWorker;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.domain.LocationHierarchyLevel;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.LocationHierarchyService;
import com.github.cimsbioko.server.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.service.refactor.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.Serializable;

@Component
public class LocationFormProcessor extends AbstractFormProcessor {

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationHierarchyService locationHierarchyService;

    @Transactional
    public void processForm(Form form) throws IOException {

        ConstraintViolations cv = new ConstraintViolations();

        if (form.locationExtId == null) {
            cv.addViolations("No Location ExtId provided");
            logError(cv, marshalForm(form), Form.LOG_NAME);
            return;
        }

        Location location;
        try {
            location = locationService.getByUuid(form.entityUuid);
            if (null != location) {
                cv.addViolations("Location already exists " + form.entityUuid);
                logError(cv, marshalForm(form), Form.LOG_NAME);
                return;
            }
        } catch (Exception e) {
            return;
        }

        location = new Location();

        // collected by whom?
        FieldWorker collectedBy = fieldWorkerService.getByUuid(form.fieldWorkerUuid);
        if (null == collectedBy) {
            cv.addViolations("Field Worker does not exist : " + form.fieldWorkerUuid);
            logError(cv, marshalForm(form), Form.LOG_NAME);
            return;
        }
        location.setCollector(collectedBy);

        // collected where?
        LocationHierarchy locationHierarchy;
        try {

            // Get hierarchy by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = form.hierarchyUuid;
            if (null == uuid) {
                locationHierarchy = locationHierarchyService.findByExtId(form.hierarchyExtId);
            } else {
                locationHierarchy = locationHierarchyService.findByUuid(uuid);
                if (null == locationHierarchy) {
                    locationHierarchy = createSector(form);
                }
            }

            if (null == locationHierarchy) {
                cv.addViolations("Location Hierarchy does not exist : " + form.hierarchyUuid + " / " + form.hierarchyExtId);
                logError(cv, marshalForm(form), Form.LOG_NAME);
                return;
            }
        } catch (Exception e) {
            return;
        }

        location.setHierarchy(locationHierarchy);

        // modify the extId if it matches another location's extId, log the change
        if (null != locationService.getByExtId(form.locationExtId)) {
            modifyExtId(location, form);
            cv.addViolations("Location persisted with Modified ExtId: Old = " + form.locationExtId + " New = " + location.getExtId());
            logError(cv, marshalForm(form), Form.LOG_NAME);
        } else {
            location.setExtId(form.locationExtId);
        }

        // fill in data for the new location
        copyFormDataToLocation(form, location);

        // persist the location
        try {
            locationService.create(location);
        } catch (ConstraintViolations e) {
            logError(cv, marshalForm(form), Form.LOG_NAME);
            return;
        } catch (Exception e) {
            return;
        }
    }

    private void modifyExtId(Location location, Form form) {
        String newExtId = generateUniqueExtId(form.locationExtId);
        location.setExtId(newExtId);
    }

    private String generateUniqueExtId(String currentExtId) {
        String duplicateLocationSuffix = "-d";
        int sequenceNumber = 1;
        while (null != locationService.getByExtId(currentExtId + duplicateLocationSuffix + sequenceNumber)) {
            sequenceNumber++;
        }
        duplicateLocationSuffix = duplicateLocationSuffix + sequenceNumber;
        return currentExtId + duplicateLocationSuffix;
    }

    private void copyFormDataToLocation(Form form, Location location) {
        // fieldWorker, CollectedDateTime, and HierarchyLevel are set outside of this method
        location.setUuid(form.entityUuid);
        location.setName(form.locationName);
        location.setDescription(form.description);
        if (form.latitude != null && form.longitude != null) {
            location.setGlobalPos(makePoint(form.longitude, form.latitude));
        }
    }

    private LocationHierarchy createSector(Form form) throws ConstraintViolations {
        LocationHierarchy locationHierarchy = new LocationHierarchy();
        locationHierarchy.setExtId(form.hierarchyExtId);
        locationHierarchy.setUuid(form.hierarchyUuid);
        locationHierarchy.setName(form.sectorName);

        String parentUuid = form.hierarchyParentUuid;
        LocationHierarchy parent = locationHierarchyService.findByUuid(parentUuid);
        if (parent == null) {
            throw new ConstraintViolations("Could not find location hierarchy parent with UUID " + parentUuid);
        }
        locationHierarchy.setParent(parent);

        int levelKeyId = parent.getLevel().getKeyId() + 1;
        LocationHierarchyLevel level = locationHierarchyService.getLevel(levelKeyId);
        if (null == level) {
            throw new ConstraintViolations("Could not find location hierarchy level with key " + levelKeyId);
        }
        locationHierarchy.setLevel(level);

        locationHierarchyService.createLocationHierarchy(locationHierarchy);

        return locationHierarchy;
    }


    @XmlRootElement(name = "locationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "LocationForm";

        private String entityUuid;

        private String fieldWorkerUuid;

        private String hierarchyExtId;

        private String hierarchyUuid;

        private String hierarchyParentUuid;

        private String locationExtId;

        private String locationName;

        private String locationType;

        private String sectorName;

        private String description;

        private String longitude;

        private String latitude;
    }
}
