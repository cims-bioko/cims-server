package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.PregnancyObservationService;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.model.*;
import org.openhds.domain.model.bioko.PregnancyObservationForm;
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
@RequestMapping("/pregnancyObservationForm")
public class PregnancyObservationFormResource extends AbstractFormResource {

    @Autowired
    private IndividualService individualService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private PregnancyObservationService pregnancyObservationService;

    @Autowired
    private CalendarAdapter adapter;

    @Autowired
    private ErrorHandlingService errorService;

    private JAXBContext context = null;
    private Marshaller marshaller = null;

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody PregnancyObservationForm pregnancyObservationForm)
                                                                            throws JAXBException {

        try {
            context = JAXBContext.newInstance(PregnancyObservationForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyObservationFormResource");
        }

        ConstraintViolations cv = new ConstraintViolations();

        PregnancyObservation pregnancyObservation = new PregnancyObservation();
        pregnancyObservation.setRecordedDate(pregnancyObservationForm.getRecordedDate());
        pregnancyObservation.setExpectedDeliveryDate(pregnancyObservationForm.getExpectedDeliveryDate());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(pregnancyObservationForm.getFieldWorkerUuid());
        if (null == fieldWorker) {
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID + " : "+pregnancyObservationForm.getFieldWorkerUuid());
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(pregnancyObservationForm), null,
                    PregnancyObservationForm.class.getSimpleName(), null, ConstraintViolations.INVALID_FIELD_WORKER_UUID, cv.getViolations());
            errorService.logError(errorLog);
            return requestError(cv);
        }
        pregnancyObservation.setCollectedBy(fieldWorker);

        Individual individual = individualService.getByUuid(pregnancyObservationForm.getIndividualUuid());
        if (null == individual) {
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_UUID + " : "+pregnancyObservationForm.getIndividualUuid());
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(pregnancyObservationForm), null,
                    PregnancyObservationForm.class.getSimpleName(), null, ConstraintViolations.INVALID_INDIVIDUAL_UUID, cv.getViolations());
            errorService.logError(errorLog);
            return requestError(cv);
        }
        pregnancyObservation.setMother(individual);

        Visit visit = visitService.findVisitByUuid(pregnancyObservationForm.getVisitUuid());
        if (null == visit) {
            cv.addViolations(ConstraintViolations.INVALID_VISIT_UUID + " : "+pregnancyObservationForm.getVisitUuid());
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(pregnancyObservationForm), null,
                    PregnancyObservationForm.class.getSimpleName(), null, ConstraintViolations.INVALID_VISIT_UUID, cv.getViolations());
            errorService.logError(errorLog);
            return requestError(cv);
        }

        pregnancyObservation.setVisit(visit);
        try {
            pregnancyObservationService.create(pregnancyObservation);
        } catch (ConstraintViolations e) {
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(pregnancyObservationForm), null,
                    PregnancyObservationForm.class.getSimpleName(), null, ErrorConstants.CONSTRAINT_VIOLATION, e.getViolations());
            errorService.logError(errorLog);
            return requestError(e);
        }

        return new ResponseEntity<>(pregnancyObservationForm, HttpStatus.CREATED);

    }

    private String createDTOPayload(PregnancyObservationForm pregnancyObservationForm) throws JAXBException {

        StringWriter writer = new StringWriter();
        marshaller.marshal(pregnancyObservationForm, writer);
        return writer.toString();

    }


}
