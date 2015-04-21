package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.DeathService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.model.*;
import org.openhds.domain.model.bioko.DeathForm;
import org.openhds.domain.model.bioko.OutMigrationForm;
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

@Controller
@RequestMapping("/deathForm")
public class DeathFormResource extends AbstractFormResource {

    @Autowired
    private IndividualService individualService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private DeathService deathService;

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private JAXBContext context = null;
    private Marshaller marshaller = null;

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody DeathForm deathForm) throws JAXBException {

        try {
            context = JAXBContext.newInstance(DeathForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        Death death = new Death();

        death.setDeathCause(deathForm.getCauseOfDeath());
        death.setDeathDate(deathForm.getDateOfDeath());
        death.setDeathPlace(deathForm.getPlaceOfDeath());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(deathForm.getFieldWorkerUuid());
        if (null == fieldWorker) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID +" : " + deathForm.getFieldWorkerUuid());
            String errorDataPayload = createDTOPayload(deathForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, DeathForm.class.getSimpleName(),
                    fieldWorker, ConstraintViolations.INVALID_FIELD_WORKER_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        death.setCollectedBy(fieldWorker);

        Visit visit = visitService.findVisitByUuid(deathForm.getVisitUuid());
        if (null == visit) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_VISIT_UUID +" : " + deathForm.getVisitUuid());
            String errorDataPayload = createDTOPayload(deathForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, DeathForm.class.getSimpleName(),
                    fieldWorker, ConstraintViolations.INVALID_VISIT_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        death.setVisitDeath(visit);

        Individual individual = individualService.getByUuid(deathForm.getIndividualUuid());
        if (null == individual) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_UUID +" : " + deathForm.getIndividualUuid());
            String errorDataPayload = createDTOPayload(deathForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, DeathForm.class.getSimpleName(),
                    fieldWorker, ConstraintViolations.INVALID_INDIVIDUAL_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        death.setIndividual(individual);

        try {
            deathService.create(death);
        } catch (ConstraintViolations cv) {
            String errorDataPayload = createDTOPayload(deathForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, OutMigrationForm.class.getSimpleName(),
                    fieldWorker, ErrorConstants.CONSTRAINT_VIOLATION, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        return new ResponseEntity<DeathForm>(deathForm, HttpStatus.CREATED);

    }

    private String createDTOPayload(DeathForm deathForm) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(deathForm, writer);
        return writer.toString();
    }

}
