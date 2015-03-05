package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.PregnancyObservationService;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.PregnancyObservation;
import org.openhds.domain.model.Visit;
import org.openhds.domain.model.bioko.PregnancyObservationForm;
import org.openhds.domain.util.CalendarAdapter;
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

        PregnancyObservation pregnancyObservation = new PregnancyObservation();
        pregnancyObservation.setRecordedDate(pregnancyObservationForm.getRecordedDate());
        pregnancyObservation.setExpectedDeliveryDate(pregnancyObservationForm.getExpectedDeliveryDate());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(pregnancyObservationForm.getFieldWorkerUuid());
        if (null == fieldWorker) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID);
            logError(cv, fieldWorker, createDTOPayload(pregnancyObservationForm), PregnancyObservationForm.class.getSimpleName());
            return requestError(cv);
        }
        pregnancyObservation.setCollectedBy(fieldWorker);

        Individual individual = individualService.getByUuid(pregnancyObservationForm.getIndividualUuid());
        if (null == individual) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_UUID);
            logError(cv, fieldWorker, createDTOPayload(pregnancyObservationForm), PregnancyObservationForm.class.getSimpleName());
            return requestError(cv);
        }
        pregnancyObservation.setMother(individual);

        Visit visit = visitService.findVisitByUuid(pregnancyObservationForm.getVisitUuid());
        if (null == visit) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_VISIT_UUID);
            logError(cv, fieldWorker, createDTOPayload(pregnancyObservationForm), PregnancyObservationForm.class.getSimpleName());
            return requestError(cv);
        }

        pregnancyObservation.setVisit(visit);
        try {
            pregnancyObservationService.create(pregnancyObservation);
        } catch (ConstraintViolations cv) {
            logError(cv, fieldWorker, createDTOPayload(pregnancyObservationForm), PregnancyObservationForm.class.getSimpleName());
            return requestError(cv);
        }

        return new ResponseEntity<PregnancyObservationForm>(pregnancyObservationForm, HttpStatus.CREATED);

    }

    private String createDTOPayload(PregnancyObservationForm pregnancyObservationForm) throws JAXBException {

        StringWriter writer = new StringWriter();
        marshaller.marshal(pregnancyObservationForm, writer);
        return writer.toString();

    }


}
