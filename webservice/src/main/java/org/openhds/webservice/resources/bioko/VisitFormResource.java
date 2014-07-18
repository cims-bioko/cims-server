package org.openhds.webservice.resources.bioko;

import java.io.Serializable;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.FieldWorkerService;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Visit;
import org.openhds.domain.model.bioko.VisitForm;
import org.openhds.webservice.WebServiceCallException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/visitForm")
public class VisitFormResource extends AbstractFormResource {

    private static final Logger logger = LoggerFactory.getLogger(VisitFormResource.class);

	@Autowired
	private EntityService entityService;

	@Autowired
	private VisitService visitService;

	@Autowired
	private FieldWorkerService fieldWorkerService;

	@Autowired
	private LocationHierarchyService locationHierarchyService;


	@RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
	@Transactional
	public ResponseEntity<? extends Serializable> processForm(
			@RequestBody VisitForm visitForm) {

        Visit visit = new Visit();

		// Bioko project does not support Rounds, all visits will be considered
		// as Round 1
		visit.setRoundNumber(1);
		visit.setVisitDate(visitForm.getVisitDate());

		FieldWorker fieldWorker;
		try {
			fieldWorker = fieldWorkerService.findFieldWorkerById(
					visitForm.getFieldworkerExtId(),
					"Visit form has non-existent field worker extId");
		} catch (Exception e) {
			return requestError("Error getting field worker" + e.getMessage());
		}
		visit.setCollectedBy(fieldWorker);

		Location location;
		try {
			location = locationHierarchyService.findLocationById(visitForm
					.getLocationExtId());
		} catch (Exception e) {
			return requestError("Error getting location" + e.getMessage());
		}
		visit.setVisitLocation(location);

        visit.setExtId(visitForm.getVisitExtId());

        //check to see if Visit with the same extId already exists: visits with the same extId
        //by definition will not contain different data, so there's no need to call an update()
        Visit exisitingVisit = null;
        try {
            exisitingVisit = visitService.findVisitById(visitForm.getVisitExtId(), "Non-existent visit will throw an exception");
        } catch (Exception e) {

        }
        if (null == exisitingVisit) {
            // persist the visit
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
