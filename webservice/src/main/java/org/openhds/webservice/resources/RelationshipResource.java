package org.openhds.webservice.resources;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.RelationshipService;
import org.openhds.domain.model.Relationship;
import org.openhds.domain.model.Relationship.Relationships;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/relationships")
public class RelationshipResource {

    private RelationshipService relationshipService;
    private FieldBuilder fieldBuilder;

    @Autowired
    public RelationshipResource(RelationshipService relationshipService, FieldBuilder fieldBuilder) {
        this.relationshipService = relationshipService;
        this.fieldBuilder = fieldBuilder;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Relationships getAllRelationships() {
        List<Relationship> allRelationships = relationshipService.getAllRelationships();
        List<Relationship> copies = new ArrayList<Relationship>();

        for (Relationship relationship : allRelationships) {
            Relationship copy = ShallowCopier.makeShallowCopy(relationship);
            copies.add(copy);
        }

        Relationships relationships = new Relationship.Relationships();
        relationships.setRelationships(copies);

        return relationships;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<? extends Serializable> insert(@RequestBody Relationship relationship) {
        ConstraintViolations cv = new ConstraintViolations();
        relationship.setIndividualA(fieldBuilder.referenceField(relationship.getIndividualA(), cv,
                "Invalid external id for individual A"));
        relationship.setIndividualB(fieldBuilder.referenceField(relationship.getIndividualB(), cv,
                "Invalid external id for individual B"));
        relationship.setCollectedBy(fieldBuilder.referenceField(relationship.getCollectedBy(), cv));

        if (cv.hasViolations()) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        try {
            relationshipService.createRelationship(relationship);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Relationship>(ShallowCopier.makeShallowCopy(relationship), HttpStatus.CREATED);
    }
}
