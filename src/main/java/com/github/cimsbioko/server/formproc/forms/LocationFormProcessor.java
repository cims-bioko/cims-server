package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.dao.FieldWorkerRepository;
import com.github.cimsbioko.server.dao.LocationHierarchyLevelRepository;
import com.github.cimsbioko.server.dao.LocationHierarchyRepository;
import com.github.cimsbioko.server.dao.LocationRepository;
import com.github.cimsbioko.server.domain.FieldWorker;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.domain.LocationHierarchyLevel;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

public class LocationFormProcessor extends AbstractFormProcessor {

    private LocationRepository locRepo;
    private LocationHierarchyRepository hierRepo;
    private LocationHierarchyLevelRepository hierLevelRepo;
    private FieldWorkerRepository fwRepo;

    public LocationFormProcessor(LocationRepository locRepo, LocationHierarchyRepository hierRepo, LocationHierarchyLevelRepository hierLevelRepo, FieldWorkerRepository fwRepo) {
        this.locRepo = locRepo;
        this.hierRepo = hierRepo;
        this.hierLevelRepo = hierLevelRepo;
        this.fwRepo = fwRepo;
    }

    @Transactional
    public void processForm(Form form) throws ConstraintViolations {

        ConstraintViolations cv = new ConstraintViolations();

        if (form.locationExtId == null) {
            cv.addViolations("No Location ExtId provided");
            throw cv;
        }

        Location location;
        try {
            // FIXME: use optional rather than null
            location = locRepo.findById(form.entityUuid).orElse(null);
            if (location != null) {
                cv.addViolations("Location already exists " + form.entityUuid);
                throw cv;
            }
        } catch (Exception e) {
            return;
        }

        location = new Location();

        // collected by whom?
        // FIXME: use optional rather than null
        FieldWorker collectedBy = fwRepo.findById(form.fieldWorkerUuid).orElse(null);
        if (null == collectedBy) {
            cv.addViolations("Field Worker does not exist : " + form.fieldWorkerUuid);
            throw cv;
        }
        location.setCollector(collectedBy);

        // collected where?
        LocationHierarchy locationHierarchy;
        try {

            // Get hierarchy by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = form.hierarchyUuid;
            if (uuid == null) {
                locationHierarchy = hierRepo.findByExtId(form.hierarchyExtId);
            } else {
                // FIXME: Use optional rather than null
                locationHierarchy = hierRepo.findById(uuid).orElse(null);
                if (null == locationHierarchy) {
                    locationHierarchy = createSector(form);
                }
            }

            if (locationHierarchy == null) {
                cv.addViolations("Location Hierarchy does not exist : " + form.hierarchyUuid + " / " + form.hierarchyExtId);
                throw cv;
            }
        } catch (Exception e) {
            return;
        }

        location.setHierarchy(locationHierarchy);

        if (!locRepo.findByExtIdAndDeletedIsNull(form.locationExtId).isEmpty()) {
            modifyExtId(location, form);
        } else {
            location.setExtId(form.locationExtId);
        }

        // fill in data for the new location
        copyFormDataToLocation(form, location);

        // persist the location
        locRepo.save(location);
    }

    private void modifyExtId(Location location, Form form) {
        String newExtId = generateUniqueExtId(form.locationExtId);
        location.setExtId(newExtId);
    }

    private String generateUniqueExtId(String currentExtId) {
        String duplicateLocationSuffix = "-d";
        int sequenceNumber = 1;
        while (!locRepo.findByExtIdAndDeletedIsNull(currentExtId + duplicateLocationSuffix + sequenceNumber).isEmpty()) {
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
        if (form.locationType != null) {
            location.getAttrsForUpdate().put("type", form.locationType);
        }
        if (form.housingType != null) {
            JSONObject attrs = location.getAttrsForUpdate(), housing = new JSONObject();
            attrs.put("housing", housing);
            housing.put("type", form.housingType);
            if ("other".equals(form.housingType)) {
                housing.put("other", form.housingTypeOther);
            }
        }
    }

    private LocationHierarchy createSector(Form form) throws ConstraintViolations {
        LocationHierarchy locationHierarchy = new LocationHierarchy();
        locationHierarchy.setExtId(form.hierarchyExtId);
        locationHierarchy.setUuid(form.hierarchyUuid);
        locationHierarchy.setName(form.sectorName);

        String parentUuid = form.hierarchyParentUuid;
        // FIXME: Use optional rather than null
        LocationHierarchy parent = hierRepo.findById(parentUuid).orElse(null);
        if (parent == null) {
            throw new ConstraintViolations("Could not find location hierarchy parent with UUID " + parentUuid);
        }
        locationHierarchy.setParent(parent);

        int levelKeyId = parent.getLevel().getKeyId() + 1;
        LocationHierarchyLevel level = hierLevelRepo.findByKeyId(levelKeyId);
        if (level == null) {
            throw new ConstraintViolations("Could not find location hierarchy level with key " + levelKeyId);
        }
        locationHierarchy.setLevel(level);

        return hierRepo.save(locationHierarchy);
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

        private String housingType;

        private String housingTypeOther;

        private String sectorName;

        private String description;

        private String longitude;

        private String latitude;
    }
}
