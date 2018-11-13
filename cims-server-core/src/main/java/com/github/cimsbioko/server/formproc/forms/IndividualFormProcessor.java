package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.dao.FieldWorkerRepository;
import com.github.cimsbioko.server.dao.IndividualRepository;
import com.github.cimsbioko.server.dao.LocationRepository;
import com.github.cimsbioko.server.domain.FieldWorker;
import com.github.cimsbioko.server.domain.Individual;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.util.CalendarAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;

public class IndividualFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(IndividualFormProcessor.class);

    // FIXME: value codes can be configured by projects
    private static final String HEAD_OF_HOUSEHOLD_SELF = "1";

    private FieldWorkerRepository fwRepo;
    private LocationRepository locRepo;
    private IndividualRepository indivRepo;

    public IndividualFormProcessor(FieldWorkerRepository fwRepo, LocationRepository locRepo, IndividualRepository indivRepo) {
        this.fwRepo = fwRepo;
        this.locRepo = locRepo;
        this.indivRepo = indivRepo;
    }

    @Transactional
    public void processForm(Form form) throws ConstraintViolations {

        // Default relationship to head of household is "self"
        if (form.individualRelationshipToHeadOfHousehold == null) {
            form.individualRelationshipToHeadOfHousehold = HEAD_OF_HOUSEHOLD_SELF;
        }

        // inserted when?
        Calendar insertTime = Calendar.getInstance();

        // collected by whom?
        // FIXME: use optional rather than null
        FieldWorker collectedBy = fwRepo.findById(form.fieldWorkerUuid).orElse(null);
        if (collectedBy == null) {
            throw new ConstraintViolations("Field Worker does not exist: " + form.fieldWorkerUuid);
        }

        // where are we?
        Location location;
        try {
            // Get location by uuid.
            // Fall back on extId if uuid is missing, which allows us to re-process older forms.
            if (form.householdUuid != null) {
                // FIXME: use optional rather than null
                location = locRepo.findById(form.householdUuid).orElse(null);
            } else {
                location = locRepo.findByExtIdAndDeletedIsNull(form.householdExtId)
                        .stream().findFirst().orElse(null);
            }

            if (location == null) {
                throw new ConstraintViolations("Location does not exist: " + form.householdUuid + " / " + form.householdExtId);
            }

        } catch (Exception e) {
            return;
        }

        // make a new individual, to be persisted below
        Individual individual = findOrMakeIndividual(form, collectedBy, insertTime);
        location.addResident(individual);

        // change the individual's extId if the server has previously changed the extId of their location/household
        if (!form.householdExtId.equalsIgnoreCase(location.getExtId())) {
            updateIndividualExtId(individual, location);
            throw new ConstraintViolations("Individual ExtId updated from " + form.individualExtId + " to " + individual.getExtId());
        }

        // log a warning if the individual extId clashes with an existing individual's extId
        if (!indivRepo.findByExtIdAndDeletedIsNull(individual.getExtId()).isEmpty()) {
            throw new ConstraintViolations("Warning: Individual ExtId clashes with an existing Individual's extId : " + individual.getExtId());
        }

        // persist the individual, since it may have been created if not present
        individual = indivRepo.save(individual);

        if (form.individualRelationshipToHeadOfHousehold.equals(HEAD_OF_HOUSEHOLD_SELF)) {
            location.setName(individual.getLastName());
        }
    }

    private void updateIndividualExtId(Individual individual, Location location) {
        String individualSuffixSequence = individual.getExtId().substring(individual.getExtId().length() - 4);
        individual.setExtId(location.getExtId() + individualSuffixSequence);
    }

    private Individual findOrMakeIndividual(Form form, FieldWorker collectedBy, Calendar insertTime) {
        Individual individual = indivRepo.findById(form.entityUuid).orElse(new Individual());
        individual.setCollector(collectedBy);
        individual.setCreated(insertTime);
        copyFormDataToIndividual(form, individual);
        return individual;
    }

    private void copyFormDataToIndividual(Form form, Individual individual) {
        if (individual.getUuid() == null) {
            individual.setUuid(form.entityUuid);
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
        individual.setStatus(form.individualMemberStatus);
    }


    @XmlRootElement(name = "individualForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "IndividualForm";

        private static final long serialVersionUID = 1143017330340385847L;

        private String entityUuid;

        private String fieldWorkerUuid;

        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar collectionDateTime;

        private String householdExtId;

        private String householdUuid;

        private String individualExtId;

        private String individualFirstName;

        private String individualLastName;

        private String individualOtherNames;

        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar individualDateOfBirth;

        private String individualGender;

        private String individualRelationshipToHeadOfHousehold;

        private String individualPhoneNumber;

        private String individualOtherPhoneNumber;

        private String individualLanguagePreference;

        private String individualPointOfContactName;

        private String individualPointOfContactPhoneNumber;

        private int individualDip;

        private String individualNationality;

        private String individualMemberStatus;
    }
}
