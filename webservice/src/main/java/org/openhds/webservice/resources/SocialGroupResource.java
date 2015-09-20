package org.openhds.webservice.resources;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.SocialGroupService;
import org.openhds.domain.model.SocialGroup;
import org.openhds.domain.model.SocialGroup.SocialGroups;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/socialgroups")
public class SocialGroupResource {
    private static final Logger logger = LoggerFactory.getLogger(SocialGroupResource.class);

    private SocialGroupService socialGroupService;
    private FieldBuilder fieldBuilder;
    private FileResolver fileResolver;

    @Autowired
    private CacheResponseWriter cacheResponseWriter;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    public SocialGroupResource(SocialGroupService socialGroupService, FieldBuilder fieldBuilder,
            FileResolver fileResolver) {
        this.socialGroupService = socialGroupService;
        this.fieldBuilder = fieldBuilder;
        this.fileResolver = fileResolver;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public SocialGroups getAllSocialGroups() {
        List<SocialGroup> allSocialGroups = socialGroupService.getAllSocialGroups();
        List<SocialGroup> copies = new ArrayList<SocialGroup>();

        for (SocialGroup sg : allSocialGroups) {
            SocialGroup copy = ShallowCopier.makeShallowCopy(sg);
            copies.add(copy);
        }

        SocialGroups sgs = new SocialGroup.SocialGroups();
        sgs.setSocialGroups(copies);

        return sgs;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<? extends Serializable> insert(@RequestBody SocialGroup socialGroup) {
        ConstraintViolations cv = new ConstraintViolations();

        socialGroup.setCollectedBy(fieldBuilder.referenceField(socialGroup.getCollectedBy(), cv));
        socialGroup.setGroupHead(fieldBuilder.referenceField(socialGroup.getGroupHead(), cv,
                "Invalid Ext Id for Group Head"));

        if (cv.hasViolations()) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv), HttpStatus.BAD_REQUEST);
        }

        try {
            socialGroupService.createSocialGroup(socialGroup);
        } catch (ConstraintViolations e) {
            return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(e), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<SocialGroup>(ShallowCopier.makeShallowCopy(socialGroup), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/cached", method = RequestMethod.GET)
    public void getCachedSocialGroups(HttpServletRequest request, HttpServletResponse response) {

        String contentHash = asyncTaskService.getContentHash(AsyncTaskService.SOCIALGROUP_TASK_NAME);
        String eTag = request.getHeader(CacheHeaders.IF_NONE_MATCH);

        if (eTag != null && eTag.equals(contentHash)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        try {
            cacheResponseWriter.writeResponse(MediaType.APPLICATION_XML_VALUE, fileResolver.resolvesocialGroupXmlFile(), contentHash, response);
        } catch (IOException e) {
            logger.error("Problem writing social group xml file: " + e.getMessage());
        }
    }

}
