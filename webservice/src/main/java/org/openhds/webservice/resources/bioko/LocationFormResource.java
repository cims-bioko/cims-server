package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.LocationService;
import org.openhds.domain.model.*;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.errorhandling.util.ErrorLogUtil;
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private Marshaller marshaller = null;


    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(
            @RequestBody Form form) throws JAXBException {

        try {
            JAXBContext context = JAXBContext.newInstance(Form.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        List<String> logMessage = new ArrayList<>();
        ConstraintViolations cv = new ConstraintViolations();

        // clean up "null" strings created by Mirth0
        if ("null".equals(form.getHierarchyUuid())) {
            form.setHierarchyUuid(null);
        }

        if ("null".equals(form.getLocationExtId())) {
            cv.addViolations("No Location ExtId provided");
            logError(cv, null, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_LOCATION_EXT_ID);
            return requestError(cv);
        }

        Location location;
        try {
            location = locationService.getByUuid(form.getUuid());
            if (null != location) {
                cv.addViolations("Location already exists " + form.getUuid());
                logError(cv, null, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_LOCATION_UUID);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error checking for existing location : " + e.getMessage());
        }

        location = new Location();

        // collected by whom?
        FieldWorker collectedBy = fieldWorkerService.getByUuid(form.getFieldWorkerUuid());
        if (null == collectedBy) {
            cv.addViolations("Field Worker does not exist : "+ form.getFieldWorkerUuid());
            logError(cv, null, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_FIELD_WORKER_UUID);
            return requestError(cv);
        }
        location.setCollectedBy(collectedBy);

        // collected where?
        LocationHierarchy locationHierarchy;
        try {

            // Get hierarchy by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = form.getHierarchyUuid();
            if (null == uuid) {
                locationHierarchy = locationHierarchyService.findByExtId(form.getHierarchyExtId());
            } else {
                locationHierarchy = locationHierarchyService.findByUuid(uuid);
                if (null == locationHierarchy) {
                    locationHierarchy = createSector(form);
                }
            }

            if (null == locationHierarchy) {
                cv.addViolations("Location Hierarchy does not exist : "+ form.getHierarchyUuid() + " / "+ form.getHierarchyExtId());
                logError(cv, collectedBy, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_LOCATION_HIERARCHY_UUID);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error getting reference to LocationHierarchy: " + e.getMessage());
        }

        location.setLocationHierarchy(locationHierarchy);

        // modify the extId if it matches another location's extId, log the change
        if (null != locationService.getByExtId(form.getLocationExtId())) {

            modifyExtId(location, form);
            // log the modification
            logMessage.add("Location persisted with Modified ExtId: Old = "+ form.getLocationExtId()+" New = "+location.getExtId());
            String payload = createDTOPayload(form);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, payload, null,
                    Form.LOG_NAME, collectedBy,
                    ErrorConstants.MODIFIED_EXTID, logMessage);
            errorService.logError(error);
        } else {
            location.setExtId(form.getLocationExtId());
            location.setBuildingNumber(form.getBuildingNumber());
        }

        // fill in data for the new location
        copyFormDataToLocation(form, location);

        // persist the location
        try {
            locationService.create(location);
        } catch (ConstraintViolations e) {
            logError(cv, collectedBy, createDTOPayload(form), Form.LOG_NAME, ErrorConstants.CONSTRAINT_VIOLATION);
            return requestError(e);
        } catch (Exception e) {
            return serverError("General Error creating location: " + e.getMessage());
        }

        return new ResponseEntity<>(form, HttpStatus.CREATED);
    }

    private void modifyExtId(Location location, Form form) {

        String newExtId = generateUniqueExtId(form.getLocationExtId());
        location.setExtId(newExtId);

    }

    private String generateUniqueExtId(String currentExtId) {

        String duplicateLocationSuffix = "-d";
        int sequenceNumber = 1;
        // -d                   // 1
        while (null != locationService.getByExtId(currentExtId+duplicateLocationSuffix+sequenceNumber)) {
            sequenceNumber++;
        }

        duplicateLocationSuffix = duplicateLocationSuffix+sequenceNumber;

        return currentExtId+duplicateLocationSuffix;

    }

    private void copyFormDataToLocation(Form form, Location location){

        // fieldWorker, CollectedDateTime, and HierarchyLevel are set outside of this method
        location.setUuid(form.getUuid());
        location.setCommunityName(form.getCommunityName());
        location.setCommunityCode(form.getCommunityCode());
        location.setLocalityName(form.getLocalityName());
        location.setMapAreaName(form.getMapAreaName());
        location.setSectorName(form.getSectorName());
        location.setLocationName(form.getLocationName());
        location.setLocationType(nullTypeToUrb(form.getLocationType()));
        location.setBuildingNumber(digitsOnly(form.getBuildingNumber()));
        location.setFloorNumber(digitsOnly(form.getFloorNumber()));
        location.setDescription(form.getDescription());
        location.setLongitude(form.getLongitude());
        location.setLatitude(form.getLatitude());
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

    private LocationHierarchy createSector(Form form) throws ConstraintViolations {
        LocationHierarchy locationHierarchy = new LocationHierarchy();

        locationHierarchy.setExtId(form.getHierarchyExtId());
        locationHierarchy.setUuid(form.getHierarchyUuid());
        locationHierarchy.setName(form.getSectorName());

        String parentUuid = form.getHierarchyParentUuid();
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

        @XmlElement(name = "entity_ext_id")
        private String entityExtId;

        @XmlElement(name = "processed_by_mirth")
        private boolean processedByMirth;

        @XmlElement(name = "field_worker_ext_id")
        private String fieldWorkerExtId;

        @XmlElement(name = "field_worker_uuid")
        private String fieldWorkerUuid;

        @XmlElement(name = "collection_date_time")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar collectionDateTime;

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

        @XmlElement(name = "community_name")
        private String communityName;

        @XmlElement(name = "community_code")
        private String communityCode;

        @XmlElement(name = "map_area_name")
        private String mapAreaName;

        @XmlElement(name = "locality_name")
        private String localityName;

        @XmlElement(name = "sector_name")
        private String sectorName;

        @XmlElement(name = "location_building_number")
        private String buildingNumber;

        @XmlElement(name = "location_floor_number")
        private String floorNumber;

        @XmlElement(name = "description")
        private String description;

        @XmlElement(name = "longitude")
        private String longitude;

        @XmlElement(name = "latitude")
        private String latitude;

        public String getHierarchyParentUuid() {
            return hierarchyParentUuid;
        }

        public void setHierarchyParentUuid(String hierarchyParentUuid) {
            this.hierarchyParentUuid = hierarchyParentUuid;
        }

        public String getEntityExtId() {
            return entityExtId;
        }

        public void setEntityExtId(String entityExtId) {
            this.entityExtId = entityExtId;
        }

        public String getFieldWorkerUuid() {
            return fieldWorkerUuid;
        }

        public void setFieldWorkerUuid(String fieldWorkerUuid) {
            this.fieldWorkerUuid = fieldWorkerUuid;
        }

        public String getHierarchyUuid() {
            return hierarchyUuid;
        }

        public void setHierarchyUuid(String hierarchyUuid) {
            this.hierarchyUuid = hierarchyUuid;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getBuildingNumber() {
            return buildingNumber;
        }

        public void setBuildingNumber(String buildingNumber) {
            this.buildingNumber = buildingNumber;
        }

        public String getFloorNumber() {
            return floorNumber;
        }

        public void setFloorNumber(String floorNumber) {
            this.floorNumber = floorNumber;
        }

        public boolean isProcessedByMirth() {
            return processedByMirth;
        }

        public void setProcessedByMirth(boolean processedByMirth) {
            this.processedByMirth = processedByMirth;
        }

        public String getFieldWorkerExtId() {
            return fieldWorkerExtId;
        }

        public void setFieldWorkerExtId(String fieldWorkerExtId) {
            this.fieldWorkerExtId = fieldWorkerExtId;
        }

        public Calendar getCollectionDateTime() {
            return collectionDateTime;
        }

        public void setCollectionDateTime(Calendar collectionDateTime) {
            this.collectionDateTime = collectionDateTime;
        }

        public String getHierarchyExtId() {
            return hierarchyExtId;
        }

        public void setHierarchyExtId(String hierarchyExtId) {
            this.hierarchyExtId = hierarchyExtId;
        }

        public String getLocationExtId() {
            return locationExtId;
        }

        public void setLocationExtId(String locationExtId) {
            this.locationExtId = locationExtId;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        public String getLocationType() {
            return locationType;
        }

        public void setLocationType(String locationType) {
            this.locationType = locationType;
        }

        public String getCommunityName() {
            return communityName;
        }

        public void setCommunityName(String communityName) {
            this.communityName = communityName;
        }

        public String getCommunityCode() {
            return communityCode;
        }

        public void setCommunityCode(String communityCode) {
            this.communityCode = communityCode;
        }

        public String getMapAreaName() {
            return mapAreaName;
        }

        public void setMapAreaName(String mapAreaName) {
            this.mapAreaName = mapAreaName;
        }

        public String getLocalityName() {
            return localityName;
        }

        public void setLocalityName(String localityName) {
            this.localityName = localityName;
        }

        public String getSectorName() {
            return sectorName;
        }

        public void setSectorName(String sectorName) {
            this.sectorName = sectorName;
        }
    }
}
