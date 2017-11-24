package com.github.cimsbioko.server.webapi.rest;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.controller.service.refactor.LocationService;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.ResidencyService;
import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.controller.service.refactor.SocialGroupService;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.Location;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.Residency;
import com.github.cimsbioko.server.domain.model.SocialGroup;
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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import static com.github.cimsbioko.server.webapi.rest.IndividualFormResource.INDIVIDUAL_FORM_PATH;

@Controller
@RequestMapping(INDIVIDUAL_FORM_PATH)
public class IndividualFormResource extends AbstractFormResource {

    public static final String INDIVIDUAL_FORM_PATH = "/rest/individualForm";

    private static final Logger log = LoggerFactory.getLogger(IndividualFormResource.class);

    // TODO: value codes can be configured by projects
    private static final String HEAD_OF_HOUSEHOLD_SELF = "1";
    private static final String HOUSEHOLD_GROUP_TYPE = "COH";

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

    // This individual form should cause several CRUDS:
    // location, individual, socialGroup, residency, membership, relationship
    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) throws IOException {

        // Default relationship to head of household is "self"
        if (form.individualRelationshipToHeadOfHousehold == null) {
            form.individualRelationshipToHeadOfHousehold = HEAD_OF_HOUSEHOLD_SELF;
        }

        // collected when?
        Calendar collectionTime = form.collectionDateTime;
        if (null == collectionTime) {
            collectionTime = getDateInPast();
        }

        // inserted when?
        Calendar insertTime = Calendar.getInstance();

        // collected by whom?
        ConstraintViolations cv = new ConstraintViolations();
        FieldWorker collectedBy = fieldWorkerService.getByUuid(form.fieldWorkerUuid);
        if (null == collectedBy) {
            cv.addViolations("Field Worker does not exist");
            logError(cv, marshalForm(form), Form.LOG_NAME);
            return requestError(cv);
        }

        // where are we?
        Location location;
        try {
            // Get location by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            String uuid = form.householdUuid;
            if (null == uuid) {
                location = locationService.getByExtId(form.householdExtId);
            } else {
                location = locationService.getByUuid(uuid);
            }

            if (null == location) {
                String errorMessage = "Location does not exist " + form.householdUuid + " / " + form.householdExtId;
                cv.addViolations(errorMessage);
                logError(cv, marshalForm(form), Form.LOG_NAME);
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
                logError(cv, marshalForm(form), Form.LOG_NAME);
                return requestError(cv);
            }
        } catch (Exception e) {
            return requestError("Error finding or creating individual: " + e.getMessage());
        }

        // change the individual's extId if the server has previously changed the extId of their location/household
        if (!form.householdExtId.equalsIgnoreCase(location.getExtId())) {

            updateIndividualExtId(individual, location);

            // log the modification
            cv.addViolations("Individual ExtId updated from " + form.individualExtId + " to " + individual.getExtId());
            logError(cv, marshalForm(form), Form.LOG_NAME);

            //household extId used later by social group, need to correct it here
            form.householdExtId = location.getExtId();
        }

        // log a warning if the individual extId clashes with an existing individual's extId
        if (0 != individualService.getExistingExtIdCount(individual.getExtId())) {
            // log the modification
            cv.addViolations("Warning: Individual ExtId clashes with an existing Individual's extId : " + individual.getExtId());
            logError(cv, marshalForm(form), Form.LOG_NAME);
        }

        // individual's residency at location
        findOrMakeResidency(individual, location, collectionTime, insertTime, collectedBy);

        // persist the individual, cascade to residency
        try {
            createOrSaveIndividual(individual);
        } catch (ConstraintViolations e) {
            logError(e, marshalForm(form), Form.LOG_NAME);
            return requestError(e);
        } catch (SQLException e) {
            return serverError("SQL Error updating or saving individual: " + e.getMessage());
        } catch (Exception e) {
            return serverError("General Error updating or saving individual: " + e.getMessage());
        }

        SocialGroup socialGroup;
        if (form.individualRelationshipToHeadOfHousehold.equals(HEAD_OF_HOUSEHOLD_SELF)) {

            // may create social group for head of household
            socialGroup = findOrMakeSocialGroup(form, location, individual, insertTime, collectedBy);

            // name the location after the head of household
            location.setLocationName(individual.getLastName());

        } else {
            // household must already exist for household member

            // Get social group by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            if (form.socialgroupUuid == null) {
                socialGroup = socialGroupService.getByExtId(form.householdExtId);
            } else {
                socialGroup = socialGroupService.getByUuid(form.socialgroupUuid);
            }
        }

        // persist the socialGroup, cascade to through individual to relationship
        try {
            createOrSaveSocialGroup(socialGroup);
        } catch (ConstraintViolations e) {
            logError(e, marshalForm(form), Form.LOG_NAME);
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
            logError(constraintViolations, marshalForm(form), Form.LOG_NAME);
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
        individual.setExtId(location.getExtId() + individualSuffixSequence);

    }

    private Individual findOrMakeIndividual(Form form, FieldWorker collectedBy,
                                            Calendar insertTime, ConstraintViolations cv) throws Exception {

        Individual individual = individualService.getByUuid(form.uuid);
        if (null == individual) {
            individual = new Individual();
        }

        individual.setCollectedBy(collectedBy);
        individual.setInsertDate(insertTime);

        copyFormDataToIndividual(form, individual);

        return individual;
    }

    private void copyFormDataToIndividual(Form form, Individual individual)
            throws Exception {
        if (null == individual.getUuid()) {
            individual.setUuid(form.uuid);
        }
        individual.setExtId(form.individualExtId);
        individual.setFirstName(form.individualFirstName);
        individual.setMiddleName(form.individualOtherNames);
        individual.setLastName(form.individualLastName);
        individual.setGender(form.individualGender);

        Calendar dob = form.individualDateOfBirth;
        log.debug("date of birth {}", dob);
        if (null == dob) {
            dob = getDateInPast();
        }
        individual.setDob(dob);
        individual.setPhoneNumber(form.individualPhoneNumber);
        individual.setOtherPhoneNumber(form.individualOtherPhoneNumber);
        individual.setLanguagePreference(form.individualLanguagePreference);
        individual.setPointOfContactName(form.individualPointOfContactName);
        individual.setPointOfContactPhoneNumber(form.individualPointOfContactPhoneNumber);
        individual.setDip(form.individualDip);
        individual.setNationality(form.individualNationality);
    }


    private SocialGroup findOrMakeSocialGroup(Form form, Location location, Individual head,
                                              Calendar insertTime, FieldWorker collectedBy) {

        // Get social group by uuid.
        // Fall back on extId if uuid is missing, which allows us to re-process older forms.
        SocialGroup socialGroup;
        String uuid = form.socialgroupUuid;
        if (null == uuid) {
            socialGroup = socialGroupService.getByExtId(form.householdExtId);
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

    private Residency findOrMakeResidency(Individual individual, Location location, Calendar collectionTime,
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

        // attach to individial
        individual.getAllResidencies().add(residency);

        return residency;
    }

    private Membership findOrMakeMembership(Individual individual, SocialGroup socialGroup, FieldWorker collectedBy,
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
            membership.setUuid(form.membershipUuid);
        }

        // fill in or update
        membership.setIndividual(individual);
        membership.setSocialGroup(socialGroup);
        membership.setCollectedBy(collectedBy);
        membership.setInsertDate(insertTime);
        membership.setbIsToA(form.individualRelationshipToHeadOfHousehold);


        // attach to individual
        individual.getAllMemberships().add(membership);

        return membership;
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

    @Description(description = "Model data from the Individual xform for the Bioko island project. Contains Individual, social data.")
    @XmlRootElement(name = "individualForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "IndividualForm";

        private static final long serialVersionUID = 1143017330340385847L;

        //core form fields
        @XmlElement(name = "entity_uuid")
        private String uuid;

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

        @XmlElement(name = "individual_nationality")
        private String individualNationality;
    }
}
