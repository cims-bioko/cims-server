package org.openhds.webservice.resources.bioko;

import java.io.Serializable;
import java.sql.SQLException;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.FieldWorkerService;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Visit;
import org.openhds.domain.model.bioko.VisitForm;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/visitForm")
public class VisitFormResource {

	@Autowired
	private EntityService entityService;

	@Autowired
	private VisitService visitService;

	@Autowired
	private FieldWorkerService fieldWorkerService;

	@Autowired
	private LocationHierarchyService locationHierarchyService;

	@Autowired
	private FieldBuilder fieldBuilder;

	@RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
	@Transactional
	public ResponseEntity<? extends Serializable> processForm(
			@RequestBody VisitForm visitForm) {

		ConstraintViolations cv = new ConstraintViolations();
		Visit visit = new Visit();

		// Bioko project does not support Rounds, all visits will be considered
		// as Round 1 for conveniency
		// TODO: Should the visit model and underlying schema be stripped of
		// "round"?
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

		// persist the visit
		try {
			visitService.createVisit(visit);
		} catch (ConstraintViolations e) {
			return requestError(e);
		} catch (Exception e) {
			return serverError("General Error updating or saving visit"
					+ e.getMessage());
		}

		return new ResponseEntity<VisitForm>(visitForm, HttpStatus.CREATED);

	}

	private ResponseEntity<WebServiceCallException> requestError(String message) {
		WebServiceCallException error = new WebServiceCallException();
		error.getErrors().add(message);
		return new ResponseEntity<WebServiceCallException>(error,
				HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<WebServiceCallException> serverError(String message) {
		WebServiceCallException error = new WebServiceCallException();
		error.getErrors().add(message);
		return new ResponseEntity<WebServiceCallException>(error,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<WebServiceCallException> requestError(
			ConstraintViolations cv) {
		return new ResponseEntity<WebServiceCallException>(
				new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
	}

}
