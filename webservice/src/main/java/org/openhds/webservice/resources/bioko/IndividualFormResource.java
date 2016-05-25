package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.ResidencyService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.LocationService;
import org.openhds.controller.service.refactor.SocialGroupService;
import org.openhds.controller.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import org.openhds.domain.annotations.Description;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Relationship;
import org.openhds.domain.model.Residency;
import org.openhds.domain.model.SocialGroup;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Controller
@RequestMapping("/individualForm")
public class IndividualFormResource extends AbstractFormResource {
    private static final Logger logger = LoggerFactory.getLogger(IndividualFormResource.class);

    // TODO: value codes can be configured by projects
    private static final String HEAD_OF_HOUSEHOLD_SELF = "1";
    private static final String HOUSEHOLD_GROUP_TYPE = "COH";
    private static final String START_TYPE = "Form";

    //data model assumes that all newly created Memberships have an endType of "NA"
    private static final String NOT_APPLICABLE_END_TYPE = "NA";

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ResidencyService residencyService;

    @Autowired
    private SocialGroupService socialGroupService;

    @Autowired
    private CalendarAdapter adapter;

    private Marshaller marshaller = null;

    // This individual form should cause several CRUDS:
    // location, individual, socialGroup, residency, membership, relationship
    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form)
            throws JAXBException {

        try {
            JAXBContext context = JAXBContext.newInstance(Form.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        List<String> logMessage = new ArrayList<>();

        // Clean up "null" strings created by Mirth
        if ("null".equals(form.getIndividualRelationshipToHeadOfHousehold())) {
            form.setIndividualRelationshipToHeadOfHousehold(null);
        }

        if ("null".equals(form.getHouseholdUuid())) {
            form.setHouseholdUuid(null);
        }

        if ("null".equals(form.getSocialgroupUuid())) {
            form.setSocialgroupUuid(null);
        }

        if ("null".equals(form.getRelationshipUuid())) {
            form.setRelationshipUuid(null);
        }

        if ("null".equals(form.getMembershipUuid())) {
            form.setMembershipUuid(null);
        }

        // Default relationship to head of household is "self"
        if (null == form.getIndividualRelationshipToHeadOfHousehold()) {
            form.setIndividualRelationshipToHeadOfHousehold(HEAD_OF_HOUSEHOLD_SELF);
        }

        // collected when?
        Calendar collectionTime = form.getCollectionDateTime();
        if (null == collectionTime) {
            collectionTime = getDateInPast();
        }

        // inserted when?
        Calendar insertTime = Calendar.getInstance();

        // collected by whom?
        ConstraintViolations cv = new ConstraintViolations();
        FieldWorker collectedBy = fieldWorkerService.getByUuid(form.getFieldWorkerUuid());
        if (null == collectedBy) {
            cv.addViolations("Field Worker does not exist");
            logError(cv, null, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_FIELD_WORKER_UUID);
            return requestError(cv);
        }

        // where are we?
        Location location;
        try {
            // Get location by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = form.getHouseholdUuid();
            if (null == uuid) {
                location = locationService.getByExtId(form.getHouseholdExtId());
            } else {
                location = locationService.getByUuid(uuid);
            }

            if (null == location) {
                String errorMessage = "Location does not exist "+ form.getHouseholdUuid()+" / "+ form.getHouseholdExtId();
                cv.addViolations(errorMessage);
                logError(cv, collectedBy, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_LOCATION_UUID);
                return requestError(errorMessage);
            }

        } catch (Exception e) {
            return requestError("Error getting location: " + e.getMessage());
        }

        // persist the location
        try {
            createOrSaveLocation(location);
        } catch (ConstraintViolations e) {
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving location: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving location: " + e.getMessage());
        }

        // make a new individual, to be persisted below
        Individual individual;
        try {
            individual = findOrMakeIndividual(form, collectedBy, insertTime, cv);
            if (cv.hasViolations()) {
                logError(cv, collectedBy, createDTOPayload(form), Form.LOG_NAME, ErrorConstants.CONSTRAINT_VIOLATION);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error finding or creating individual: " + e.getMessage());
        }

        // change the individual's extId if the server has previously changed the extId of their location/household
        if (!form.getHouseholdExtId().equalsIgnoreCase(location.getExtId())) {

            updateIndividualExtId(individual, location);

            // log the modification
            cv.addViolations("Individual ExtId updated from "+ form.getIndividualExtId()+" to "+individual.getExtId());
            logError(cv, collectedBy, createDTOPayload(form), Form.LOG_NAME, ErrorConstants.MODIFIED_EXTID);

            //household extId used later by social group, need to correct it here
            form.setHouseholdExtId(location.getExtId());
        }

        // log a warning if the individual extId clashes with an existing individual's extId
        if (0 != individualService.getExistingExtIdCount(individual.getExtId())) {
            // log the modification
            cv.addViolations("Warning: Individual ExtId clashes with an existing Individual's extId : "+individual.getExtId());
            logError(cv, collectedBy, createDTOPayload(form), Form.LOG_NAME, ErrorConstants.DUPLICATE_EXTID);
        }

        // individual's residency at location
        findOrMakeResidency(individual, location, collectionTime, insertTime, collectedBy);

        // persist the individual, cascade to residency
        try {
            createOrSaveIndividual(individual);
        } catch (ConstraintViolations e) {
            logError(e, collectedBy, createDTOPayload(form), Form.LOG_NAME, ErrorConstants.CONSTRAINT_VIOLATION);
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving individual: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving individual: " + e.getMessage());
        }

        SocialGroup socialGroup;
        if (form.getIndividualRelationshipToHeadOfHousehold().equals(HEAD_OF_HOUSEHOLD_SELF)) {

            // may create social group for head of household
            socialGroup = findOrMakeSocialGroup(form, location, individual, insertTime, collectedBy);

            // name the location after the head of household
            location.setLocationName(individual.getLastName());

        } else {
            // household must already exist for household member

            // Get social group by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = form.getSocialgroupUuid();
            if (null == uuid) {
                socialGroup = socialGroupService.getByExtId(form.getHouseholdExtId());
            } else {
                socialGroup = socialGroupService.getByUuid(uuid);
            }
        }

        // individual's relationship with group
        findOrMakeRelationship(individual, socialGroup.getGroupHead(), collectedBy, collectionTime, insertTime,
                form);

        // persist the socialGroup, cascade to through individual to relationship
        try {
            createOrSaveSocialGroup(socialGroup);
        } catch (ConstraintViolations e) {
            logError(e, collectedBy, createDTOPayload(form), Form.LOG_NAME, ErrorConstants.CONSTRAINT_VIOLATION);
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving socialGroup: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving socialGroup: " + e.getMessage());
        }

        // individual's membership in the social group
        Membership membership = findOrMakeMembership(individual, socialGroup, collectedBy,
                collectionTime, insertTime, form);
        try {
            entityService.create(membership);
        } catch (ConstraintViolations constraintViolations) {
            logError(constraintViolations, collectedBy, createDTOPayload(form), Form.LOG_NAME, ErrorConstants.CONSTRAINT_VIOLATION);
            return serverError("ConstraintViolations saving membership: " + constraintViolations.getMessage());
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving membership: " + e.getMessage());
        }

        return new ResponseEntity<>(form, HttpStatus.CREATED);
    }

    private void updateIndividualExtId(Individual individual, Location location) {

        // -001
        String individualSuffixSequence = individual.getExtId().substring(individual.getExtId().length() - 4);

        // M1000S57E02P1-001
        individual.setExtId(location.getExtId()+individualSuffixSequence);

    }

    private Individual findOrMakeIndividual(Form form, FieldWorker collectedBy,
                                            Calendar insertTime, ConstraintViolations cv) throws Exception {

        Individual individual = individualService.getByUuid(form.getUuid());
        if (null == individual) {
            individual = new Individual();
        }

        individual.setCollectedBy(collectedBy);
        individual.setInsertDate(insertTime);

        copyFormDataToIndividual(form, individual);

        // Bioko project forms don't include parents!
        individual.setMother(individualService.getUnknownIndividual());
        individual.setFather(individualService.getUnknownIndividual());

        return individual;
    }

    private void copyFormDataToIndividual(Form form, Individual individual)
            throws Exception {
        if (null == individual.getUuid()) {
            individual.setUuid(form.getUuid());
        }
        individual.setExtId(form.getIndividualExtId());
        individual.setFirstName(form.getIndividualFirstName());
        individual.setMiddleName(form.getIndividualOtherNames());
        individual.setLastName(form.getIndividualLastName());
        individual.setGender(form.getIndividualGender());

        Calendar dob = form.getIndividualDateOfBirth();
        if (null == dob) {
            dob = getDateInPast();
        }
        individual.setDob(dob);
        individual.setAge(form.getIndividualAge());
        individual.setAgeUnits(form.getIndividualAgeUnits());
        individual.setPhoneNumber(form.getIndividualPhoneNumber());
        individual.setOtherPhoneNumber(form.getIndividualOtherPhoneNumber());
        individual.setLanguagePreference(form.getIndividualLanguagePreference());
        individual.setPointOfContactName(form.getIndividualPointOfContactName());
        individual.setPointOfContactPhoneNumber(form.getIndividualPointOfContactPhoneNumber());
        individual.setDip(form.getIndividualDip());
        individual.setMemberStatus(form.getIndividualMemberStatus());
        individual.setNationality(form.getIndividualNationality());

    }



    private SocialGroup findOrMakeSocialGroup(Form form, Location location, Individual head,
                                              Calendar insertTime, FieldWorker collectedBy) {

        // Get social group by uuid.
        // Fall back on extId if uuid is missing, which allows us to re-process older forms.
        SocialGroup socialGroup;
        String uuid = form.getSocialgroupUuid();
        if (null == uuid) {
            socialGroup = socialGroupService.getByExtId(form.getHouseholdExtId());
        } else {
            socialGroup = socialGroupService.getByUuid(uuid);
        }

        if (null == socialGroup) {
            // make a new social group
            socialGroup = new SocialGroup();
            socialGroup.setUuid(uuid);
            socialGroup.setExtId(location.getExtId());
            socialGroup.setLocation(location);
            socialGroup.setCollectedBy(collectedBy);
            socialGroup.setInsertDate(insertTime);
            AbstractEntityCrudHelperImpl.setEntityUuidIfNull(socialGroup);
        }

        socialGroup.setGroupHead(head);
        socialGroup.setGroupName(head.getLastName());
        socialGroup.setGroupType(HOUSEHOLD_GROUP_TYPE);

        return socialGroup;
    }

    private Residency findOrMakeResidency(Individual individual, Location location,  Calendar collectionTime,
                                          Calendar insertTime, FieldWorker collectedBy) {

        Residency residency = null;

        // try to find an existing residency to modify
        if (residencyService.hasOpenResidency(individual)) {
            List<Residency> allResidencies = residencyService.getAllResidencies(individual);
            for (Residency r : allResidencies) {
                if (location.equals(r.getLocation())) {
                    residency = r;
                    break;
                }
            }
        }

        // might need to make a new residency
        if (null == residency) {
            residency = new Residency();
            AbstractEntityCrudHelperImpl.setEntityUuidIfNull(residency);
        }

        // fill in or update
        residency.setIndividual(individual);
        residency.setLocation(location);
        residency.setCollectedBy(collectedBy);
        residency.setInsertDate(insertTime);
        residency.setStartDate(collectionTime);
        residency.setStartType(START_TYPE);
        residency.setEndType(NOT_APPLICABLE_END_TYPE);

        // attach to individial
        individual.getAllResidencies().add(residency);

        return residency;
    }

    private Membership findOrMakeMembership(Individual individual, SocialGroup socialGroup,  FieldWorker collectedBy,
                                            Calendar collectionTime, Calendar insertTime, Form form) {

        Membership membership = null;


        // try to find existing membership
        for (Membership m : individual.getAllMemberships()) {
            if (m.getSocialGroup().equals(socialGroup)) {
                membership = m;
            }
        }

        // might need a brand new membership
        if (null == membership) {
            membership = new Membership();
            membership.setUuid(form.getMembershipUuid());
        }

        // fill in or update
        membership.setIndividual(individual);
        membership.setSocialGroup(socialGroup);
        membership.setCollectedBy(collectedBy);
        membership.setInsertDate(insertTime);
        membership.setStartDate(collectionTime);
        membership.setStartType(START_TYPE);
        membership.setEndType(NOT_APPLICABLE_END_TYPE);
        membership.setbIsToA(form.getIndividualRelationshipToHeadOfHousehold());


        // attach to individual
        individual.getAllMemberships().add(membership);

        return membership;
    }

    private Relationship findOrMakeRelationship(Individual individualA, Individual individualB, FieldWorker collectedBy,
                                                Calendar collectionTime, Calendar insertTime, Form form) {

        Relationship relationship = null;

        // get relationships where this individualA acts as relationship individualA
        for (Relationship r : individualA.getAllRelationships1()) {
            if (r.getIndividualB().equals(individualB)) {
                relationship = r;
                break;
            }
        }

        // might need a brand new relationship
        if (null == relationship) {
            relationship = new Relationship();
            relationship.setUuid(form.getRelationshipUuid());
        }

        // fill in or update
        relationship.setIndividualA(individualA);
        relationship.setIndividualB(individualB);
        relationship.setCollectedBy(collectedBy);
        relationship.setInsertDate(insertTime);
        relationship.setStartDate(collectionTime);
        relationship.setaIsToB(form.getIndividualRelationshipToHeadOfHousehold());

        // attach to individual
        individualA.getAllRelationships1().add(relationship);

        return relationship;
    }

    private void createOrSaveLocation(Location location) throws ConstraintViolations, SQLException {
        if (null == locationService.getByUuid(location.getUuid())) {
            locationService.create(location);
        } else {
            locationService.save(location);
        }
    }

    private void createOrSaveSocialGroup(SocialGroup socialGroup) throws ConstraintViolations,
            SQLException {

        SocialGroup sg = socialGroupService.getByUuid(socialGroup.getUuid());

        if (null == sg) {
            socialGroupService.create(socialGroup);
        } else {
            socialGroupService.save(socialGroup);
        }

    }

    private void createOrSaveIndividual(Individual individual) throws ConstraintViolations,
            SQLException {
        if (null == individualService.getByUuid(individual.getUuid())) {
            individualService.create(individual);
        } else {
            individualService.save(individual);
        }
    }

    private String createDTOPayload(Form form) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();
    }


    @Description(description = "Model data from the Individual xform for the Bioko island project.  Contains Individual, Relationship, and Membership data.")
    @XmlRootElement(name = "individualForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "IndividualForm";

        private static final long serialVersionUID = 1143017330340385847L;

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

        //individual form fields
        @XmlElement(name = "household_ext_id")
        private String householdExtId;

        @XmlElement(name = "household_uuid")
        private String householdUuid;

        @XmlElement(name = "membership_uuid")
        private String membershipUuid;

        @XmlElement(name = "relationship_uuid")
        private String relationshipUuid;

        @XmlElement(name = "socialgroup_uuid")
        private String socialgroupUuid;

        @XmlElement(name = "individual_ext_id")
        private String individualExtId;

        @XmlElement(name = "individual_first_name")
        private String individualFirstName;

        @XmlElement(name = "individual_last_name")
        private String individualLastName;

        @XmlElement(name = "individual_other_names")
        private String individualOtherNames;

        @XmlElement(name = "individual_age")
        private int individualAge;

        @XmlElement(name = "individual_age_units")
        private String individualAgeUnits;

        @XmlElement(name = "individual_date_of_birth")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar individualDateOfBirth;

        @XmlElement(name = "individual_gender")
        private String individualGender;

        @XmlElement(name = "individual_relationship_to_head_of_household")
        private String individualRelationshipToHeadOfHousehold;

        @XmlElement(name = "individual_phone_number")
        private String individualPhoneNumber;

        @XmlElement(name = "individual_other_phone_number")
        private String individualOtherPhoneNumber;

        @XmlElement(name = "individual_language_preference")
        private String individualLanguagePreference;

        @XmlElement(name = "individual_point_of_contact_name")
        private String individualPointOfContactName;

        @XmlElement(name = "individual_point_of_contact_phone_number")
        private String individualPointOfContactPhoneNumber;

        @XmlElement(name = "individual_dip")
        private int individualDip;

        @XmlElement(name = "individual_member_status")
        private String individualMemberStatus;

        @XmlElement(name = "individual_nationality")
        private String individualNationality;


        public String getEntityExtId() {
            return entityExtId;
        }

        public void setEntityExtId(String entityExtId) {
            this.entityExtId = entityExtId;
        }

        public String getMembershipUuid() {
            return membershipUuid;
        }

        public void setMembershipUuid(String membershipUuid) {
            this.membershipUuid = membershipUuid;
        }

        public String getRelationshipUuid() {
            return relationshipUuid;
        }

        public void setRelationshipUuid(String relationshipUuid) {
            this.relationshipUuid = relationshipUuid;
        }

        public String getSocialgroupUuid() {
            return socialgroupUuid;
        }

        public void setSocialgroupUuid(String socialgroupUuid) {
            this.socialgroupUuid = socialgroupUuid;
        }

        public String getFieldWorkerUuid() {
            return fieldWorkerUuid;
        }

        public void setFieldWorkerUuid(String fieldWorkerUuid) {
            this.fieldWorkerUuid = fieldWorkerUuid;
        }

        public String getHouseholdUuid() {
            return householdUuid;
        }

        public void setHouseholdUuid(String householdUuid) {
            this.householdUuid = householdUuid;
        }


        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
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

        public String getHouseholdExtId() {
            return householdExtId;
        }

        public void setHouseholdExtId(String householdExtId) {
            this.householdExtId = householdExtId;
        }

        public String getIndividualExtId() {
            return individualExtId;
        }

        public void setIndividualExtId(String individualExtId) {
            this.individualExtId = individualExtId;
        }

        public String getIndividualFirstName() {
            return individualFirstName;
        }

        public void setIndividualFirstName(String individualFirstName) {
            this.individualFirstName = individualFirstName;
        }

        public String getIndividualLastName() {
            return individualLastName;
        }

        public void setIndividualLastName(String individualLastName) {
            this.individualLastName = individualLastName;
        }

        public String getIndividualOtherNames() {
            return individualOtherNames;
        }

        public void setIndividualOtherNames(String individualOtherNames) {
            this.individualOtherNames = individualOtherNames;
        }

        public int getIndividualAge() {
            return individualAge;
        }

        public void setIndividualAge(int individualAge) {
            this.individualAge = individualAge;
        }

        public String getIndividualAgeUnits() {
            return individualAgeUnits;
        }

        public void setIndividualAgeUnits(String individualAgeUnits) {
            this.individualAgeUnits = individualAgeUnits;
        }

        public Calendar getIndividualDateOfBirth() {
            return individualDateOfBirth;
        }

        public void setIndividualDateOfBirth(Calendar individualDateOfBirth) {
            this.individualDateOfBirth = individualDateOfBirth;
        }

        public String getIndividualGender() {
            return individualGender;
        }

        public void setIndividualGender(String individualGender) {
            this.individualGender = individualGender;
        }

        public String getIndividualRelationshipToHeadOfHousehold() {
            return individualRelationshipToHeadOfHousehold;
        }

        public void setIndividualRelationshipToHeadOfHousehold(
                String individualRelationshipToHeadOfHousehold) {
            this.individualRelationshipToHeadOfHousehold = individualRelationshipToHeadOfHousehold;
        }

        public String getIndividualPhoneNumber() {
            return individualPhoneNumber;
        }

        public void setIndividualPhoneNumber(String individualPhoneNumber) {
            this.individualPhoneNumber = individualPhoneNumber;
        }

        public String getIndividualOtherPhoneNumber() {
            return individualOtherPhoneNumber;
        }

        public void setIndividualOtherPhoneNumber(String individualOtherPhoneNumber) {
            this.individualOtherPhoneNumber = individualOtherPhoneNumber;
        }

        public String getIndividualLanguagePreference() {
            return individualLanguagePreference;
        }

        public void setIndividualLanguagePreference(String individualLanguagePreference) {
            this.individualLanguagePreference = individualLanguagePreference;
        }

        public String getIndividualPointOfContactName() {
            return individualPointOfContactName;
        }

        public void setIndividualPointOfContactName(String individualPointOfContactName) {
            this.individualPointOfContactName = individualPointOfContactName;
        }

        public String getIndividualPointOfContactPhoneNumber() {
            return individualPointOfContactPhoneNumber;
        }

        public void setIndividualPointOfContactPhoneNumber(String individualPointOfContactPhoneNumber) {
            this.individualPointOfContactPhoneNumber = individualPointOfContactPhoneNumber;
        }

        public int getIndividualDip() {
            return individualDip;
        }

        public void setIndividualDip(int individualDip) {
            this.individualDip = individualDip;
        }

        public String getIndividualMemberStatus() {
            return individualMemberStatus;
        }

        public void setIndividualMemberStatus(String individualMemberStatus) {
            this.individualMemberStatus = individualMemberStatus;
        }

        public String getIndividualNationality() {
            return individualNationality;
        }

        public void setIndividualNationality(String individualNationality) {
            this.individualNationality = individualNationality;
        }
    }
}
