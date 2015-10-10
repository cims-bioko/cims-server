package org.openhds.webservice.resources;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.IndividualService;
import org.openhds.controller.service.MembershipService;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Membership.Memberships;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/memberships")
public class MembershipResource {

    private final MembershipService membershipService;

    private final IndividualService individualService;

    private final FieldBuilder fieldBuilder;

    @Autowired
    public MembershipResource(MembershipService membershipService, IndividualService individualService,
                              FieldBuilder fieldBuilder) {
        this.membershipService = membershipService;
        this.individualService = individualService;
        this.fieldBuilder = fieldBuilder;
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<? extends Serializable> getAllMembershipsByIndividualId(@PathVariable String extId) {

        Individual individual = individualService.findIndivById(extId);
        if (individual == null) {
            return new ResponseEntity<>("No such individual.", HttpStatus.NOT_FOUND);
        }


        List<Membership> memberships = membershipService.getAllMemberships();
        List<Membership> copies = new ArrayList<>(memberships.size());

        for (Membership m : memberships) {
            Membership copy = ShallowCopier.makeShallowCopy(m);
            copies.add(copy);
        }

        Memberships allMemberships = new Memberships();
        allMemberships.setMemberships(copies);
        return new ResponseEntity<>(allMemberships, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<? extends Serializable> getAllMemberships() {
        List<Membership> memberships = membershipService.getAllMemberships();
        List<Membership> copies = new ArrayList<>(memberships.size());

        for (Membership m : memberships) {
            Membership copy = ShallowCopier.makeShallowCopy(m);
            copies.add(copy);
        }

        Memberships allMemberships = new Membership.Memberships();
        allMemberships.setMemberships(copies);
        return new ResponseEntity<>(allMemberships, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    public ResponseEntity<? extends Serializable> insertXml(@RequestBody Membership membership) {
        ConstraintViolations cv = new ConstraintViolations();
        membership.setCollectedBy(fieldBuilder.referenceField(membership.getCollectedBy(), cv));
        membership.setSocialGroup(fieldBuilder.referenceField(membership.getSocialGroup(), cv));
        membership.setIndividual(fieldBuilder.referenceField(membership.getIndividual(), cv));

        if (cv.hasViolations()) {
            return new ResponseEntity<>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        try {
            membershipService.createMembership(membership);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ShallowCopier.makeShallowCopy(membership), HttpStatus.CREATED);
    }
}
