package org.openhds.webservice.resources;

import com.github.batkinson.jrsync.Metadata;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.util.CacheResponseWriter;
import org.openhds.domain.model.ErrorLog;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Location.Locations;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.errorhandling.util.ErrorLogUtil;
import org.openhds.task.service.AsyncTaskService;
import org.openhds.task.support.FileResolver;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
import org.openhds.webservice.response.WebserviceResult;
import org.openhds.webservice.response.WebserviceResultHelper;
import org.openhds.webservice.response.constants.ResultCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/locations")
public class LocationResource implements ServletContextAware {
    private static final Logger logger = LoggerFactory.getLogger(LocationResource.class);

    private final FieldBuilder fieldBuilder;
    private final LocationHierarchyService locationHierarchyService;
    private final FileResolver fileResolver;
    private final ErrorHandlingService errorService;
    private final JAXBContext context;
    private final Marshaller marshaller;

    @Autowired
    private CacheResponseWriter cacheResponseWriter;

    @Autowired
    private AsyncTaskService asyncTaskService;

    private ServletContext ctx;

    @Autowired
    public LocationResource(LocationHierarchyService locationHierarchyService, FieldBuilder fieldBuilder,
                            FileResolver fileResolver, ErrorHandlingService errorService) {
        this.locationHierarchyService = locationHierarchyService;
        this.fieldBuilder = fieldBuilder;
        this.fileResolver = fileResolver;
        this.errorService = errorService;
        try {
            context = JAXBContext.newInstance(Location.class);
            marshaller = context.createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for location resource, CRITICAL ERROR");
        }


    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<? extends Serializable> getLocationByExtIdJson(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);
        if (location == null) {
            return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
        }

        WebserviceResult result = new WebserviceResult();
        result.addDataElement("location", ShallowCopier.makeShallowCopy(location));
        result.setResultCode(ResultCodes.SUCCESS_CODE);
        result.setStatus(ResultCodes.SUCCESS);
        result.setResultMessage("Location was found");

        return new ResponseEntity<WebserviceResult>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.GET, produces = "application/xml")
    public ResponseEntity<? extends Serializable> getLocationByExtIdXml(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);
        if (location == null) {
            return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Location>(ShallowCopier.makeShallowCopy(location), HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public Locations getAllLocationsXml() {
        List<Location> locations = locationHierarchyService.getAllLocations();
        List<Location> copies = new ArrayList<Location>(locations.size());

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
        List<Location> copies = new ArrayList<Location>(locations.size());

        for (Location loc : locations) {
            Location copy = ShallowCopier.makeShallowCopy(loc);
            copies.add(copy);
        }

        WebserviceResult result = new WebserviceResult();
        result.addDataElement("locations", copies);
        result.setResultCode(ResultCodes.SUCCESS_CODE);
        result.setStatus(ResultCodes.SUCCESS);
        result.setResultMessage(locations.size() + " locations were found.");

        return new ResponseEntity<WebserviceResult>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/streamtest", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource streamOutCachedXml() {
        return new FileSystemResource(fileResolver.resolveLocationXmlFile());
    }

    @RequestMapping(value = "/cached", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE, Metadata.MIME_TYPE })
    public void getAllCachedLocationsXml(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String contentHash = asyncTaskService.getContentHash(AsyncTaskService.LOCATION_TASK_NAME);
        String eTag = request.getHeader(Headers.IF_NONE_MATCH);

        if (eTag != null && eTag.equals(contentHash)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        File xmlFile = fileResolver.resolveLocationXmlFile();
        File metaFile = new File(xmlFile.getParentFile(), xmlFile.getName() + "." + Metadata.FILE_EXT);

        if (Metadata.MIME_TYPE.equals(request.getHeader(Headers.ACCEPT)) && metaFile.exists()) {
            response.setContentType(Metadata.MIME_TYPE);
            request.getRequestDispatcher(contextPath(metaFile)).forward(request, response);
            return;
        }

        if (contentHash != null) {
            response.setHeader(Headers.ETAG, contentHash);
        }

        // This prevents the downstream servlet from overwriting our ETag value
        HttpServletResponseWrapper ignoreEtag = new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                if (!Headers.ETAG.equalsIgnoreCase(name)) {
                    super.setHeader(name, value);
                }
            }
        };

        try {
            request.getRequestDispatcher(contextPath(xmlFile)).forward(request, ignoreEtag);
        } catch (IOException e) {
            logger.error("Problem writing location xml file: " + e.getMessage());
        }
    }

