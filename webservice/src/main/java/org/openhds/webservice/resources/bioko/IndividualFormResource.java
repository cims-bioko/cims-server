package org.openhds.webservice.resources.bioko;

import java.io.Serializable;

import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.controller.service.MembershipService;
import org.openhds.controller.service.RelationshipService;
import org.openhds.domain.model.bioko.IndividualForm;
import org.openhds.webservice.FieldBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/individualForm")
public class IndividualFormResource {
	private static final Logger logger = LoggerFactory
			.getLogger(IndividualFormResource.class);

	private final FieldBuilder fieldBuilder;
	private final LocationHierarchyService locationHierarchyService;
	private final MembershipService membershipService;
	private final RelationshipService relationshipService;

	@Autowired
	public IndividualFormResource(
			LocationHierarchyService locationHierarchyService,
			MembershipService membershipService,
			RelationshipService relationshipService, FieldBuilder fieldBuilder) {
		this.locationHierarchyService = locationHierarchyService;
		this.membershipService = membershipService;
		this.relationshipService = relationshipService;
		this.fieldBuilder = fieldBuilder;
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
	public ResponseEntity<? extends Serializable> processForm(
			@RequestBody IndividualForm individualForm) {

		return new ResponseEntity<IndividualForm>(individualForm,
				HttpStatus.CREATED);
	}

}
