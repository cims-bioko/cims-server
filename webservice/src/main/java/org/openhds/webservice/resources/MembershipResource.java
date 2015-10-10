package org.openhds.webservice.resources;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.IndividualService;
import org.openhds.controller.service.MembershipService;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Membership.Memberships;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.openhds.task.support.FileResolver;
import org.openhds.controller.util.CacheResponseWriter;
import org.openhds.webservice.FieldBuilder;
import org.openhds.webservice.WebServiceCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/memberships")
public class MembershipResource {

    private static final Logger logger = LoggerFactory.getLogger(MembershipResource.class);
	
    private MembershipService membershipService;

    private IndividualService individualService;

    private FieldBuilder fieldBuilder;
    
    private FileResolver fileResolver;

    @Autowired
    private CacheResponseWriter cacheResponseWriter;

    @Autowired
    private AsyncTaskService asyncTaskService;


    @Autowired
    public MembershipResource(MembershipService membershipService, IndividualService individualService,
                              FieldBuilder fieldBuilder, FileResolver fileResolver) {
        this.membershipService = membershipService;
        this.individualService = individualService;
        this.fieldBuilder = fieldBuilder;
        this.fileResolver = fileResolver;
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<? extends Serializable> getAllMembershipsByIndividualId(@PathVariable String extId) {

        Individual individual = individualService.findIndivById(extId);
        if (individual == null) {
            return new ResponseEntity<String>("No such individual.", HttpStatus.NOT_FOUND);
        }


        List<Membership> memberships = membershipService.getAllMemberships();
        List<Membership> copies = new ArrayList<Membership>(memberships.size());

        for (Membership m : memberships) {
            Membership copy = ShallowCopier.makeShallowCopy(m);
            copies.add(copy);
        }

        Memberships allMemberships = new Memberships();
        allMemberships.setMemberships(copies);
        return new ResponseEntity<Memberships>(allMemberships, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<? extends Serializable> getAllMemberships() {
        List<Membership> memberships = membershipService.getAllMemberships();
        List<Membership> copies = new ArrayList<Membership>(memberships.size());

        for (Membership m : memberships) {
            Membership copy = ShallowCopier.makeShallowCopy(m);
            copies.add(copy);
        }

        Memberships allMemberships = new Membership.Memberships();
        allMemberships.setMemberships(copies);
        return new ResponseEntity<Memberships>(allMemberships, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    public ResponseEntity<? extends Serializable> insertXml(@RequestBody Membership membership) {
        ConstraintViolations cv = new ConstraintViolations();
        membership.setCollectedBy(fieldBuilder.referenceField(membership.getCollectedBy(), cv));
        membership.setSocialGroup(fieldBuilder.referenceField(membership.getSocialGroup(), cv));
        membership.setIndividual(fieldBuilder.referenceField(membership.getIndividual(), cv));

        if (cv.hasViolations()) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        try {
            membershipService.createMembership(membership);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Membership>(ShallowCopier.makeShallowCopy(membership), HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/cached", method = RequestMethod.GET)
    public void getCachedMemberships(HttpServletRequest request, HttpServletResponse response) {

        String contentHash = asyncTaskService.getContentHash(AsyncTaskService.MEMBERSHIP_TASK_NAME);
        String eTag = request.getHeader(Headers.IF_NONE_MATCH);

        if (eTag != null && eTag.equals(contentHash)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        try {
            cacheResponseWriter.writeResponse(MediaType.APPLICATION_XML_VALUE, fileResolver.resolveMembershipXmlFile(), contentHash, response);
        } catch (IOException e) {
            logger.error("Problem writing membership xml file: " + e.getMessage());
        }
    }
}
