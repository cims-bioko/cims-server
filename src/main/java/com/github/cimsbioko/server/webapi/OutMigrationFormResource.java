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
    private Marshaller marshaller = null;


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
        @XmlElement(name = "processed_by_mirth")
        private boolean processedByMirth;

        @XmlElement(name = "entity_uuid")
        private String entityUuid;

        @XmlElement(name = "entity_ext_id")
        private String entityExtId;

        @XmlElement(name = "field_worker_ext_id")
        private String fieldWorkerExtId;

        @XmlElement(name = "field_worker_uuid")
        private String fieldWorkerUuid;

        @XmlElement(name = "collection_date_time")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar collectionDateTime;

        //OutMigr form fields
        @XmlElement(name = "individual_ext_id")
        private String individualExtId;

        @XmlElement(name = "individual_uuid")
        private String individualUuid;

        @XmlElement(name = "visit_ext_id")
        private String visitExtId;

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

        public void setVisitUuid(String visitUuid) {
            this.visitUuid = visitUuid;
        }

        public String getIndividualUuid() {
            return individualUuid;
        }

        public void setIndividualUuid(String individualUuid) {
            this.individualUuid = individualUuid;
        }

        public String getEntityUuid() {
            return entityUuid;
        }

        public void setEntityUuid(String entityUuid) {
            this.entityUuid = entityUuid;
        }

        public String getEntityExtId() {
            return entityExtId;
        }

        public void setEntityExtId(String entityExtId) {
            this.entityExtId = entityExtId;
        }

        public String getFieldWorkerUuid() {
            return fieldWorkerUuid;
        }

        public void setFieldWorkerUuid(String fieldWorkerUuid) {
            this.fieldWorkerUuid = fieldWorkerUuid;
        }

        public Calendar getCollectionDateTime() {
            return collectionDateTime;
        }

        public void setCollectionDateTime(Calendar collectionDateTime) {
            this.collectionDateTime = collectionDateTime;
        }

        public boolean isProcessedByMirth() {
            return processedByMirth;
        }

        public void setProcessedByMirth(boolean processedByMirth) {
            this.processedByMirth = processedByMirth;
        }

        public String getIndividualExtId() {
            return individualExtId;
        }

        public void setIndividualExtId(String individualExtId) {
            this.individualExtId = individualExtId;
        }

        public String getFieldWorkerExtId() {
            return fieldWorkerExtId;
        }

        public void setFieldWorkerExtId(String fieldWorkerExtId) {
            this.fieldWorkerExtId = fieldWorkerExtId;
        }

        public String getVisitExtId() {
            return visitExtId;
        }

        public void setVisitExtId(String visitExtId) {
            this.visitExtId = visitExtId;
        }

        public Calendar getDateOfMigration() {
            return dateOfMigration;
        }

        public void setDateOfMigration(Calendar dateOfMigration) {
            this.dateOfMigration = dateOfMigration;
        }

        public String getNameOfDestination() {
            return nameOfDestination;
        }

        public void setNameOfDestination(String nameOfDestination) {
            this.nameOfDestination = nameOfDestination;
        }

        public String getReasonForOutMigration() {
            return reasonForOutMigration;
        }

        public void setReasonForOutMigration(String reasonForOutMigration) {
            this.reasonForOutMigration = reasonForOutMigration;
        }
    }
}