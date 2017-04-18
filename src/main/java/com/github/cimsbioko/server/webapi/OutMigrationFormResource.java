package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.controller.service.refactor.OutMigrationService;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.OutMigration;
import com.github.cimsbioko.server.domain.model.Visit;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.VisitService;
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
@RequestMapping("/outMigrationForm")
public class OutMigrationFormResource extends AbstractFormResource {

    @Autowired
    private OutMigrationService outMigrationService;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private CalendarAdapter adapter;

    private JAXBContext context = null;
    private Marshaller marshaller = null; // FIXME: *not thread safe!!!*


    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) throws JAXBException {

        try {
            context = JAXBContext.newInstance(Form.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        OutMigration outMigration = new OutMigration();

        outMigration.setRecordedDate(form.getDateOfMigration());
        outMigration.setDestination(form.getNameOfDestination());
        outMigration.setReason(form.getReasonForOutMigration());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(form.getFieldWorkerUuid());
        if (null == fieldWorker) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID + " : " + form.getFieldWorkerUuid());
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_FIELD_WORKER_UUID);
            return requestError(cv);
        }
        outMigration.setCollectedBy(fieldWorker);

        Individual individual = individualService.getByUuid(form.getIndividualUuid());
        if (null == individual) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_UUID + " : " + form.getIndividualUuid());
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_INDIVIDUAL_UUID);
            return requestError(cv);
        }
        outMigration.setIndividual(individual);

        Visit visit = visitService.findVisitByUuid(form.getVisitUuid());
        if (null == visit) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_VISIT_UUID + " : " + form.getVisitUuid());
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_VISIT_UUID);
            return requestError(cv);
        }
        outMigration.setVisit(visit);

        try {
            outMigrationService.create(outMigration);
        } catch (ConstraintViolations cv) {
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_OUT_MIGRATION);
            return requestError(cv);
        }

        return new ResponseEntity<>(form, HttpStatus.CREATED);

    }

    private String createDTOPayload(Form form) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();
    }


    @XmlRootElement(name = "outMigrationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "OutMigrationForm";

        private static final long serialVersionUID = 4321517330340385847L;

        //core form fields
        @XmlElement(name = "field_worker_uuid")
        private String fieldWorkerUuid;

        @XmlElement(name = "individual_uuid")
        private String individualUuid;

        @XmlElement(name = "visit_uuid")
        private String visitUuid;

        @XmlElement(name = "out_migration_date")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar dateOfMigration;

        @XmlElement(name = "out_migration_name_of_destination")
        private String nameOfDestination;

        @XmlElement(name = "out_migration_reason")
        private String reasonForOutMigration;

        public String getVisitUuid() {
            return visitUuid;
        }

        public String getIndividualUuid() {
            return individualUuid;
        }

        public String getFieldWorkerUuid() {
            return fieldWorkerUuid;
        }

        public Calendar getDateOfMigration() {
            return dateOfMigration;
        }

        public String getNameOfDestination() {
            return nameOfDestination;
        }

        public String getReasonForOutMigration() {
            return reasonForOutMigration;
        }

    }
}