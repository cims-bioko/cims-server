package com.github.cimsbioko.server.formproc;

import com.github.cimsbioko.server.domain.FieldWorker;
import com.github.cimsbioko.server.domain.Individual;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.service.refactor.IndividualService;
import com.github.cimsbioko.server.service.refactor.LocationService;
import com.github.cimsbioko.server.util.CalendarAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

@Component
public class IndividualFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(IndividualFormProcessor.class);

    // FIXME: value codes can be configured by projects
    private static final String HEAD_OF_HOUSEHOLD_SELF = "1";

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private LocationService locationService;

    // This individual form should cause several CRUDS:
    // location, individual
    @Transactional
    public void processForm(Form form) throws IOException {

        // Default relationship to head of household is "self"
        if (form.individualRelationshipToHeadOfHousehold == null) {
            form.individualRelationshipToHeadOfHousehold = HEAD_OF_HOUSEHOLD_SELF;
        }

        // inserted when?
        Calendar insertTime = Calendar.getInstance();

        // collected by whom?
        ConstraintViolations cv = new ConstraintViolations();
        FieldWorker collectedBy = fieldWorkerService.getByUuid(form.fieldWorkerUuid);
        if (collectedBy == null) {
            cv.addViolations("Field Worker does not exist");
            logError(cv, marshalForm(form), Form.LOG_NAME);
            return;
        }

        // where are we?
        Location location;
        try {
            // Get location by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            if (form.householdUuid != null) {
                location = locationService.getByUuid(form.householdUuid);
            } else {
                location = locationService.getByExtId(form.householdExtId);
            }

            if (location == null) {
                String errorMessage = "Location does not exist " + form.householdUuid + " / " + form.householdExtId;
                cv.addViolations(errorMessage);
                logError(cv, marshalForm(form), Form.LOG_NAME);
                return;
            }

        } catch (Exception e) {
            return;
        }

        // make a new individual, to be persisted below
        Individual individual;
        try {
            individual = findOrMakeIndividual(form, collectedBy, insertTime, cv);
            if (cv.hasViolations()) {
                logError(cv, marshalForm(form), Form.LOG_NAME);
                return;
            }
        } catch (Exception e) {
            return;
        }

        location.addResident(individual);

        // change the individual's extId if the server has previously changed the extId of their location/household
        if (!form.householdExtId.equalsIgnoreCase(location.getExtId())) {

            updateIndividualExtId(individual, location);

            // log the modification
            cv.addViolations("Individual ExtId updated from " + form.individualExtId + " to " + individual.getExtId());
            logError(cv, marshalForm(form), Form.LOG_NAME);
        }

        // log a warning if the individual extId clashes with an existing individual's extId
        if (individualService.getExistingExtIdCount(individual.getExtId()) != 0) {
            cv.addViolations("Warning: Individual ExtId clashes with an existing Individual's extId : " + individual.getExtId());
            logError(cv, marshalForm(form), Form.LOG_NAME);
        }

        // persist the individual, used to be for cascading to residency
        // TODO: remove this type of code, since it's probably unnecessary after removing residency
        try {
            createOrSaveIndividual(individual);
        } catch (ConstraintViolations e) {
            logError(e, marshalForm(form), Form.LOG_NAME);
            return;
        } catch (Exception e) {
            return;
        }

        if (form.individualRelationshipToHeadOfHousehold.equals(HEAD_OF_HOUSEHOLD_SELF)) {
            location.setName(individual.getLastName());
        }

        return;
    }

    private void updateIndividualExtId(Individual individual, Location location) {
        String individualSuffixSequence = individual.getExtId().substring(individual.getExtId().length() - 4);
        individual.setExtId(location.getExtId() + individualSuffixSequence);
    }

    private Individual findOrMakeIndividual(Form form, FieldWorker collectedBy,
                                            Calendar insertTime, ConstraintViolations cv) {
        Individual individual = individualService.getByUuid(form.uuid);
        if (individual == null) {
            individual = new Individual();
        }

        individual.setCollector(collectedBy);
        individual.setCreated(insertTime);

        copyFormDataToIndividual(form, individual);

        return individual;
    }

    private void copyFormDataToIndividual(Form form, Individual individual) {
        if (individual.getUuid() == null) {
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
        individual.setPhone1(form.individualPhoneNumber);
        individual.setPhone2(form.individualOtherPhoneNumber);
        individual.setLanguage(form.individualLanguagePreference);
        individual.setContactName(form.individualPointOfContactName);
        individual.setContactPhone(form.individualPointOfContactPhoneNumber);
        individual.setDip(form.individualDip);
        individual.setNationality(form.individualNationality);
        individual.setHomeRole(form.individualRelationshipToHeadOfHousehold);
    }

    private void createOrSaveIndividual(Individual individual) throws ConstraintViolations {
        if (individualService.getByUuid(individual.getUuid()) == null) {
            individualService.create(individual);
        } else {
            individualService.save(individual);
        }
    }

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
