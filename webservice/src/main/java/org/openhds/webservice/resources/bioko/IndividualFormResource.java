package org.openhds.webservice.resources.bioko;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/individualForm")
public class IndividualFormResource {
    private static final Logger logger = LoggerFactory.getLogger(IndividualFormResource.class);

    private final FieldWorkerService fieldWorkerService;
    private final EntityService entityService;
    private final IndividualService individualService;
    private final LocationHierarchyService locationHierarchyService;
    private final ResidencyService residencyService;
    private final SocialGroupService socialGroupService;
    private final FieldBuilder fieldBuilder;

    @Autowired
    public IndividualFormResource(FieldWorkerService fieldWorkerService,
            EntityService entityService, IndividualService individualService,
            LocationHierarchyService locationHierarchyService, ResidencyService residencyService,
            SocialGroupService socialGroupService, FieldBuilder fieldBuilder) {
        this.fieldWorkerService = fieldWorkerService;
        this.entityService = entityService;
        this.individualService = individualService;
        this.locationHierarchyService = locationHierarchyService;
        this.residencyService = residencyService;
        this.socialGroupService = socialGroupService;
        this.fieldBuilder = fieldBuilder;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    public ResponseEntity<? extends Serializable> processForm(
            @RequestBody IndividualForm individualForm) {

        ConstraintViolations cv = new ConstraintViolations();

        FieldWorker collectedBy;
        try {
            collectedBy = fieldWorkerService.findFieldWorkerById(
                    individualForm.getFieldWorkerExtId(),
                    "Individual form has nonexistent field worker id.");
        } catch (Exception e) {
            return requestError("Error getting field worker: " + e.getMessage());
        }

        // the actual individual
        Individual individual;
        Calendar collectionTime;
        try {
            individual = makeIndividual(individualForm, collectedBy, cv);
            if (cv.hasViolations()) {
                return requestError(cv);
            }
            collectionTime = Calendar.getInstance();
            collectionTime.setTime(individualForm.getCollectionDateTime());
        } catch (Exception e) {
            return requestError("Error finding or creating individual: " + e.getMessage());
        }

        // individual's household social group
        SocialGroup socialGroup;
        try {
            socialGroup = socialGroupService.findSocialGroupById(
                    individualForm.getHouseholdExtId(),
                    "Individual form has nonexistent household social group.");
        } catch (Exception e) {
            return requestError("Error getting social group: " + e.getMessage());
        }

        // individual's membership in the social group
        try {
            if (individual.getAllMemberships().size() == 0) {
                Membership membership = new Membership();
                membership.setIndividual(individual);
                membership.setSocialGroup(socialGroup);
                membership.setCollectedBy(collectedBy);
                membership.setStartDate(collectionTime);
                membership.setStartType("Census Individual Form");
                membership.setbIsToA(individualForm.getIndividualRelationshipToHeadOfHousehold());
                individual.getAllMemberships().add(membership);
            }
        } catch (Exception e) {
            return requestError("Error creating membership: " + e.getMessage());
        }

        // individual's household location
        Location location;
        try {

            location = locationHierarchyService
                    .findLocationById(individualForm.getHouseholdExtId());
            if (null == location) {
                WebServiceCallException error = new WebServiceCallException();
                error.getErrors().add("Individual form has nonexistent household location.");
                return new ResponseEntity<WebServiceCallException>(error, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return requestError("Error getting location: " + e.getMessage());
        }

        // individual's residency at location
        Residency residency;
        try {
            if (!residencyService.hasOpenResidency(individual)) {
                residency = residencyService.createResidency(individual, location, collectionTime,
                        "Census Individual Form", collectedBy);
                residency = residencyService.evaluateResidency(residency);
                individual.getAllResidencies().add(residency);
            }
        } catch (ConstraintViolations e) {
            return requestError(e);
        } catch (Exception e) {
            return requestError("Error creating residency: " + e.getMessage());
        }

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

    private void copyFormDataToIndividual(IndividualForm individualForm, Individual individual) {
        individual.setExtId(individualForm.getIndividualExtId());
        individual.setFirstName(individualForm.getIndividualFirstName());
        individual.setMiddleName(individualForm.getIndividualOtherNames());
        individual.setLastName(individualForm.getIndividualLastName());
        individual.setGender(individualForm.getIndividualGender());
        Calendar cal = Calendar.getInstance();
        cal.setTime(individualForm.getIndividualDateOfBirth());
        individual.setDob(cal);

        // TODO: add these to the individual data model
        // private int individualAge;
        // private String individualAgeUnits;
        // private String individualPhoneNumber;
        // private String individualOtherPhoneNumber;
        // private String individualLanguagePreference;
        // private String individualPointOfContactName;
        // private String individualPointOfContactPhoneNumber;
        // private int individualDip;
        // private String individualMemberStatus;
    }

    private Individual getUnknownParent(String gender) {
        Individual parent = new Individual();
        parent.setGender(gender);
        parent.setExtId("UNK");

        Calendar dob = Calendar.getInstance();
        dob.set(1900, 0, 1);
        parent.setDob(dob);

        return parent;
    }

    private Individual makeIndividual(IndividualForm individualForm, FieldWorker collectedBy,
            ConstraintViolations cv) {
        Individual individual = individualService
                .findIndivById(individualForm.getIndividualExtId());
        if (null == individual) {
            individual = new Individual();
        }

        copyFormDataToIndividual(individualForm, individual);

        collectedBy = new FieldWorker();
        collectedBy.setExtId(individualForm.getFieldWorkerExtId());
        individual.setCollectedBy(fieldBuilder.referenceField(collectedBy, cv));

        // Bioko project forms don't include parents!
        Individual mother = getUnknownParent("F");
        individual.setMother(fieldBuilder.referenceField(mother, cv, "Using Unknown Mother"));
        Individual father = getUnknownParent("M");
        individual.setFather(fieldBuilder.referenceField(father, cv, "Using Unknown Father"));

        return individual;
    }

    private void createOrSaveIndividual(Individual individual) throws ConstraintViolations,
            SQLException {
        boolean isUpdate = null != individualService.findIndivById(individual.getExtId());
        if (isUpdate) {
            entityService.save(individual);
        } else {
            individualService.createIndividual(individual);
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
