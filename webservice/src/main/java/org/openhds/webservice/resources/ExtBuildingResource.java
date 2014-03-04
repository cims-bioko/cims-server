package org.openhds.webservice.resources;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.ExtBuilding;
import org.openhds.domain.model.LocationHierarchy;
import org.openhds.domain.model.wrappers.Locations;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.support.FileResolver;
import org.openhds.webservice.CacheResponseWriter;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/extbuildings")
public class ExtBuildingResource {
    private static final Logger logger = LoggerFactory.getLogger(ExtBuildingResource.class);

    private final FieldBuilder fieldBuilder;
    private final LocationHierarchyService locationHierarchyService;

    @Autowired
    public ExtBuildingResource(LocationHierarchyService locationHierarchyService, FieldBuilder fieldBuilder) {
        this.locationHierarchyService = locationHierarchyService;
        this.fieldBuilder = fieldBuilder;
    }

    // For now this is serving a LocationHierarchy. It should server an
    // ExtBuilding.
    @RequestMapping(value = "/{extId}", method = RequestMethod.GET)
    public ResponseEntity<? extends Serializable> getExtBuildingByHierarchyExtId(@PathVariable String extId) {
        LocationHierarchy locationHierarchy;
        try {
            locationHierarchy = locationHierarchyService.findLocationHierarchyById(extId, "");
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (locationHierarchy == null) {
            return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<LocationHierarchy>(ShallowCopier.copyLocationHierarchy(locationHierarchy),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Locations getAllLocations() {
        List<Location> locations = locationHierarchyService.getAllLocations();
        List<Location> copies = new ArrayList<Location>(locations.size());

        for (Location loc : locations) {
            Location copy = ShallowCopier.copyLocation(loc);
            copies.add(copy);
        }

        Locations allLocations = new Locations();
        allLocations.setLocations(copies);
        return allLocations;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<? extends Serializable> insert(@RequestBody ExtBuilding extBuilding) {
        ConstraintViolations cv = new ConstraintViolations();
        extBuilding.setCollectedBy(fieldBuilder.referenceField(extBuilding.getCollectedBy(), cv));
        extBuilding.setLocationHierarchy(fieldBuilder.referenceField(extBuilding.getLocationHierarchy(), cv));

        if (cv.hasViolations()) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        try {
            locationHierarchyService.createExtBuilding(extBuilding);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<ExtBuilding>(ShallowCopier.copyExtBuilding(extBuilding), HttpStatus.CREATED);
    }
}
