package org.openhds.webservice.resources;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.domain.model.ErrorLog;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Location.Locations;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.errorhandling.util.ErrorLogUtil;
import org.openhds.task.support.FileResolver;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
import org.openhds.webservice.response.WebserviceResult;
import org.openhds.webservice.response.WebserviceResultHelper;
import org.openhds.webservice.response.constants.ResultCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

@Controller
@RequestMapping("/locations")
public class LocationResource {

    private final FieldBuilder fieldBuilder;
    private final LocationHierarchyService locationHierarchyService;
    private final FileResolver fileResolver;
    private final ErrorHandlingService errorService;
    private final Marshaller marshaller;

    @Autowired
    public LocationResource(LocationHierarchyService locationHierarchyService, FieldBuilder fieldBuilder,
                            FileResolver fileResolver, ErrorHandlingService errorService) {
        this.locationHierarchyService = locationHierarchyService;
        this.fieldBuilder = fieldBuilder;
        this.fileResolver = fileResolver;
        this.errorService = errorService;
        try {
            JAXBContext context = JAXBContext.newInstance(Location.class);
            marshaller = context.createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for location resource, CRITICAL ERROR");
        }


    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<? extends Serializable> getLocationByExtIdJson(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);
        if (location == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        WebserviceResult result = new WebserviceResult();
        result.addDataElement("location", ShallowCopier.makeShallowCopy(location));
        result.setResultCode(ResultCodes.SUCCESS_CODE);
        result.setStatus(ResultCodes.SUCCESS);
        result.setResultMessage("Location was found");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.GET, produces = "application/xml")
    public ResponseEntity<? extends Serializable> getLocationByExtIdXml(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);
        if (location == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(ShallowCopier.makeShallowCopy(location), HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public Locations getAllLocationsXml() {
        List<Location> locations = locationHierarchyService.getAllLocations();
        List<Location> copies = new ArrayList<>(locations.size());

        for (Location loc : locations) {
            Location copy = ShallowCopier.makeShallowCopy(loc);
            copies.add(copy);
        }

        Locations allLocations = new Location.Locations();
        allLocations.setLocations(copies);
        return allLocations;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<WebserviceResult> getAllLocationsJson() {
        List<Location> locations = locationHierarchyService.getAllLocations();
        List<Location> copies = new ArrayList<>(locations.size());

        for (Location loc : locations) {
            Location copy = ShallowCopier.makeShallowCopy(loc);
            copies.add(copy);
        }

        WebserviceResult result = new WebserviceResult();
        result.addDataElement("locations", copies);
        result.setResultCode(ResultCodes.SUCCESS_CODE);
        result.setStatus(ResultCodes.SUCCESS);
        result.setResultMessage(locations.size() + " locations were found.");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    public ResponseEntity<? extends Serializable> insertXml(@RequestBody Location location) throws JAXBException {
        ConstraintViolations cv = new ConstraintViolations();
        location.setCollectedBy(fieldBuilder.referenceField(location.getCollectedBy(), cv));
        location.setLocationHierarchy(fieldBuilder.referenceField(location.getLocationHierarchy(), cv));

        if (cv.hasViolations()) {
            StringWriter writer = new StringWriter();
            marshaller.marshal(location, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Location.class.getSimpleName(),
                    location.getCollectedBy(), ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return new ResponseEntity<>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        try {
            locationHierarchyService.createLocation(location);
        } catch (ConstraintViolations e) {
            StringWriter writer = new StringWriter();
            marshaller.marshal(location, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Location.class.getSimpleName(),
                    location.getCollectedBy(), ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return new ResponseEntity<>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ShallowCopier.makeShallowCopy(location), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<? extends Serializable> insertJson(@RequestBody Location location) throws JAXBException {
        ConstraintViolations cv = new ConstraintViolations();
        location.setCollectedBy(fieldBuilder.referenceField(location.getCollectedBy(), cv));
        location.setLocationHierarchy(fieldBuilder.referenceField(location.getLocationHierarchy(), cv));

        if (cv.hasViolations()) {
            StringWriter writer = new StringWriter();
            marshaller.marshal(location, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Location.class.getSimpleName(),
                    location.getCollectedBy(), ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return WebserviceResultHelper.genericConstraintResponse(cv);
        }

        try {
            locationHierarchyService.createLocation(location);
        } catch (ConstraintViolations e) {
            StringWriter writer = new StringWriter();
            marshaller.marshal(location, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Location.class.getSimpleName(),
                    location.getCollectedBy(), ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return WebserviceResultHelper.genericConstraintResponse(cv);
        }

        WebserviceResult result = new WebserviceResult();
        result.addDataElement("location", ShallowCopier.makeShallowCopy(location));
        result.setResultCode(ResultCodes.SUCCESS_CODE);
        result.setStatus(ResultCodes.SUCCESS);
        result.setResultMessage("Location created");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "application/xml", consumes = "application/xml")
    public ResponseEntity<? extends Serializable> addOrUpdateXml(@RequestBody Location location) {

        ConstraintViolations cv = new ConstraintViolations();
        location.setCollectedBy(fieldBuilder.referenceField(location.getCollectedBy(), cv));
        location.setLocationHierarchy(fieldBuilder.referenceField(location.getLocationHierarchy(), cv));

        if (cv.hasViolations()) {
            return new ResponseEntity<>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        Location existingLocation = locationHierarchyService.findLocationById(location.getExtId());
        if (existingLocation == null) {
            try {
                locationHierarchyService.createLocation(location);
            } catch (ConstraintViolations e) {
                return new ResponseEntity<>(new WebServiceCallException(new ConstraintViolations(e.getMessage(), e.getViolations())), HttpStatus.BAD_REQUEST);

            }
            return new ResponseEntity<>(ShallowCopier.makeShallowCopy(location), HttpStatus.CREATED);

        }

        //updating fields on existing persistent object
        existingLocation.setLocationName(location.getLocationName());

        try {
            locationHierarchyService.updateLocation(existingLocation);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<>(new WebServiceCallException(new ConstraintViolations(e.getMessage(), e.getViolations())), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ShallowCopier.makeShallowCopy(location), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public ResponseEntity<? extends Serializable> addOrUpdateJson(@RequestBody Location location) {

        ConstraintViolations cv = new ConstraintViolations();
        location.setCollectedBy(fieldBuilder.referenceField(location.getCollectedBy(), cv));
        location.setLocationHierarchy(fieldBuilder.referenceField(location.getLocationHierarchy(), cv));

        if (cv.hasViolations()) {
            return WebserviceResultHelper.genericConstraintResponse(cv);
        }

        Location existingLocation = locationHierarchyService.findLocationById(location.getExtId());
        if (existingLocation == null) {
            try {
                locationHierarchyService.createLocation(location);
            } catch (ConstraintViolations e) {
                return WebserviceResultHelper.genericConstraintResponse(cv);
            }
            WebserviceResult result = new WebserviceResult();
            result.addDataElement("location", ShallowCopier.makeShallowCopy(location));
            result.setResultCode(ResultCodes.SUCCESS_CODE);
            result.setStatus(ResultCodes.SUCCESS);
            result.setResultMessage("Location was created");

            return new ResponseEntity<>(result, HttpStatus.CREATED);

        }

        //updating fields on existing persistent object
        existingLocation.setLocationName(location.getLocationName());

        try {
            locationHierarchyService.updateLocation(existingLocation);
        } catch (ConstraintViolations e) {
            return WebserviceResultHelper.genericConstraintResponse(cv);
        }

        WebserviceResult result = new WebserviceResult();
        result.addDataElement("location", ShallowCopier.makeShallowCopy(location));
        result.setResultCode(ResultCodes.SUCCESS_CODE);
        result.setStatus(ResultCodes.SUCCESS);
        result.setResultMessage("Location was updated");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.DELETE, produces = "application/xml")
    public ResponseEntity<? extends Serializable> deleteLocationByExtIdXml(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);

        if (location == null) {
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        try {
            locationHierarchyService.deleteLocation(location);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<>(new WebServiceCallException(new ConstraintViolations(e.getMessage(), e.getViolations())), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ShallowCopier.makeShallowCopy(location), HttpStatus.OK);
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<? extends Serializable> deleteLocationByExtIdJson(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);

        if (location == null) {
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        try {
            locationHierarchyService.deleteLocation(location);
        } catch (ConstraintViolations cv) {
            return WebserviceResultHelper.genericConstraintResponse(cv);
        }

        WebserviceResult result = new WebserviceResult();
        result.addDataElement("location", ShallowCopier.makeShallowCopy(location));
        result.setResultCode(ResultCodes.SUCCESS_CODE);
        result.setStatus(ResultCodes.SUCCESS);
        result.setResultMessage("Location was deleted");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
