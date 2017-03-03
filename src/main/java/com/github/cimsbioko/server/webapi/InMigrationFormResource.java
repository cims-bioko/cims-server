package com.github.cimsbioko.server.webapi;


import com.github.cimsbioko.server.controller.service.VisitService;
import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.controller.service.refactor.InMigrationService;
import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.controller.service.refactor.LocationService;
import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.model.*;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import com.github.cimsbioko.server.domain.util.UUIDGenerator;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.CurrentUser;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
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
import java.util.Date;


@Controller
@RequestMapping("/inMigrationForm")
public class InMigrationFormResource extends AbstractFormResource {

    @Autowired
    protected CurrentUser currentUser;

    @Autowired
    protected CalendarUtil calendarUtil;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private SitePropertiesService sitePropertiesService;

    @Autowired
    private InMigrationService inMigrationService;

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
            throw new RuntimeException("Could not create JAXB context and marshaller for InMigrationFormResource");
        }

        InMigration inMigration = new InMigration();

        inMigration.setRecordedDate(form.getMigrationDate());
        inMigration.setReason(form.getMigrationReason());
        inMigration.setOrigin(form.getMigrationOrigin());

        //TODO: determine a consistent configuration plan between siteProperties and MigrationType enum
        //TODO: (question) why use an enum and not the siteproperties?
        if ("internal_inmigration".equals(form.getMigrationType())) {
            inMigration.setMigType(MigrationType.INTERNAL_INMIGRATION);
        } else {
            inMigration.setMigType(MigrationType.EXTERNAL_INMIGRATION);
        }

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(form.getFieldWorkerUuid());
        if (null == fieldWorker) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_UUID + " : " + form.getFieldWorkerUuid());
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_FIELD_WORKER_UUID);
            return requestError(cv);
        }
        inMigration.setCollectedBy(fieldWorker);

        Visit visit = visitService.findVisitByUuid(form.getVisitUuid());
        if (null == visit) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_VISIT_UUID + " : " + form.getVisitUuid());
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_VISIT_UUID);
            return requestError(cv);
        }
        inMigration.setVisit(visit);

        Individual individual = individualService.getByUuid(form.getIndividualUuid());
        if (null == individual) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_UUID + " : " + form.getIndividualUuid());
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_INDIVIDUAL_UUID);
            return requestError(cv);
        }
        inMigration.setIndividual(individual);

        Location location = locationService.getByUuid(form.getLocationUuid());
        if (null == location) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_LOCATION_UUID + " : " + form.getLocationUuid());
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_LOCATION_UUID);
            return requestError(cv);
        }

        Residency newResidency = new Residency();

        //TODO: since the InMigration domain model contains a reference to a Residency instead of a Location,
        //we must assemble the Residency at this level to provide a fully-graphed InMigration to the service
        newResidency.setCollectedBy(fieldWorker);

        newResidency.setLocation(location);
        newResidency.setIndividual(individual);
        newResidency.setUuid(UUIDGenerator.generate());
        newResidency.setStartDate(inMigration.getRecordedDate());
        newResidency.setStartType(sitePropertiesService.getInmigrationCode());
        newResidency.setEndType(sitePropertiesService.getNotApplicableCode());

        if (null != currentUser) {
            newResidency.setInsertBy(currentUser.getCurrentUser());
        }

        Calendar insertDate = calendarUtil.convertDateToCalendar(new Date());
        newResidency.setInsertDate(insertDate);

        newResidency.setStatus(sitePropertiesService.getDataStatusPendingCode());

        inMigration.setResidency(newResidency);

        try {
            inMigrationService.create(inMigration);
        } catch (ConstraintViolations cv) {
            logError(cv, fieldWorker, createDTOPayload(form), Form.LOG_NAME, ConstraintViolations.INVALID_IN_MIGRATION);
            return requestError(cv);
        }

        return new ResponseEntity<>(form, HttpStatus.CREATED);

    }

    private String createDTOPayload(Form form) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();
    }


    @Description(description = "Model data from the InMigration xform for the Bioko island project.")
    @XmlRootElement(name = "inMigrationForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "InMigrationForm";

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

        //InMigr form fields
        @XmlElement(name = "visit_ext_id")
        private String visitExtId;

        @XmlElement(name = "visit_uuid")
        private String visitUuid;


        @XmlElement(name = "location_ext_id")
        private String locationExtId;

        @XmlElement(name = "location_uuid")
        private String locationUuid;

        @XmlElement(name = "individual_ext_id")
        private String individualExtId;

        @XmlElement(name = "individual_uuid")
        private String individualUuid;

        @XmlElement(name = "migration_type")
        private String migrationType;

        @XmlElement(name = "migration_date")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar migrationDate;

        @XmlElement(name = "migration_origin")
        private String migrationOrigin;

        @XmlElement(name = "migration_reason")
        private String migrationReason;

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

        public String getVisitExtId() {
            return visitExtId;
        }

        public void setVisitExtId(String visitExtId) {
            this.visitExtId = visitExtId;
        }

        public String getMigrationType() {
            return migrationType;
        }

        public void setMigrationType(String migrationType) {
            this.migrationType = migrationType;
        }

        public String getIndividualExtId() {
            return individualExtId;
        }

        public void setIndividualExtId(String individualExtId) {
            this.individualExtId = individualExtId;
        }

        public Calendar getCollectionDateTime() {
            return collectionDateTime;
        }

        public void setCollectionDateTime(Calendar collectionDateTime) {
            this.collectionDateTime = collectionDateTime;
        }

        public String getLocationExtId() {
            return locationExtId;
        }

        public void setLocationExtId(String locationExtId) {
            this.locationExtId = locationExtId;
        }

        public Calendar getMigrationDate() {
            return migrationDate;
        }

        public void setMigrationDate(Calendar migrationDate) {
            this.migrationDate = migrationDate;
        }

        public String getMigrationOrigin() {
            return migrationOrigin;
        }

        public void setMigrationOrigin(String migrationOrigin) {
            this.migrationOrigin = migrationOrigin;
        }

        public String getMigrationReason() {
            return migrationReason;
        }

        public void setMigrationReason(String migrationReason) {
            this.migrationReason = migrationReason;
        }

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

        public String getIndividualUuid() {
            return individualUuid;
        }

        public void setIndividualUuid(String individualUuid) {
            this.individualUuid = individualUuid;
        }
    }
}
