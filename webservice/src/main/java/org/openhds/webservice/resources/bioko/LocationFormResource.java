package org.openhds.webservice.resources.bioko;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.FieldWorkerService;
import org.openhds.controller.service.IndividualService;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.ResidencyService;
import org.openhds.controller.service.SocialGroupService;
import org.openhds.domain.model.*;
import org.openhds.domain.model.bioko.IndividualForm;
import org.openhds.domain.model.bioko.LocationForm;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
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
    private LocationHierarchyService locationHierarchyService;

    @Autowired
    private EntityService entityService;

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(
            @RequestBody LocationForm locationForm) {

        Location location = null;
        try {
            location = locationHierarchyService
                    .findLocationById(locationForm.getLocationExtId());
            if (null != location) {
                return requestError("Location already exists!: " + locationForm.getLocationExtId());
            }
        } catch (Exception e) { }

        location = new Location();

        // collected by whom?
        FieldWorker collectedBy;
        try {
            collectedBy = fieldWorkerService.findFieldWorkerById(
                    locationForm.getFieldWorkerExtId(),
                    "Individual form has nonexistent field worker id.");
            location.setCollectedBy(collectedBy);
        } catch (Exception e) {
            return requestError("Error getting field worker: " + e.getMessage());
        }


        // collected where?
        LocationHierarchy locationHierarchy;
        try {
            locationHierarchy = locationHierarchyService
                    .findLocationHierarchyById(locationForm.getHierarchyExtId());
            location.setLocationLevel(locationHierarchy);
            if (null == locationHierarchy) {
                return requestError("LocationHierarchy doesn't exist!: " + locationForm.getHierarchyExtId());
            }
        } catch (Exception e) {
            return requestError("Error getting reference LocationHierarchy: " + e.getMessage());
        }


        // do it
        copyFormDataToIndividual(locationForm, location);



        // persist the location
        try {
            locationHierarchyService.createLocation(location);
        } catch (ConstraintViolations e) {
            return requestError(e);
        } catch (Exception e) {
            return serverError("General Error creating location: " + e.getMessage());
        }

        return new ResponseEntity<LocationForm>(locationForm, HttpStatus.CREATED);
    }

    private void copyFormDataToIndividual(LocationForm locationForm, Location location){


        // fieldWorker, CollectedDateTime, and HierarchyLevel are set outside of this method

        location.setExtId(locationForm.getLocationExtId());
        location.setCommunityName(locationForm.getCommunityName());
        location.setLocalityName(locationForm.getLocalityName());
        location.setMapAreaName(locationForm.getMapAreaName());
        location.setSectorName(locationForm.getSectorName());
        location.setLocationName(locationForm.getLocationName());
        location.setLocationType(locationForm.getLocationType());

        location.setBuildingNumber(locationForm.getBuildingNumber());
        location.setFloorNumber(locationForm.getFloorNumber());

    }
    private static Calendar getDateInPast() {
        Calendar inPast = Calendar.getInstance();
        inPast.set(1900, 0, 1);
        return inPast;
    }

}