    private String contextPath(File file) {
        return "/" + file.getAbsolutePath().replaceFirst(ctx.getRealPath("/"), "");
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
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        try {
            locationHierarchyService.createLocation(location);
        } catch (ConstraintViolations e) {
            StringWriter writer = new StringWriter();
            marshaller.marshal(location, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Location.class.getSimpleName(),
                    location.getCollectedBy(), ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Location>(ShallowCopier.makeShallowCopy(location), HttpStatus.CREATED);
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
            return WebserviceResultHelper.genericConstraintResponse(cv);        }

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
        return new ResponseEntity<WebserviceResult>(result, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "application/xml", consumes = "application/xml")
    public ResponseEntity<? extends Serializable> addOrUpdateXml(@RequestBody Location location) {

        ConstraintViolations cv = new ConstraintViolations();
        location.setCollectedBy(fieldBuilder.referenceField(location.getCollectedBy(), cv));
        location.setLocationHierarchy(fieldBuilder.referenceField(location.getLocationHierarchy(), cv));

        if (cv.hasViolations()) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        Location existingLocation  = locationHierarchyService.findLocationById(location.getExtId());
        if (existingLocation == null) {
            try {
                locationHierarchyService.createLocation(location);
            } catch (ConstraintViolations e) {
                return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(new ConstraintViolations(e.getMessage(), e.getViolations())), HttpStatus.BAD_REQUEST);

            }
            return new ResponseEntity<Location>(ShallowCopier.makeShallowCopy(location), HttpStatus.CREATED);

        }

        //updating fields on existing persistent object
        existingLocation.setLocationName(location.getLocationName());

        try {
            locationHierarchyService.updateLocation(existingLocation);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(new ConstraintViolations(e.getMessage(), e.getViolations())), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Location>(ShallowCopier.makeShallowCopy(location), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public ResponseEntity<? extends Serializable> addOrUpdateJson(@RequestBody Location location) {

        ConstraintViolations cv = new ConstraintViolations();
        location.setCollectedBy(fieldBuilder.referenceField(location.getCollectedBy(), cv));
        location.setLocationHierarchy(fieldBuilder.referenceField(location.getLocationHierarchy(), cv));

        if (cv.hasViolations()) {
            return WebserviceResultHelper.genericConstraintResponse(cv);
        }

        Location existingLocation  = locationHierarchyService.findLocationById(location.getExtId());
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

            return new ResponseEntity<WebserviceResult>(result, HttpStatus.CREATED);

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

        return new ResponseEntity<WebserviceResult>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.DELETE, produces = "application/xml")
    public ResponseEntity<? extends Serializable> deleteLocationByExtIdXml(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);

        if (location == null) {
            return new ResponseEntity<String>(HttpStatus.GONE);
        }

        try {
            locationHierarchyService.deleteLocation(location);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(new ConstraintViolations(e.getMessage(), e.getViolations())), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Location>(ShallowCopier.makeShallowCopy(location), HttpStatus.OK);
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<? extends Serializable> deleteLocationByExtIdJson(@PathVariable String extId) {
        Location location = locationHierarchyService.findLocationById(extId);

        if (location == null) {
            return new ResponseEntity<String>(HttpStatus.GONE);
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

        return new ResponseEntity<WebserviceResult>(result, HttpStatus.OK);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.ctx = servletContext;
    }
}
