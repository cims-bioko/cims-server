package org.openhds.webservice.resources.bioko;

import java.io.Serializable;
import java.util.Calendar;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.refactor.LocationService;
import org.openhds.domain.model.*;
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

        Location location;
        try {
            location = locationService
                    .getByUuid(locationForm.getUuid());
            if (null != location) {
                return requestError("Location already exists!: " + locationForm.getLocationExtId());
            }
        } catch (Exception e) { }

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
            locationHierarchy = locationHierarchyService
                    .findByUuid(locationForm.getHierarchyUuid());
            location.setLocationHierarchy(locationHierarchy);
            if (null == locationHierarchy) {
                return requestError("LocationHierarchy doesn't exist!: " + locationForm.getHierarchyUuid());
            }
        } catch (Exception e) {
            return requestError("Error getting reference LocationHierarchy: " + e.getMessage());
        }

        // do it
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
        location.setLocationType(locationForm.getLocationType());
        location.setBuildingNumber(locationForm.getBuildingNumber());
        location.setFloorNumber(locationForm.getFloorNumber());
        location.setDescription(locationForm.getDescription());
        location.setLongitude(locationForm.getLongitude());
        location.setLatitude(locationForm.getLatitude());

    }
    private static Calendar getDateInPast() {
        Calendar inPast = Calendar.getInstance();
        inPast.set(1900, 0, 1);
        return inPast;
    }

}
