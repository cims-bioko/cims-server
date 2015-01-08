package org.openhds.webservice.resources.bioko;

import java.io.Serializable;
import java.io.StringWriter;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.*;
import org.openhds.domain.model.ErrorLog;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Visit;
import org.openhds.domain.model.bioko.VisitForm;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.errorhandling.util.ErrorLogUtil;
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

@Controller
@RequestMapping("/visitForm")
public class VisitFormResource extends AbstractFormResource {

    private static final Logger logger = LoggerFactory.getLogger(VisitFormResource.class);

    @Autowired
	private LocationHierarchyService locationHierarchyService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private JAXBContext context = null;
    private Marshaller marshaller = null;

	@RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
	@Transactional
	public ResponseEntity<? extends Serializable> processForm(@RequestBody VisitForm visitForm) throws JAXBException {

        try {
            this.context = JAXBContext.newInstance(VisitForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for visit form resource");
        }

        Visit visit = new Visit();

		// Bioko project does not support Rounds, all visits will be considered
		// as Round 1
		visit.setRoundNumber(1);
		visit.setVisitDate(visitForm.getVisitDate());

		FieldWorker fieldWorker = fieldWorkerService.findFieldWorkerByExtId(visitForm.getFieldworkerExtId());
        if (null == fieldWorker) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_EXT_ID);
            StringWriter writer = new StringWriter();
            marshaller.marshal(visitForm, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, VisitForm.class.getSimpleName(),
                    null, ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
		visit.setCollectedBy(fieldWorker);

		Location location = locationHierarchyService.findLocationById(visitForm.getLocationExtId());
        if (null == location) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_LOCATION_EXT_ID);
            StringWriter writer = new StringWriter();
            marshaller.marshal(visitForm, writer);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, writer.toString(), null, VisitForm.class.getSimpleName(),
                    fieldWorker, ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }

		visit.setVisitLocation(location);

        visit.setExtId(visitForm.getVisitExtId());

        //check to see if Visit with the same extId already exists: visits with the same extId
        //by definition will not contain different data, so there's no need to call an update()
        Visit exisitingVisit = visitService.findVisitById(visitForm.getVisitExtId());
        if (null == exisitingVisit) {
            try {
                visitService.createVisit(visit);
            } catch (ConstraintViolations e) {
                return requestError(e);
            } catch (Exception e) {
                return serverError("General Error updating or saving visit"
                        + e.getMessage());
            }
        }

		return new ResponseEntity<VisitForm>(visitForm, HttpStatus.CREATED);
	}

}
