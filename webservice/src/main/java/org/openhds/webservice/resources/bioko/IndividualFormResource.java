package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.ResidencyService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.LocationService;
import org.openhds.controller.service.refactor.SocialGroupService;
import org.openhds.controller.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import org.openhds.domain.model.ErrorLog;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Relationship;
import org.openhds.domain.model.Residency;
import org.openhds.domain.model.SocialGroup;
import org.openhds.domain.model.bioko.IndividualForm;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.errorhandling.util.ErrorLogUtil;
import org.openhds.webservice.FieldBuilder;
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
    private static final String START_TYPE = "IndividualForm";

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
    private FieldBuilder fieldBuilder;

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private Marshaller marshaller = null;

    // This individual form should cause several CRUDS:
    // location, individual, socialGroup, residency, membership, relationship
    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody IndividualForm individualForm)
            throws JAXBException {

        try {
            JAXBContext context = JAXBContext.newInstance(IndividualForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        List<String> logMessage = new ArrayList<String>();

        // Clean up "null" strings created by Mirth
        if ("null".equals(individualForm.getIndividualRelationshipToHeadOfHousehold())) {
            individualForm.setIndividualRelationshipToHeadOfHousehold(null);
        }

        if ("null".equals(individualForm.getHouseholdUuid())) {
            individualForm.setHouseholdUuid(null);
        }

        if ("null".equals(individualForm.getSocialgroupUuid())) {
            individualForm.setSocialgroupUuid(null);
        }

        if ("null".equals(individualForm.getRelationshipUuid())) {
            individualForm.setRelationshipUuid(null);
        }

        if ("null".equals(individualForm.getMembershipUuid())) {
            individualForm.setMembershipUuid(null);
        }

        // Default relationship to head of household is "self"
        if (null == individualForm.getIndividualRelationshipToHeadOfHousehold()) {
            individualForm.setIndividualRelationshipToHeadOfHousehold(HEAD_OF_HOUSEHOLD_SELF);
        }

        // collected when?
        Calendar collectionTime = individualForm.getCollectionDateTime();
        if (null == collectionTime) {
            collectionTime = getDateInPast();
        }

        // inserted when?
        Calendar insertTime = Calendar.getInstance();

        // collected by whom?
        ConstraintViolations cv = new ConstraintViolations();
        FieldWorker collectedBy = fieldWorkerService.getByUuid(individualForm.getFieldWorkerUuid());
        if (null == collectedBy) {
            cv.addViolations("Field Worker does not exist");
            logError(cv, null, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ConstraintViolations.INVALID_FIELD_WORKER_UUID);
            return requestError(cv);
        }

        // where are we?
        Location location;
        try {
            // Get location by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = individualForm.getHouseholdUuid();
            if (null == uuid) {
                location = locationService.getByExtId(individualForm.getHouseholdExtId());
            } else {
                location = locationService.getByUuid(uuid);
            }

            if (null == location) {
                String errorMessage = "Location does not exist "+individualForm.getHouseholdUuid()+" / "+individualForm.getHouseholdExtId();
                cv.addViolations(errorMessage);
                logError(cv, collectedBy, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ConstraintViolations.INVALID_LOCATION_UUID);
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
            individual = findOrMakeIndividual(individualForm, collectedBy, insertTime, cv);
            if (cv.hasViolations()) {
                logError(cv, collectedBy, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ErrorConstants.CONSTRAINT_VIOLATION);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error finding or creating individual: " + e.getMessage());
        }

        // change the individual's extId if the server has previously changed the extId of their location/household
        if (!individualForm.getHouseholdExtId().equalsIgnoreCase(location.getExtId())) {

            updateIndividualExtId(individual, location);

            // log the modification
            cv.addViolations("Individual ExtId updated from "+individualForm.getIndividualExtId()+" to "+individual.getExtId());
            logError(cv, collectedBy, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ErrorConstants.MODIFIED_EXTID);

            //household extId used later by social group, need to correct it here
            individualForm.setHouseholdExtId(location.getExtId());
        }

        // log a warning if the individual extId clashes with an existing individual's extId
        if (0 != individualService.getExistingExtIdCount(individual.getExtId())) {
            // log the modification
            cv.addViolations("Warning: Individual ExtId clashes with an existing Individual's extId : "+individual.getExtId());
            logError(cv, collectedBy, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ErrorConstants.DUPLICATE_EXTID);
        }

        // individual's residency at location
        findOrMakeResidency(individual, location, collectionTime, insertTime, collectedBy);

        // persist the individual, cascade to residency
        try {
            createOrSaveIndividual(individual);
        } catch (ConstraintViolations e) {
            logError(e, collectedBy, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ErrorConstants.CONSTRAINT_VIOLATION);
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving individual: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving individual: " + e.getMessage());
        }

        SocialGroup socialGroup;
        if (individualForm.getIndividualRelationshipToHeadOfHousehold().equals(
                HEAD_OF_HOUSEHOLD_SELF)) {

            // may create social group for head of household
            socialGroup = findOrMakeSocialGroup(individualForm, location, individual, insertTime, collectedBy);

            // name the location after the head of household
            location.setLocationName(individual.getLastName());

        } else {
            // household must already exist for household member

            // Get social group by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = individualForm.getSocialgroupUuid();
            if (null == uuid) {
                socialGroup = socialGroupService.getByExtId(individualForm.getHouseholdExtId());
            } else {
                socialGroup = socialGroupService.getByUuid(uuid);
            }
        }

        // individual's relationship with group
        findOrMakeRelationship(individual, socialGroup.getGroupHead(), collectedBy, collectionTime, insertTime,
                individualForm);

        // persist the socialGroup, cascade to through individual to relationship
        try {
            createOrSaveSocialGroup(socialGroup);
        } catch (ConstraintViolations e) {
            logError(e, collectedBy, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ErrorConstants.CONSTRAINT_VIOLATION);
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving socialGroup: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving socialGroup: " + e.getMessage());
        }

        // individual's membership in the social group
        Membership membership = findOrMakeMembership(individual, socialGroup, collectedBy,
                collectionTime, insertTime, individualForm);
        try {
            entityService.create(membership);
        } catch (ConstraintViolations constraintViolations) {
            logError(constraintViolations, collectedBy, createDTOPayload(individualForm), IndividualForm.class.getSimpleName(), ErrorConstants.CONSTRAINT_VIOLATION);
            return serverError("ConstraintViolations saving membership: " + constraintViolations.getMessage());
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving membership: " + e.getMessage());
        }

        return new ResponseEntity<IndividualForm>(individualForm, HttpStatus.CREATED);
    }

    private void updateIndividualExtId(Individual individual, Location location) {

        // -001
        String individualSuffixSequence = individual.getExtId().substring(individual.getExtId().length() - 4);

        // M1000S57E02P1-001
        individual.setExtId(location.getExtId()+individualSuffixSequence);

    }

    private Individual findOrMakeIndividual(IndividualForm individualForm, FieldWorker collectedBy,
                                            Calendar insertTime, ConstraintViolations cv) throws Exception {

        Individual individual = individualService.getByUuid(individualForm.getUuid());
        if (null == individual) {
            individual = new Individual();
        }

        individual.setCollectedBy(collectedBy);
        individual.setInsertDate(insertTime);

        copyFormDataToIndividual(individualForm, individual);

        // Bioko project forms don't include parents!
        Individual mother = makeUnknownParent(FEMALE);
        individual.setMother(fieldBuilder.referenceField(mother, cv, "Using Unknown Mother"));
        Individual father = makeUnknownParent(MALE);
        individual.setFather(fieldBuilder.referenceField(father, cv, "Using Unknown Father"));

        return individual;
    }

    private void copyFormDataToIndividual(IndividualForm individualForm, Individual individual)
            throws Exception {
        if (null == individual.getUuid()) {
            individual.setUuid(individualForm.getUuid());
        }
        individual.setExtId(individualForm.getIndividualExtId());
        individual.setFirstName(individualForm.getIndividualFirstName());
        individual.setMiddleName(individualForm.getIndividualOtherNames());
        individual.setLastName(individualForm.getIndividualLastName());
        individual.setGender(individualForm.getIndividualGender());

        Calendar dob = individualForm.getIndividualDateOfBirth();
        if (null == dob) {
            dob = getDateInPast();
        }
        individual.setDob(dob);
        individual.setAge(individualForm.getIndividualAge());
        individual.setAgeUnits(individualForm.getIndividualAgeUnits());
        individual.setPhoneNumber(individualForm.getIndividualPhoneNumber());
        individual.setOtherPhoneNumber(individualForm.getIndividualOtherPhoneNumber());
        individual.setLanguagePreference(individualForm.getIndividualLanguagePreference());
        individual.setPointOfContactName(individualForm.getIndividualPointOfContactName());
        individual.setPointOfContactPhoneNumber(individualForm.getIndividualPointOfContactPhoneNumber());
        individual.setDip(individualForm.getIndividualDip());
        individual.setMemberStatus(individualForm.getIndividualMemberStatus());
        individual.setNationality(individualForm.getIndividualNationality());

    }



    private SocialGroup findOrMakeSocialGroup(IndividualForm individualForm, Location location, Individual head,
                                              Calendar insertTime, FieldWorker collectedBy) {

        // Get social group by uuid.
        // Fall back on extId if uuid is missing, which allows us to re-process older forms.
        SocialGroup socialGroup;
        String uuid = individualForm.getSocialgroupUuid();
        if (null == uuid) {
            socialGroup = socialGroupService.getByExtId(individualForm.getHouseholdExtId());
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
                                            Calendar collectionTime, Calendar insertTime, IndividualForm individualForm) {

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
            membership.setUuid(individualForm.getMembershipUuid());
        }

        // fill in or update
        membership.setIndividual(individual);
        membership.setSocialGroup(socialGroup);
        membership.setCollectedBy(collectedBy);
        membership.setInsertDate(insertTime);
        membership.setStartDate(collectionTime);
        membership.setStartType(START_TYPE);
        membership.setEndType(NOT_APPLICABLE_END_TYPE);
        membership.setbIsToA(individualForm.getIndividualRelationshipToHeadOfHousehold());


        // attach to individual
        individual.getAllMemberships().add(membership);

        return membership;
    }

    private Relationship findOrMakeRelationship(Individual individualA, Individual individualB, FieldWorker collectedBy,
                                                Calendar collectionTime, Calendar insertTime, IndividualForm individualForm) {

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
            relationship.setUuid(individualForm.getRelationshipUuid());
        }

        // fill in or update
        relationship.setIndividualA(individualA);
        relationship.setIndividualB(individualB);
        relationship.setCollectedBy(collectedBy);
        relationship.setInsertDate(insertTime);
        relationship.setStartDate(collectionTime);
        relationship.setaIsToB(individualForm.getIndividualRelationshipToHeadOfHousehold());

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

    private String createDTOPayload(IndividualForm individualForm) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(individualForm, writer);
        return writer.toString();
    }
}
