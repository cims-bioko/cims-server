package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.PregnancyService;
import org.openhds.controller.service.VisitService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.SocialGroupService;
import org.openhds.domain.annotations.Description;
import org.openhds.domain.model.*;
import org.openhds.domain.service.SitePropertiesService;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.domain.util.UUIDGenerator;
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
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


@Controller
@RequestMapping("/pregnancyOutcomeForm")
public class PregnancyOutcomeFormResource extends AbstractFormResource {

    private static final String START_TYPE = "PregnancyOutcomeForm";

    @Autowired
    private PregnancyService pregnancyService;

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

    @Autowired
    private SitePropertiesService siteProperties;


    @RequestMapping(value = "/core", method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processCoreForm(@RequestBody CoreForm coreForm) throws JAXBException {

        Marshaller marshaller;
        try {
            JAXBContext context = JAXBContext.newInstance(CoreForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyOutcomeFormResource");
        }

        List<String> logMessage = new ArrayList<>();

        PregnancyOutcome pregnancyOutcome = new PregnancyOutcome();
        try {
            fillInCoreFields(coreForm, pregnancyOutcome);
            pregnancyService.createPregnancyOutcome(pregnancyOutcome);
        } catch (ConstraintViolations constraintViolations) {
            logMessage.add(constraintViolations.getMessage());
            String errorDataPayload = createDTOPayload(coreForm, marshaller);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    CoreForm.LOG_NAME, pregnancyOutcome.getCollectedBy(),
                    ConstraintViolations.INVALID_PREGNANCY_OUTCOME_CORE, logMessage);
            errorService.logError(error);
            return requestError(constraintViolations.getMessage());
        }

        return new ResponseEntity<>(coreForm, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/outcomes", method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processOutcomesForm(@RequestBody OutcomesForm outcomesForm) throws JAXBException {

        Marshaller marshaller;
        try {
            JAXBContext context = JAXBContext.newInstance(OutcomesForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyOutcomeFormResource");
        }

        List<String> logMessage = new ArrayList<>();

        try {
            PregnancyOutcome updatedOutcome = fillInOutcomesFields(outcomesForm);
            pregnancyService.createPregnancyOutcome(updatedOutcome);
        } catch (ConstraintViolations cv) {
            logMessage.add(cv.getMessage());
            String errorDataPayload = createDTOPayload(outcomesForm, marshaller);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null,
                    OutcomesForm.LOG_NAME, null,
                    ConstraintViolations.INVALID_PREGNANCY_OUTCOME_CHILD, logMessage);
            errorService.logError(error);
            return requestError(cv.getMessage());
        }



        return new ResponseEntity<>(outcomesForm, HttpStatus.CREATED);
    }

    private PregnancyOutcome fillInOutcomesFields(OutcomesForm outcomesForm) throws ConstraintViolations {

        PregnancyOutcome parentOutcome = pregnancyService.getPregnancyOutcomeByUuid(outcomesForm.getPregnancyOutcomeUuid());
        if (null == parentOutcome) {
            throw new ConstraintViolations("Could not find parent PregnancyOutcome with UUID:" + outcomesForm.getPregnancyOutcomeUuid());
        }

        List<Outcome> existingOutcomes = parentOutcome.getOutcomes();
        if (null == existingOutcomes) {
            existingOutcomes = new ArrayList<>();
        }

        Outcome outcome = new Outcome();
        outcome.setType(outcomesForm.getOutcomeType());
        outcome.setUuid(UUIDGenerator.generate());

        if (outcome.getType().equalsIgnoreCase(siteProperties.getLiveBirthCode())) {
            Individual child = new Individual();
            child.setUuid(outcomesForm.getChildUuid());

            // generate an extId for this child
            child.setExtId(individualService.generateChildExtId(parentOutcome.getMother()));

            child.setCollectedBy(parentOutcome.getCollectedBy());
            child.setFirstName(outcomesForm.getChildFirstName());
            child.setMiddleName(outcomesForm.getChildMiddleName());
            child.setLastName(outcomesForm.getChildLastName());
            child.setGender(outcomesForm.getChildGender());
            child.setNationality(outcomesForm.getChildNationality());

            SocialGroup socialGroup = socialGroupService.getByUuid(outcomesForm.getSocialGroupUuid());
            if (null == socialGroup) {
                throw new ConstraintViolations("Could not find Social Group with UUID: " + outcomesForm.getSocialGroupUuid());
            }

            child.setMother(parentOutcome.getMother());
            child.setFather(parentOutcome.getFather());

            //Instantiate Relationship
            establishRelationship(child, outcomesForm, socialGroup);

            //Instantiate Membership: Delegate to the service entirely?
            Membership m = establishMembership(child, outcomesForm, socialGroup);
            child.getAllMemberships().add(m);
            outcome.setChild(child);
            outcome.setChildMembership(m);
        }
        existingOutcomes.add(outcome);
        return parentOutcome;

    }

    private Membership establishMembership(Individual child, OutcomesForm form, SocialGroup socialGroup) {
        Membership mem = new Membership();
        mem.setUuid(UUIDGenerator.generate());
        mem.setIndividual(child);
        mem.setInsertDate(form.getCollectionDateTime());
        mem.setbIsToA(form.getChildRelationshipToGroupHead());
        mem.setStartDate(form.getCollectionDateTime());
        mem.setCollectedBy(child.getCollectedBy());
        mem.setSocialGroup(socialGroup);
        mem.setStartType(START_TYPE);
        mem.setEndType(NOT_APPLICABLE_END_TYPE);
        return mem;
    }

    private void establishRelationship(Individual child, OutcomesForm form, SocialGroup socialGroup) {
        Relationship rel = new Relationship();
        rel.setUuid(UUIDGenerator.generate());
        rel.setInsertDate(form.getCollectionDateTime());
        rel.setIndividualA(child);
        rel.setIndividualB(socialGroup.getGroupHead());
        rel.setaIsToB(form.getChildRelationshipToGroupHead());
        rel.setCollectedBy(child.getCollectedBy());
        rel.setInsertDate(form.getCollectionDateTime());
        rel.setStartDate(form.getCollectionDateTime());
        child.getAllRelationships1().add(rel);
    }

    private void fillInCoreFields(CoreForm coreForm, PregnancyOutcome pregnancyOutcome) throws ConstraintViolations {

        pregnancyOutcome.setUuid(coreForm.getPregnancyOutcomeUuid());

        pregnancyOutcome.setOutcomeDate(coreForm.getDeliveryDate());

        FieldWorker fieldWorker = fieldWorkerService.getByUuid(coreForm.getFieldWorkerUuid());
        if(null == fieldWorker) {
            throw new ConstraintViolations("Could not find fieldworker with UUID: " + coreForm.getFieldWorkerUuid());
        }
        pregnancyOutcome.setCollectedBy(fieldWorker);

        Visit visit = visitService.findVisitByUuid(coreForm.getVisitUuid());
        if(null == visit) {
            throw new ConstraintViolations("Could not find visit with UUID: " + coreForm.getVisitUuid());
        }
        pregnancyOutcome.setVisit(visit);

        Individual father = individualService.getByUuid(coreForm.getFatherUuid());
        if(null == father) {
            father = individualService.getUnknownIndividual();
        }
        pregnancyOutcome.setFather(father);


        Individual mother = individualService.getByUuid(coreForm.getMotherUuid());
        if(null == mother) {
            throw new ConstraintViolations("Could not find mother with UUID: " + coreForm.getMotherUuid());
        }
        pregnancyOutcome.setMother(mother);
    }

    private String createDTOPayload(OutcomesForm form, Marshaller marshaller) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(form, writer);
        return writer.toString();
    }

    private String createDTOPayload(CoreForm coreForm, Marshaller marshaller) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(coreForm, writer);
        return writer.toString();
    }


