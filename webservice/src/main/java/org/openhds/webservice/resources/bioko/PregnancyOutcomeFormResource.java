package org.openhds.webservice.resources.bioko;

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
import java.util.List;

/**
 * Created by motech on 4/10/15.
 */

@Controller
@RequestMapping("/pregnancyOutcome")
public class PregnancyOutcomeFormResource extends AbstractFormResource {

    @Autowired
    private PregnancyService pregnancyService;

    @Autowired
    private ResidencyService residencyService;

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


        return null;
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
            throw new ConstraintViolations("Could not find daddy with UUID: " + pregnancyOutcomeCoreForm.getFatherUuid());
        }
        pregnancyOutcome.setFather(daddy);


        Individual mommy = individualService.getByUuid(pregnancyOutcomeCoreForm.getMotherUuid());
        if(null == mommy) {
            throw new ConstraintViolations("Could not find mommy with UUID: " + pregnancyOutcomeCoreForm.getMotherUuid());
        }
        pregnancyOutcome.setMother(mommy);
    }

    private String createDTOPayload(PregnancyOutcomeCoreForm pregnancyOutcomeCoreForm, Marshaller marshaller) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(pregnancyOutcomeCoreForm, writer);
        return writer.toString();
    }
}
