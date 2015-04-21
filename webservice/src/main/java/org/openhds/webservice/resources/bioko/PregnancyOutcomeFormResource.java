package org.openhds.webservice.resources.bioko;

import org.hibernate.id.UUIDGenerator;
import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.PregnancyService;
import org.openhds.controller.service.ResidencyService;
import org.openhds.controller.service.VisitService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.SocialGroupService;
import org.openhds.domain.model.*;
import org.openhds.domain.model.bioko.PregnancyOutcomeCoreForm;
import org.openhds.domain.model.bioko.PregnancyOutcomeOutcomesForm;
import org.openhds.domain.service.SitePropertiesService;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.errorhandling.util.ErrorLogUtil;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/pregnancyOutcomeForm")
public class PregnancyOutcomeFormResource extends AbstractFormResource {

    private static final String START_TYPE = "PregnancyOutcomeForm";

    @Autowired
    private PregnancyService pregnancyService;

    @Autowired
    private SocialGroupService socialGroupService;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    @Autowired
    private SitePropertiesService siteProperties;


    @RequestMapping(value = "/core", method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processCoreForm(@RequestBody PregnancyOutcomeCoreForm coreForm) throws JAXBException {

        Marshaller marshaller;
        try {
            JAXBContext context = JAXBContext.newInstance(PregnancyOutcomeCoreForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyOutcomeFormResource");
        }

        List<String> logMessage = new ArrayList<String>();

        PregnancyOutcome pregnancyOutcome = new PregnancyOutcome();
        try {
            fillInCoreFields(coreForm, pregnancyOutcome);
            pregnancyService.createPregnancyOutcome(pregnancyOutcome);
        } catch (ConstraintViolations constraintViolations) {
            logMessage.add(constraintViolations.getMessage());
            String errorDataPayload = createDTOPayload(coreForm, marshaller);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    PregnancyOutcomeCoreForm.class.getSimpleName(), pregnancyOutcome.getCollectedBy(),
                    ConstraintViolations.INVALID_PREGNANCY_OUTCOME_CORE, logMessage);
            errorService.logError(error);
            return requestError(constraintViolations.getMessage());
        }

        return new ResponseEntity<PregnancyOutcomeCoreForm>(coreForm, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/outcomes", method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processOutcomesForm(@RequestBody PregnancyOutcomeOutcomesForm outcomesForm) throws JAXBException {

        Marshaller marshaller;
        try {
            JAXBContext context = JAXBContext.newInstance(PregnancyOutcomeOutcomesForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyOutcomeFormResource");
        }

        List<String> logMessage = new ArrayList<String>();

        try {
            PregnancyOutcome updatedOutcome = fillInOutcomesFields(outcomesForm);
            pregnancyService.createPregnancyOutcome(updatedOutcome);
        } catch (ConstraintViolations cv) {
            logMessage.add(cv.getMessage());
            String errorDataPayload = createDTOPayload(outcomesForm, marshaller);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    PregnancyOutcomeOutcomesForm.class.getSimpleName(), null,
                    ConstraintViolations.INVALID_PREGNANCY_OUTCOME_CHILD, logMessage);
            errorService.logError(error);
            return requestError(cv.getMessage());
        }



        return new ResponseEntity<PregnancyOutcomeOutcomesForm>(outcomesForm, HttpStatus.CREATED);
    }

    private PregnancyOutcome fillInOutcomesFields(PregnancyOutcomeOutcomesForm outcomesForm) throws ConstraintViolations {

        PregnancyOutcome parentOutcome = pregnancyService.getPregnancyOutcomeByUuid(outcomesForm.getPregnancyOutcomeUuid());
        if (null == parentOutcome) {
            throw new ConstraintViolations("Could not find parent PregnancyOutcome with UUID:" + outcomesForm.getPregnancyOutcomeUuid());
        }

        List<Outcome> existingOutcomes = parentOutcome.getOutcomes();
        if (null == existingOutcomes) {
            existingOutcomes = new ArrayList<Outcome>();
        }

        Outcome outcome = new Outcome();
        outcome.setType(outcomesForm.getOutcomeType());

        if (outcome.getType().equalsIgnoreCase(siteProperties.getLiveBirthCode())) {
            Individual child = new Individual();
            child.setUuid(outcomesForm.getChildUuid());
            child.setExtId("Test");
            child.setCollectedBy(parentOutcome.getCollectedBy());
            child.setFirstName(outcomesForm.getChildFirstName());
            child.setMiddleName(outcomesForm.getChildMiddleName());
            child.setLastName(outcomesForm.getChildLastName());
            child.setGender(outcomesForm.getChildGender());
            child.setNationality(outcomesForm.getChildNationality());

            SocialGroup socialGroup = socialGroupService.getByUuid(outcomesForm.getSocialGroupUuid());
            if (null == socialGroup) {
                throw new ConstraintViolations("Could not find Social Group with UUID: " + outcomesForm.getSocialGroupUuid());
            }

            child.setMother(parentOutcome.getMother());
            child.setFather(parentOutcome.getFather());

            //Instantiate Relationship
            establishRelationship(child, outcomesForm, socialGroup);
            //Instantiate Membership: Delegate to the service entirely?
            establishMembership(child, outcomesForm, socialGroup);

            outcome.setChild(child);
        }

        existingOutcomes.add(outcome);
        parentOutcome.setOutcomes(existingOutcomes);

        return parentOutcome;

    }

    private void establishMembership(Individual child, PregnancyOutcomeOutcomesForm form, SocialGroup socialGroup) {
        Membership mem = new Membership();
        mem.setUuid(UUID.randomUUID().toString().replace("-",""));
        mem.setIndividual(child);
        mem.setInsertDate(form.getCollectionDateTime());
        mem.setbIsToA(form.getChildRelationshipToGroupHead());
        mem.setStartDate(form.getCollectionDateTime());
        mem.setCollectedBy(child.getCollectedBy());
        mem.setSocialGroup(socialGroup);
        mem.setStartType(START_TYPE);
        mem.setEndType(NOT_APPLICABLE_END_TYPE);
        child.getAllMemberships().add(mem);
    }

    private void establishRelationship(Individual child, PregnancyOutcomeOutcomesForm form, SocialGroup socialGroup) {
        Relationship rel = new Relationship();
        rel.setUuid(UUID.randomUUID().toString().replace("-", ""));
        rel.setInsertDate(form.getCollectionDateTime());
        rel.setIndividualA(child);
        rel.setIndividualB(socialGroup.getGroupHead());
        rel.setaIsToB(form.getChildRelationshipToGroupHead());
        rel.setCollectedBy(child.getCollectedBy());
        rel.setInsertDate(form.getCollectionDateTime());
        rel.setStartDate(form.getCollectionDateTime());
        child.getAllRelationships1().add(rel);
    }

    private void fillInCoreFields(PregnancyOutcomeCoreForm pregnancyOutcomeCoreForm, PregnancyOutcome pregnancyOutcome) throws ConstraintViolations {

        pregnancyOutcome.setUuid(pregnancyOutcomeCoreForm.getPregnancyOutcomeUuid());

        pregnancyOutcome.setOutcomeDate(pregnancyOutcomeCoreForm.getDeliveryDate());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(pregnancyOutcomeCoreForm.getFieldWorkerUuid());
        if(null == fieldWorker) {
            throw new ConstraintViolations("Could not find fieldworker with UUID: " + pregnancyOutcomeCoreForm.getFieldWorkerUuid());
        }
        pregnancyOutcome.setCollectedBy(fieldWorker);

        Visit visit = visitService.findVisitByUuid(pregnancyOutcomeCoreForm.getVisitUuid());
        if(null == visit) {
            throw new ConstraintViolations("Could not find visit with UUID: " + pregnancyOutcomeCoreForm.getVisitUuid());
        }
        pregnancyOutcome.setVisit(visit);

        Individual daddy = individualService.getByUuid(pregnancyOutcomeCoreForm.getFatherUuid());
        if(null == daddy) {
            daddy = makeUnknownParent(MALE);
        }
        pregnancyOutcome.setFather(daddy);


        Individual mommy = individualService.getByUuid(pregnancyOutcomeCoreForm.getMotherUuid());
        if(null == mommy) {
            throw new ConstraintViolations("Could not find mommy with UUID: " + pregnancyOutcomeCoreForm.getMotherUuid());
        }
        pregnancyOutcome.setMother(mommy);
    }

    private String createDTOPayload(PregnancyOutcomeOutcomesForm form, Marshaller marshaller) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();
    }

    private String createDTOPayload(PregnancyOutcomeCoreForm pregnancyOutcomeCoreForm, Marshaller marshaller) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(pregnancyOutcomeCoreForm, writer);
        return writer.toString();
    }
}
