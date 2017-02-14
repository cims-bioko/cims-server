package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.service.refactor.DeathService;
import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.model.*;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.errorhandling.constants.ErrorConstants;
import com.github.cimsbioko.server.errorhandling.service.ErrorHandlingService;
import com.github.cimsbioko.server.errorhandling.util.ErrorLogUtil;
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
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) throws JAXBException {

        try {
            context = JAXBContext.newInstance(Form.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        Death death = new Death();

        death.setDeathCause(form.getCauseOfDeath());
        death.setDeathDate(form.getDateOfDeath());
        death.setDeathPlace(form.getPlaceOfDeath());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(form.getFieldWorkerUuid());
        if (null == fieldWorker) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID + " : " + form.getFieldWorkerUuid());
            String errorDataPayload = createDTOPayload(form);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, Form.LOG_NAME,
                    fieldWorker, ConstraintViolations.INVALID_FIELD_WORKER_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        death.setCollectedBy(fieldWorker);

        Visit visit = visitService.findVisitByUuid(form.getVisitUuid());
        if (null == visit) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_VISIT_UUID + " : " + form.getVisitUuid());
            String errorDataPayload = createDTOPayload(form);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, Form.LOG_NAME,
                    fieldWorker, ConstraintViolations.INVALID_VISIT_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        death.setVisitDeath(visit);

        Individual individual = individualService.getByUuid(form.getIndividualUuid());
        if (null == individual) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_UUID + " : " + form.getIndividualUuid());
            String errorDataPayload = createDTOPayload(form);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, Form.LOG_NAME,
                    fieldWorker, ConstraintViolations.INVALID_INDIVIDUAL_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        death.setIndividual(individual);

        try {
            deathService.create(death);
        } catch (ConstraintViolations cv) {
            String errorDataPayload = createDTOPayload(form);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, Form.LOG_NAME,
                    fieldWorker, ErrorConstants.CONSTRAINT_VIOLATION, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        return new ResponseEntity<>(form, HttpStatus.CREATED);

    }

    private String createDTOPayload(Form form) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();
    }


    @Description(description = "Model data from the Death xform for the Bioko island project.")
    @XmlRootElement(name = "deathForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "DeathForm";

        private static final long serialVersionUID = 1L;

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

        //death form fields
        @XmlElement(name = "individual_ext_id")
        private String individualExtId;

        @XmlElement(name = "individual_uuid")
        private String individualUuid;

        @XmlElement(name = "visit_ext_id")
        private String visitExtId;

        @XmlElement(name = "visit_uuid")
        private String visitUuid;

        @XmlElement(name = "date_of_death")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar dateOfDeath;

        @XmlElement(name = "place_of_death")
        private String placeOfDeath;

        @XmlElement(name = "cause_of_death")
        private String causeOfDeath;

        public String getIndividualUuid() {
            return individualUuid;
        }

        public void setIndividualUuid(String individualUuid) {
            this.individualUuid = individualUuid;
        }

        public String getVisitUuid() {
            return visitUuid;
        }

        public void setVisitUuid(String visitUuid) {
            this.visitUuid = visitUuid;
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

        public boolean isProcessedByMirth() {
            return processedByMirth;
        }

        public void setProcessedByMirth(boolean processedByMirth) {
            this.processedByMirth = processedByMirth;
        }

        public String getFieldWorkerExtId() {
            return fieldWorkerExtId;
        }

        public void setFieldWorkerExtId(String fieldWorkerExtId) {
            this.fieldWorkerExtId = fieldWorkerExtId;
        }

        public Calendar getCollectionDateTime() {
            return collectionDateTime;
        }

        public void setCollectionDateTime(Calendar collectionDateTime) {
            this.collectionDateTime = collectionDateTime;
        }

        public String getIndividualExtId() {
            return individualExtId;
        }

        public void setIndividualExtId(String individualExtId) {
            this.individualExtId = individualExtId;
        }

        public String getVisitExtId() {
            return visitExtId;
        }

        public void setVisitExtId(String visitExtId) {
            this.visitExtId = visitExtId;
        }

        public Calendar getDateOfDeath() {
            return dateOfDeath;
        }

        public void setDateOfDeath(Calendar dateOfDeath) {
            this.dateOfDeath = dateOfDeath;
        }

        public String getPlaceOfDeath() {
            return placeOfDeath;
        }

        public void setPlaceOfDeath(String placeOfDeath) {
            this.placeOfDeath = placeOfDeath;
        }

        public String getCauseOfDeath() {
            return causeOfDeath;
        }

        public void setCauseOfDeath(String causeOfDeath) {
            this.causeOfDeath = causeOfDeath;
        }
    }
}
