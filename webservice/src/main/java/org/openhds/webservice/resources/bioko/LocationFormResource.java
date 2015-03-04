package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.LocationService;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.LocationHierarchy;
import org.openhds.domain.model.LocationHierarchyLevel;
import org.openhds.domain.model.bioko.LocationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.Calendar;


@Controller
@RequestMapping("/locationForm")
public class LocationFormResource extends AbstractFormResource{
    private static final Logger logger = LoggerFactory.getLogger(LocationFormResource.class);

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationHierarchyService locationHierarchyService;


    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(
            @RequestBody LocationForm locationForm) {

        // clean up "null" strings created by Mirth
        if ("null".equals(locationForm.getHierarchyUuid())) {
            locationForm.setHierarchyUuid(null);
        }

        Location location;
        try {
            location = locationService.getByUuid(locationForm.getUuid());
            if (null != location) {
                return requestError("Location already exists: " + locationForm.getLocationExtId());
            }
        } catch (Exception e) {
            return requestError("Error checking for existing location: " + e.getMessage());
        }

        location = new Location();

        // collected by whom?
        FieldWorker collectedBy = fieldWorkerService.getByUuid(locationForm.getFieldWorkerUuid());
        if (null == collectedBy) {
            return requestError("Error getting field worker: Location form has nonexistent field worker uuid");
        }
        location.setCollectedBy(collectedBy);

        // collected where?
        LocationHierarchy locationHierarchy;
        try {

            // Get hierarchy by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = locationForm.getHierarchyUuid();
            if (null == uuid) {
                locationHierarchy = locationHierarchyService.findByExtId(locationForm.getHierarchyExtId());
            } else {
                locationHierarchy = locationHierarchyService.findByUuid(uuid);
                if (null == locationHierarchy) {
                    locationHierarchy = createSector(locationForm);
                }

            }

            if (null == locationHierarchy) {
                return requestError("LocationHierarchy doesn't exist!: "
                        + locationForm.getHierarchyUuid()
                        + " / "
                        + locationForm.getHierarchyExtId());
            }

        } catch (Exception e) {
            return requestError("Error getting reference to LocationHierarchy: " + e.getMessage());
        }

        // fill in data for the new location
        location.setLocationHierarchy(locationHierarchy);
        copyFormDataToLocation(locationForm, location);

        // persist the location
        try {
            locationService.create(location);
        } catch (ConstraintViolations e) {
            return requestError(e);
        } catch (Exception e) {
            return serverError("General Error creating location: " + e.getMessage());
        }

        return new ResponseEntity<LocationForm>(locationForm, HttpStatus.CREATED);
    }

    private void copyFormDataToLocation(LocationForm locationForm, Location location){

        // fieldWorker, CollectedDateTime, and HierarchyLevel are set outside of this method
        location.setUuid(locationForm.getUuid());
        location.setExtId(locationForm.getLocationExtId());
        location.setCommunityName(locationForm.getCommunityName());
        location.setCommunityCode(locationForm.getCommunityCode());
        location.setLocalityName(locationForm.getLocalityName());
        location.setMapAreaName(locationForm.getMapAreaName());
        location.setSectorName(locationForm.getSectorName());
        location.setLocationName(locationForm.getLocationName());
        location.setLocationType(nullTypeToUrb(locationForm.getLocationType()));
        location.setBuildingNumber(digitsOnly(locationForm.getBuildingNumber()));
        location.setFloorNumber(digitsOnly(locationForm.getFloorNumber()));
        location.setDescription(locationForm.getDescription());
        location.setLongitude(locationForm.getLongitude());
        location.setLatitude(locationForm.getLatitude());
    }

    private static String nullTypeToUrb(String locationType) {
        return null == locationType || "null".equals(locationType) ? "URB" : locationType;
    }

    private static String digitsOnly(String dirtyNumber) {
        if (null == dirtyNumber || "null".equals(dirtyNumber)) {
            return null;
        }
        return dirtyNumber.replaceAll("\\D+","");
    }

    private static Calendar getDateInPast() {
        Calendar inPast = Calendar.getInstance();
        inPast.set(1900, 0, 1);
        return inPast;
    }

    private LocationHierarchy createSector(LocationForm locationForm) throws ConstraintViolations {
        LocationHierarchy locationHierarchy = new LocationHierarchy();

        locationHierarchy.setExtId(locationForm.getHierarchyExtId());
        locationHierarchy.setUuid(locationForm.getHierarchyUuid());
        locationHierarchy.setName(locationForm.getSectorName());

        String parentUuid = locationForm.getHierarchyParentUuid();
        LocationHierarchy parent = locationHierarchyService.findByUuid(parentUuid);
        if (null == parent) {
            throw new ConstraintViolations("Could not find location hierarchy parent with UUID " + parentUuid);
        }
        locationHierarchy.setParent(parent);

        int levelKeyId = parent.getLevel().getKeyIdentifier() + 1;
        LocationHierarchyLevel level = locationHierarchyService.getLevel(levelKeyId);
        if (null == level) {
            throw new ConstraintViolations("Could not find location hierarchy level with key " + levelKeyId);
        }
        locationHierarchy.setLevel(level);

        locationHierarchyService.createLocationHierarchy(locationHierarchy);

        return locationHierarchy;
    }
}
