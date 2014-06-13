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
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Relationship;
import org.openhds.domain.model.Residency;
import org.openhds.domain.model.SocialGroup;
import org.openhds.domain.model.bioko.IndividualForm;
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
@RequestMapping("/individualForm")
public class IndividualFormResource {
    private static final Logger logger = LoggerFactory.getLogger(IndividualFormResource.class);

    // TODO: value codes can be configured by projects
    private static final String HEAD_OF_HOUSEHOLD_SELF = "1";
    private static final String HOUSEHOLD_GROUP_TYPE = "COH";
    private static final String START_TYPE = "IndividualForm";
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

    // This individual form should cause several CRUDS:
    // location, individual, socialGroup, residency, membership, relationship
    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(
            @RequestBody IndividualForm individualForm) {

        ConstraintViolations cv = new ConstraintViolations();

        // collected when?
        Calendar collectionTime = individualForm.getCollectionDateTime();
        if (null == collectionTime) {
            collectionTime = getDateInPast();
        }

        // collected by whom?
        FieldWorker collectedBy;
        try {
            collectedBy = fieldWorkerService.findFieldWorkerById(
                    individualForm.getFieldWorkerExtId(),
                    "Individual form has nonexistent field worker id.");
        } catch (Exception e) {
            return requestError("Error getting field worker: " + e.getMessage());
        }

        // where are we?
        Location location;
        try {
            location = locationHierarchyService
                    .findLocationById(individualForm.getHouseholdExtId());
            if (null == location) {
                return requestError("Location not found: " + individualForm.getHouseholdExtId());
            }
        } catch (Exception e) {
            return requestError("Error getting location: " + e.getMessage());
        }

        // make a new individual, to be persisted below
        Individual individual;
        try {
            individual = findOrMakeIndividual(individualForm, collectedBy, cv);
            if (cv.hasViolations()) {
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error finding or creating individual: " + e.getMessage());
        }

        SocialGroup socialGroup;
        if (individualForm.getIndividualRelationshipToHeadOfHousehold().equals(
                HEAD_OF_HOUSEHOLD_SELF)) {

            // may create social group for head of household
            socialGroup = findOrMakeSocialGroup(individualForm.getHouseholdExtId(), individual,
                    collectionTime, collectedBy);

            // name the location after the head of household
            location.setLocationName(individual.getLastName());

        } else {

            // household must already exist for household member
            try {
                socialGroup = socialGroupService.findSocialGroupById(
                        individualForm.getHouseholdExtId(), "Social group does not exist: "
                                + individualForm.getHouseholdExtId());
            } catch (Exception e) {
                return requestError("Error getting social group for household member: "
                        + e.getMessage());
            }
        }

        // individual's residency at location
        Residency residency = findOrMakeResidency(individual, location, collectionTime, collectedBy);

        // individual's membership in the social group
        Membership membership = findOrMakeMembership(individual, socialGroup, collectedBy,
                collectionTime, individualForm.getIndividualRelationshipToHeadOfHousehold());

        // create relationship to head of household (may be "self")
        Relationship relationship = findOrMakeRelationship(individual, socialGroup.getGroupHead(),
                collectedBy, collectionTime,
                individualForm.getIndividualRelationshipToHeadOfHousehold());

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

        // persist the socialGroup
        try {
            createOrSaveSocialGroup(socialGroup);
        } catch (ConstraintViolations e) {
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving socialGroup: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving socialGroup: " + e.getMessage());
        }

        // persist the individual
        // which cascades to residency, membership, and relationship
        try {
            createOrSaveIndividual(individual);
        } catch (ConstraintViolations e) {
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving individual: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving individual: " + e.getMessage());
        }

        return new ResponseEntity<IndividualForm>(individualForm, HttpStatus.CREATED);
    }

    private Individual findOrMakeIndividual(IndividualForm individualForm, FieldWorker collectedBy,
                                            ConstraintViolations cv) throws Exception {
        Individual individual = individualService
                .findIndivById(individualForm.getIndividualExtId());
        if (null == individual) {
            individual = new Individual();
        }

        individual.setCollectedBy(collectedBy);

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
        inPast.set(1900, 0, 1);
        return inPast;
    }

    private SocialGroup findOrMakeSocialGroup(String socialGroupExtId, Individual head,
                                              Calendar collectionTime, FieldWorker collectedBy) {

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
            socialGroup.setInsertDate(collectionTime);
        }

        socialGroup.setGroupHead(head);
        socialGroup.setGroupName(head.getLastName());
        socialGroup.setGroupType(HOUSEHOLD_GROUP_TYPE);

        return socialGroup;
    }

