package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.ResidencyService;
import org.openhds.controller.service.SocialGroupService;
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

    private static final String MALE = "M";
    private static final String FEMALE = "F";
    private static final String UNKNOWN_EXTID = "UNK";

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private LocationHierarchyService locationHierarchyService;

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

        // TODO: this is a temporary fix until we update the tablet to work around
        // TODO: ODK Collect form field "relevancy" limitations
        if (null == individualForm.getIndividualRelationshipToHeadOfHousehold()
                || "null".equals(individualForm.getIndividualRelationshipToHeadOfHousehold())) {
            individualForm.setIndividualRelationshipToHeadOfHousehold(HEAD_OF_HOUSEHOLD_SELF);
        }

        ConstraintViolations cv = new ConstraintViolations();

        // collected when?
        Calendar collectionTime = individualForm.getCollectionDateTime();
        if (null == collectionTime) {
            collectionTime = getDateInPast();
        }

        // inserted when?
        Calendar insertTime = Calendar.getInstance();

        // collected by whom?
        FieldWorker collectedBy = fieldWorkerService.findFieldWorkerById(individualForm.getFieldWorkerExtId());
        if (null == collectedBy) {
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_EXT_ID
                    + ": IndividualForm has a nonexistent field worker id - "
                    + individualForm.getFieldWorkerExtId());
            String errorDataPayload = createDTOPayload(individualForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    IndividualForm.class.getSimpleName(), null,
                    ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }

        // where are we?
        Location location;
        try {
            location = locationHierarchyService
                    .findLocationById(individualForm.getHouseholdExtId());
            if (null == location) {
                cv.addViolations(ConstraintViolations.INVALID_LOCATION_EXT_ID
                        + ": IndividualForm has a nonexistent location id -"
                        + individualForm.getHouseholdExtId());
                String errorDataPayload = createDTOPayload(individualForm);
                ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                        IndividualForm.class.getSimpleName(), collectedBy,
                        ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
                errorService.logError(error);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error getting location: " + e.getMessage());
        }

        // persist the location
        try {
            createOrSaveLocation(location);
        } catch (ConstraintViolations e) {
            String errorDataPayload = createDTOPayload(individualForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    IndividualForm.class.getSimpleName(), collectedBy,
                    ErrorConstants.UNRESOLVED_ERROR_STATUS, e.getViolations());
            errorService.logError(error);
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
                String errorDataPayload = createDTOPayload(individualForm);
                ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                        IndividualForm.class.getSimpleName(), collectedBy,
                        ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
                errorService.logError(error);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error finding or creating individual: " + e.getMessage());
        }

        // individual's residency at location
        findOrMakeResidency(individual, location, collectionTime, insertTime, collectedBy);

        // persist the individual, cascade to residency
        try {
            createOrSaveIndividual(individual);
        } catch (ConstraintViolations e) {
            String errorDataPayload = createDTOPayload(individualForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    IndividualForm.class.getSimpleName(), collectedBy,
                    ErrorConstants.UNRESOLVED_ERROR_STATUS, e.getViolations());
            errorService.logError(error);
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
            socialGroup = findOrMakeSocialGroup(individualForm.getHouseholdExtId(), individual, insertTime, collectedBy);

            // name the location after the head of household
            location.setLocationName(individual.getLastName());

        } else {

            // household must already exist for household member
            try {
                socialGroup = socialGroupService.findSocialGroupById(
                        individualForm.getHouseholdExtId(), "Social group does not exist: "
                                + individualForm.getHouseholdExtId());
            } catch (Exception e) {
                cv.addViolations(e.getMessage()+": "+individualForm.getHouseholdExtId());
                String errorDataPayload = createDTOPayload(individualForm);
                ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                        IndividualForm.class.getSimpleName(), collectedBy,
                        ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
                errorService.logError(error);
                return requestError(cv);
            }
        }

        // individual's relationship with group head
        findOrMakeRelationship(individual, socialGroup.getGroupHead(), collectedBy, collectionTime, insertTime,
                individualForm.getIndividualRelationshipToHeadOfHousehold());

        // persist the socialGroup, cascade to through individual to relationship
        try {
            createOrSaveSocialGroup(socialGroup);
        } catch (ConstraintViolations e) {
            String errorDataPayload = createDTOPayload(individualForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    IndividualForm.class.getSimpleName(), collectedBy,
                    ErrorConstants.UNRESOLVED_ERROR_STATUS, e.getViolations());
            errorService.logError(error);
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving socialGroup: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving socialGroup: " + e.getMessage());
        }

        // individual's membership in the social group
        Membership membership = findOrMakeMembership(individual, socialGroup, collectedBy,
                collectionTime, insertTime, individualForm.getIndividualRelationshipToHeadOfHousehold());
        try {
            entityService.create(membership);
        } catch (ConstraintViolations constraintViolations) {
            return serverError("ConstraintViolations saving membership: " + constraintViolations.getMessage());
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving membership: " + e.getMessage());
        }

        return new ResponseEntity<IndividualForm>(individualForm, HttpStatus.CREATED);
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

    private Individual makeUnknownParent(String gender) {
        Individual parent = new Individual();
        parent.setGender(gender);
        parent.setExtId(UNKNOWN_EXTID);
        parent.setDob(getDateInPast());

        return parent;
    }

    private static Calendar getDateInPast() {
        Calendar inPast = Calendar.getInstance();
        inPast.set(1900, Calendar.JANUARY, 1);
        return inPast;
    }

    private SocialGroup findOrMakeSocialGroup(String socialGroupExtId, Individual head, Calendar insertTime,
                                              FieldWorker collectedBy) {

        // TODO: SocialGroupService should not force us to use try/catch for
        // flow control
        SocialGroup socialGroup;
        try {
            // update existing social group with head and head's name
            socialGroup = socialGroupService.findSocialGroupById(socialGroupExtId,
                    "Social group does not exist: " + socialGroupExtId);

        } catch (Exception e) {
            // make a new social group
            socialGroup = new SocialGroup();
            socialGroup.setExtId(socialGroupExtId);
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
                                            Calendar collectionTime, Calendar insertTime, String membershipType) {

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
            AbstractEntityCrudHelperImpl.setEntityUuidIfNull(membership);
        }

        // fill in or update
        membership.setIndividual(individual);
        membership.setSocialGroup(socialGroup);
        membership.setCollectedBy(collectedBy);
        membership.setInsertDate(insertTime);
        membership.setStartDate(collectionTime);
        membership.setStartType(START_TYPE);
        membership.setEndType(NOT_APPLICABLE_END_TYPE);
        membership.setbIsToA(membershipType);


        // attach to individual
        individual.getAllMemberships().add(membership);

        return membership;
    }

    private Relationship findOrMakeRelationship(Individual individualA, Individual individualB, FieldWorker collectedBy,
                                                Calendar collectionTime, Calendar insertTime, String aIsToB) {

        Relationship relationship = null;

        // get relationships where this individualA acts as relationship individualA
        for (Relationship r : individualA.getAllRelationships1()) {
            if (r.getIndividualB().equals(individualB)) {
                relationship = r;
                break;
            }
        }

        // might need a brand new memebership
        if (null == relationship) {
            relationship = new Relationship();
            AbstractEntityCrudHelperImpl.setEntityUuidIfNull(relationship);
        }

        // fill in or update
        relationship.setIndividualA(individualA);
        relationship.setIndividualB(individualB);
        relationship.setCollectedBy(collectedBy);
        relationship.setInsertDate(insertTime);
        relationship.setStartDate(collectionTime);
        relationship.setaIsToB(aIsToB);


        // attach to individual
        individualA.getAllRelationships1().add(relationship);

        return relationship;
    }

    private void createOrSaveLocation(Location location) throws ConstraintViolations, SQLException {
        if (null == locationHierarchyService.findLocationById(location.getExtId())) {
            locationHierarchyService.createLocation(location);
        } else {
            entityService.save(location);
        }
    }

    private void createOrSaveSocialGroup(SocialGroup socialGroup) throws ConstraintViolations,
            SQLException {
        // TODO: SocialGroupService should not force us to use try/catch for flow control
        try {
            String socialGroupExtId = socialGroup.getExtId();
            socialGroupService.findSocialGroupById(socialGroupExtId,
                    "Social group does not exist: " + socialGroupExtId);
            entityService.save(socialGroup);

        } catch (Exception e) {
            socialGroupService.createSocialGroup(socialGroup);
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
