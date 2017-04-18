package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.VisitService;
import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.controller.service.refactor.PregnancyObservationService;
import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.model.*;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.errorhandling.constants.ErrorConstants;
import com.github.cimsbioko.server.errorhandling.service.ErrorHandlingService;
import com.github.cimsbioko.server.errorhandling.util.ErrorLogUtil;
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
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Calendar;

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
    private Marshaller marshaller = null; // FIXME: *not thread safe!!!*

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form)
            throws JAXBException {

        try {
            context = JAXBContext.newInstance(Form.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyObservationFormResource");
        }

        ConstraintViolations cv = new ConstraintViolations();

        PregnancyObservation pregnancyObservation = new PregnancyObservation();
        pregnancyObservation.setRecordedDate(form.getRecordedDate());
        pregnancyObservation.setExpectedDeliveryDate(form.getExpectedDeliveryDate());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(form.getFieldWorkerUuid());
        if (null == fieldWorker) {
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID + " : " + form.getFieldWorkerUuid());
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(form), null,
                    Form.LOG_NAME, null, ConstraintViolations.INVALID_FIELD_WORKER_UUID, cv.getViolations());
            errorService.logError(errorLog);
            return requestError(cv);
        }
        pregnancyObservation.setCollectedBy(fieldWorker);

        Individual individual = individualService.getByUuid(form.getIndividualUuid());
        if (null == individual) {
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_UUID + " : " + form.getIndividualUuid());
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(form), null,
                    Form.LOG_NAME, null, ConstraintViolations.INVALID_INDIVIDUAL_UUID, cv.getViolations());
            errorService.logError(errorLog);
            return requestError(cv);
        }
        pregnancyObservation.setMother(individual);

        Visit visit = visitService.findVisitByUuid(form.getVisitUuid());
        if (null == visit) {
            cv.addViolations(ConstraintViolations.INVALID_VISIT_UUID + " : " + form.getVisitUuid());
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(form), null,
                    Form.LOG_NAME, null, ConstraintViolations.INVALID_VISIT_UUID, cv.getViolations());
            errorService.logError(errorLog);
            return requestError(cv);
        }

        pregnancyObservation.setVisit(visit);
        try {
            pregnancyObservationService.create(pregnancyObservation);
        } catch (ConstraintViolations e) {
            ErrorLog errorLog = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, createDTOPayload(form), null,
                    Form.LOG_NAME, null, ErrorConstants.CONSTRAINT_VIOLATION, e.getViolations());
            errorService.logError(errorLog);
            return requestError(e);
        }

        return new ResponseEntity<>(form, HttpStatus.CREATED);

    }

    private String createDTOPayload(Form form) throws JAXBException {

        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();

    }


    @Description(description = "Model data from the PregnancyObservation xform for the Bioko island project.")
    @XmlRootElement(name = "pregnancyObservationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "PregnancyObservationForm";

        private static final long serialVersionUID = 1L;

        //core form fields
        @XmlElement(name = "field_worker_uuid")
        private String fieldWorkerUuid;

        //PregObs form fields
        @XmlElement(name = "individual_uuid")
        private String individualUuid;

        @XmlElement(name = "visit_uuid")
        private String visitUuid;

        @XmlElement(name = "expected_delivery_date")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar expectedDeliveryDate;

        @XmlElement(name = "recorded_date")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar recordedDate;

        public String getIndividualUuid() {
            return individualUuid;
        }

        public String getVisitUuid() {
            return visitUuid;
        }

        public String getFieldWorkerUuid() {
            return fieldWorkerUuid;
        }

        public static long getSerialVersionUID() {
            return serialVersionUID;
        }

        public Calendar getExpectedDeliveryDate() {
            return expectedDeliveryDate;
        }

        public Calendar getRecordedDate() {
            return recordedDate;
        }
    }

}