    private Residency findOrMakeResidency(Individual individual, Location location,
                                          Calendar collectionTime, FieldWorker collectedBy) {

        Residency residency = null;

        // try to find an existing residency to modify
        if (residencyService.hasOpenResidency(individual)) {
            List<Residency> allResidencies = residencyService.getAllResidencies(individual);
            for (Residency r : allResidencies) {
                if (location.equals(r)) {
                    residency = r;
                    break;
                }
            }
        }

        // might need to make a new residency
        if (null == residency) {
            residency = new Residency();
        }

        // fill in or update
        residency.setIndividual(individual);
        residency.setLocation(location);
        residency.setCollectedBy(collectedBy);
        residency.setStartDate(collectionTime);
        residency.setStartType(START_TYPE);

        // attach to individial
        individual.getAllResidencies().add(residency);

        return residency;
    }

    private Membership findOrMakeMembership(Individual individual, SocialGroup socialGroup,
                                            FieldWorker collectedBy, Calendar collectionTime, String membershipType) {

        Membership membership = null;

        // try to find existing membership
        for (Membership m : individual.getAllMemberships()) {
            if (m.getSocialGroup().equals(socialGroup)) {
                membership = m;
            }
        }

        // might need a brand new memebership
        if (null == membership) {
            membership = new Membership();
        }

        // fill in or update
        membership.setIndividual(individual);
        membership.setSocialGroup(socialGroup);
        membership.setCollectedBy(collectedBy);
        membership.setStartDate(collectionTime);
        membership.setStartType(START_TYPE);
        membership.setbIsToA(membershipType);

        // attach to individual
        individual.getAllMemberships().add(membership);

        return membership;
    }

    private Relationship findOrMakeRelationship(Individual individualA, Individual individualB,
                                                FieldWorker collectedBy, Calendar collectionTime, String aIsToB) {

        Relationship relationship = null;

        // get relationships where this individualA acts as relationship
        // individualA
        for (Relationship r : individualA.getAllRelationships1()) {
            if (r.getIndividualB().equals(individualB)) {
                relationship = r;
                break;
            }
        }

        // might need a brand new memebership
        if (null == relationship) {
            relationship = new Relationship();
        }

        // fill in or update
        relationship.setIndividualA(individualA);
        relationship.setIndividualB(individualB);
        relationship.setCollectedBy(collectedBy);
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
        // TODO: SocialGroupService should not force us to use try/catch for
        // flow control
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
        if (null == individualService.findIndivById(individual.getExtId())) {
            individualService.createIndividual(individual);
        } else {
            entityService.save(individual);
        }
    }

    private ResponseEntity<WebServiceCallException> requestError(String message) {
        WebServiceCallException error = new WebServiceCallException();
        error.getErrors().add(message);
        return new ResponseEntity<WebServiceCallException>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<WebServiceCallException> serverError(String message) {
        WebServiceCallException error = new WebServiceCallException();
        error.getErrors().add(message);
        return new ResponseEntity<WebServiceCallException>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<WebServiceCallException> requestError(ConstraintViolations cv) {
        return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv),
                HttpStatus.BAD_REQUEST);
    }
}
