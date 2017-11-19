package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.service.LocationHierarchyService;
import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.controller.service.refactor.LocationService;
import com.github.cimsbioko.server.domain.model.*;
import com.github.cimsbioko.server.domain.model.Error;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.controller.service.ErrorService;
import com.github.cimsbioko.server.controller.util.ErrorUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.github.cimsbioko.server.webapi.LocationFormResource.LOCATION_FORM_PATH;

@Controller
@RequestMapping(LOCATION_FORM_PATH)
public class LocationFormResource extends AbstractFormResource {

    public static final String LOCATION_FORM_PATH = "/rest/locationForm";

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationHierarchyService locationHierarchyService;

    @Autowired
    private ErrorService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private Marshaller marshaller = null; // FIXME: *not thread safe!!!*


    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) throws JAXBException {

        try {
            JAXBContext context = JAXBContext.newInstance(Form.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        List<String> logMessage = new ArrayList<>();
        ConstraintViolations cv = new ConstraintViolations();

        if (form.locationExtId == null) {
            cv.addViolations("No Location ExtId provided");
            logError(cv, createDTOPayload(form), Form.LOG_NAME);
            return requestError(cv);
        }

        Location location;
        try {
            location = locationService.getByUuid(form.uuid);
            if (null != location) {
                cv.addViolations("Location already exists " + form.uuid);
                logError(cv, createDTOPayload(form), Form.LOG_NAME);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error checking for existing location : " + e.getMessage());
        }

        location = new Location();

        // collected by whom?
        FieldWorker collectedBy = fieldWorkerService.getByUuid(form.fieldWorkerUuid);
        if (null == collectedBy) {
            cv.addViolations("Field Worker does not exist : " + form.fieldWorkerUuid);
            logError(cv, createDTOPayload(form), Form.LOG_NAME);
            return requestError(cv);
        }
        location.setCollectedBy(collectedBy);

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
                logError(cv, createDTOPayload(form), Form.LOG_NAME);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error getting reference to LocationHierarchy: " + e.getMessage());
        }

        location.setLocationHierarchy(locationHierarchy);

        // modify the extId if it matches another location's extId, log the change
        if (null != locationService.getByExtId(form.locationExtId)) {
            modifyExtId(location, form);
            cv.addViolations("Location persisted with Modified ExtId: Old = " + form.locationExtId + " New = " + location.getExtId());
            logError(cv, createDTOPayload(form), Form.LOG_NAME);
        } else {
            location.setExtId(form.locationExtId);
        }

        // fill in data for the new location
        copyFormDataToLocation(form, location);

        // persist the location
        try {
            locationService.create(location);
        } catch (ConstraintViolations e) {
            logError(cv, createDTOPayload(form), Form.LOG_NAME);
            return requestError(e);
        } catch (Exception e) {
            return serverError("General Error creating location: " + e.getMessage());
        }

        return new ResponseEntity<>(form, HttpStatus.CREATED);
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
        location.setUuid(form.uuid);
        location.setLocationName(form.locationName);
        location.setLocationType(nullTypeToUrb(form.locationType));
        location.setDescription(form.description);
        if (form.latitude != null && form.longitude != null) {
            location.setGlobalPos(makePoint(form.longitude, form.latitude));
        }
    }

    private static String nullTypeToUrb(String locationType) {
        return locationType == null ? "URB" : locationType;
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

        int levelKeyId = parent.getLevel().getKeyIdentifier() + 1;
        LocationHierarchyLevel level = locationHierarchyService.getLevel(levelKeyId);
        if (null == level) {
            throw new ConstraintViolations("Could not find location hierarchy level with key " + levelKeyId);
        }
        locationHierarchy.setLevel(level);

        locationHierarchyService.createLocationHierarchy(locationHierarchy);

        return locationHierarchy;
    }

    private String createDTOPayload(Form form) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();
    }


    @XmlRootElement(name = "locationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "LocationForm";

        //core form fields
        @XmlElement(name = "entity_uuid")
        private String uuid;

        @XmlElement(name = "field_worker_uuid")
        private String fieldWorkerUuid;

        //location form fields
        @XmlElement(name = "hierarchy_ext_id")
        private String hierarchyExtId;

        @XmlElement(name = "hierarchy_uuid")
        private String hierarchyUuid;

        @XmlElement(name = "hierarchy_parent_uuid")
        private String hierarchyParentUuid;

        @XmlElement(name = "location_ext_id")
        private String locationExtId;

        @XmlElement(name = "location_name")
        private String locationName;

        @XmlElement(name = "location_type")
        private String locationType;

        @XmlElement(name = "sector_name")
        private String sectorName;

        @XmlElement(name = "description")
        private String description;

        @XmlElement(name = "longitude")
        private String longitude;

        @XmlElement(name = "latitude")
        private String latitude;
    }
}
