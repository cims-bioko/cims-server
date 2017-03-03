package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.service.VisitService;
import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.controller.service.refactor.LocationService;
import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.Location;
import com.github.cimsbioko.server.domain.model.Visit;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.errorhandling.constants.ErrorConstants;
import com.github.cimsbioko.server.errorhandling.service.ErrorHandlingService;
import com.github.cimsbioko.server.errorhandling.util.ErrorLogUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.ErrorLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/visitForm")
public class VisitFormResource extends AbstractFormResource {

    private static final Logger logger = LoggerFactory.getLogger(VisitFormResource.class);

    @Autowired
    private LocationService locationService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private JAXBContext context = null;
    private Marshaller marshaller = null; // FIXME: *not thread safe!!!*

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody Form form) throws JAXBException {

        try {
            this.context = JAXBContext.newInstance(Form.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for visit form resource");
        }

        Visit visit = new Visit();
        ConstraintViolations cv = new ConstraintViolations();

        // Bioko project does not support Rounds, all visits will be considered
        // as Round 1
        visit.setRoundNumber(1);
        visit.setVisitDate(form.getVisitDate());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(form.getFieldWorkerUuid());
        if (null == fieldWorker) {
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID);
            StringWriter writer = new StringWriter();
            marshaller.marshal(form, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Form.LOG_NAME,
                    null, ConstraintViolations.INVALID_FIELD_WORKER_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        visit.setCollectedBy(fieldWorker);


        Location location = locationService.getByUuid(form.getLocationUuid());
        if (null == location) {
            cv.addViolations(ConstraintViolations.INVALID_LOCATION_UUID);
            StringWriter writer = new StringWriter();
            marshaller.marshal(form, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Form.LOG_NAME,
                    fieldWorker, ConstraintViolations.INVALID_LOCATION_UUID, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }

        visit.setVisitLocation(location);


        // check if the visit's extId needs to be changed because of an underlying location extId change
        if (!location.getExtId().equalsIgnoreCase(form.getLocationExtId())) {
            String newExtId = form.getVisitExtId();

            // 2015-02-19_  +  M395S01E62P1
            newExtId = newExtId.substring(0, 11) + location.getExtId();
            visit.setExtId(newExtId);
        } else {
            visit.setExtId(form.getVisitExtId());
        }

        visit.setUuid(form.getVisitUuid());

        //check to see if Visit with the same uuid already exists: visits with the same uuid
        //by definition will not contain different data, so there's no need to call an update()
        //this makes it possible to create multiple visits with the same extId, which would
        //have to be combined at reporting time.
        //we have to do this by uuid because other entities will want to refer to this visit
        //by uuid.
        Visit existingVisit = visitService.findVisitByUuid(form.getVisitUuid());
        if (null == existingVisit) {
            try {
                visitService.createVisit(visit);
            } catch (ConstraintViolations e) {
                StringWriter writer = new StringWriter();
                marshaller.marshal(form, writer);
                ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, Form.LOG_NAME,
                        fieldWorker, ErrorConstants.CONSTRAINT_VIOLATION, e.getViolations());
                errorService.logError(error);
                return requestError(e);
            } catch (Exception e) {
                return serverError("General Error updating or saving visit"
                        + e.getMessage());
            }
        }

        return new ResponseEntity<>(form, HttpStatus.CREATED);
    }


    @Description(description = "Model data from the Visit xform for the Bioko island project.")
    @XmlRootElement(name = "visitForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "VisitForm";

        private static final long serialVersionUID = 6052940190094850124L;

        //core form fields
        @XmlElement(name = "processed_by_mirth")
        private boolean processedByMirth;

        @XmlElement(name = "entity_uuid")
        private String uuid;

        @XmlElement(name = "entity_ext_id")
        private String entityExtId;

        @XmlElement(name = "field_worker_ext_id")
        private String fieldworkerExtId;

        @XmlElement(name = "field_worker_uuid")
        private String fieldWorkerUuid;

        @XmlElement(name = "collection_date_time")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar collectionDateTime;

        //visit form fields
        @XmlElement(name = "visit_ext_id")
        private String visitExtId;

        @XmlElement(name = "visit_uuid")
        private String visitUuid;

        @XmlElement(name = "location_ext_id")
        private String locationExtId;

        @XmlElement(name = "location_uuid")
        private String locationUuid;

        @XmlElement(name = "visit_date")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar visitDate;

        public String getVisitUuid() {
            return visitUuid;
        }

        public void setVisitUuid(String visitUuid) {
            this.visitUuid = visitUuid;
        }

        public String getLocationUuid() {
            return locationUuid;
        }

        public void setLocationUuid(String locationUuid) {
            this.locationUuid = locationUuid;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
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

        public String getVisitExtId() {
            return visitExtId;
        }

        public void setVisitExtId(String visitExtId) {
            this.visitExtId = visitExtId;
        }

        public String getFieldworkerExtId() {
            return fieldworkerExtId;
        }

        public void setFieldworkerExtId(String fieldworkerExtId) {
            this.fieldworkerExtId = fieldworkerExtId;
        }

        public String getLocationExtId() {
            return locationExtId;
        }

        public void setLocationExtId(String locationExtId) {
            this.locationExtId = locationExtId;
        }

        public Calendar getVisitDate() {
            return visitDate;
        }

        public void setVisitDate(Calendar visitDate) {
            this.visitDate = visitDate;
        }

    }
}