    @Description(description = "Model data from the PregnancyOutcome form for the Bioko project. Additional Outcome data is contained in OutcomesForm")
    @XmlRootElement(name = "pregnancyOutcomeCoreForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = CoreForm.LOG_NAME)
    public static class CoreForm implements Serializable {

        public static final String LOG_NAME = "PregnancyOutcomeCoreForm";

        //core form fields
        @XmlElement(name = "pregnancy_outcome_uuid")
        private String pregnancyOutcomeUuid;

        @XmlElement(name = "processed_by_mirth")
        private boolean processedByMirth;

        @XmlElement(name = "field_worker_ext_id")
        private String fieldWorkerExtId;

        @XmlElement(name = "field_worker_uuid")
        private String fieldWorkerUuid;

        @XmlElement(name = "collection_date_time")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar collectionDateTime;

        @XmlElement(name = "visit_uuid")
        private String visitUuid;

        @XmlElement(name = "mother_uuid")
        private String motherUuid;

        @XmlElement(name = "father_uuid")
        private String fatherUuid;

        @XmlElement(name = "socialgroup_uuid")
        private String socialGroupUuid;

        @XmlElement(name = "number_of_outcomes")
        private String numberOfOutcomes;

        @XmlElement(name = "delivery_date")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar deliveryDate;

        public String getPregnancyOutcomeUuid() {
            return pregnancyOutcomeUuid;
        }

        public void setPregnancyOutcomeUuid(String pregnancyOutcomeUuid) {
            this.pregnancyOutcomeUuid = pregnancyOutcomeUuid;
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

        public String getVisitUuid() {
            return visitUuid;
        }

        public void setVisitUuid(String visitUuid) {
            this.visitUuid = visitUuid;
        }

        public String getMotherUuid() {
            return motherUuid;
        }

        public void setMotherUuid(String motherUuid) {
            this.motherUuid = motherUuid;
        }

        public String getFatherUuid() {
            return fatherUuid;
        }

        public void setFatherUuid(String fatherUuid) {
            this.fatherUuid = fatherUuid;
        }

        public String getSocialGroupUuid() {
            return socialGroupUuid;
        }

        public void setSocialGroupUuid(String socialGroupUuid) {
            this.socialGroupUuid = socialGroupUuid;
        }

        public String getNumberOfOutcomes() {
            return numberOfOutcomes;
        }

        public void setNumberOfOutcomes(String numberOfOutcomes) {
            this.numberOfOutcomes = numberOfOutcomes;
        }

        public Calendar getDeliveryDate() {
            return deliveryDate;
        }

        public void setDeliveryDate(Calendar deliveryDate) {
            this.deliveryDate = deliveryDate;
        }
    }


    @XmlRootElement(name = "pregnancyOutcomeOutcomesForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = OutcomesForm.LOG_NAME)
    public static class OutcomesForm implements Serializable{

        public static final String LOG_NAME = "PregnancyOutcomeOutcomesForm";

        @XmlElement(name = "pregnancy_outcome_uuid")
        private String pregnancyOutcomeUuid;

        @XmlElement(name = "collection_date_time")
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar collectionDateTime;

        @XmlElement(name = "processed_by_mirth")
        private boolean processedByMirth;

        @XmlElement(name = "socialgroup_uuid")
        private String socialGroupUuid;

        @XmlElement(name = "outcome_type")
        private String outcomeType;

        @XmlElement(name = "child_uuid")
        private String childUuid;

        @XmlElement(name = "child_first_name")
        private String childFirstName;

        @XmlElement(name = "child_middle_name")
        private String childMiddleName;

        @XmlElement(name = "child_last_name")
        private String childLastName;

        @XmlElement(name = "child_gender")
        private String childGender;

        @XmlElement(name = "child_relationship_to_group_head")
        private String childRelationshipToGroupHead;

        @XmlElement(name = "child_nationality")
        private String childNationality;

        public String getSocialGroupUuid() {
            return socialGroupUuid;
        }

        public void setSocialGroupUuid(String socialGroupUuid) {
            this.socialGroupUuid = socialGroupUuid;
        }

        public String getPregnancyOutcomeUuid() {
            return pregnancyOutcomeUuid;
        }

        public void setPregnancyOutcomeUuid(String pregnancyOutcomeUuid) {
            this.pregnancyOutcomeUuid = pregnancyOutcomeUuid;
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

        public String getOutcomeType() {
            return outcomeType;
        }

        public void setOutcomeType(String outcomeType) {
            this.outcomeType = outcomeType;
        }

        public String getChildUuid() {
            return childUuid;
        }

        public void setChildUuid(String childUuid) {
            this.childUuid = childUuid;
        }

        public String getChildFirstName() {
            return childFirstName;
        }

        public void setChildFirstName(String childFirstName) {
            this.childFirstName = childFirstName;
        }

        public String getChildMiddleName() {
            return childMiddleName;
        }

        public void setChildMiddleName(String childMiddleName) {
            this.childMiddleName = childMiddleName;
        }

        public String getChildLastName() {
            return childLastName;
        }

        public void setChildLastName(String childLastName) {
            this.childLastName = childLastName;
        }

        public String getChildGender() {
            return childGender;
        }

        public void setChildGender(String childGender) {
            this.childGender = childGender;
        }

        public String getChildRelationshipToGroupHead() {
            return childRelationshipToGroupHead;
        }

        public void setChildRelationshipToGroupHead(String childRelationshipToGroupHead) {
            this.childRelationshipToGroupHead = childRelationshipToGroupHead;
        }

        public String getChildNationality() {
            return childNationality;
        }

        public void setChildNationality(String childNationality) {
            this.childNationality = childNationality;
        }
    }
}
