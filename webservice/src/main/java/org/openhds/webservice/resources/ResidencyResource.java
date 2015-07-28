package org.openhds.webservice.resources;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openhds.controller.service.ResidencyService;
import org.openhds.domain.model.Residency;
import org.openhds.domain.model.Residency.Residencies;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.support.FileResolver;
import org.openhds.controller.util.CacheResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/residencies")
public class ResidencyResource {

    private FileResolver fileResolver;
    
    private ResidencyService residencyService;
    
    private static final Logger logger = LoggerFactory.getLogger(ResidencyResource.class);

    @Autowired
    private CacheResponseWriter cacheResponseWriter;

    @Autowired
    public ResidencyResource(FileResolver fileResolver, ResidencyService residencyService) {
    	this.fileResolver = fileResolver;
    	this.residencyService = residencyService;
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<? extends Serializable> getAllResidencies() {
    	int count = (int) residencyService.getTotalResidencyCount();
        List<Residency> residencies = residencyService.getAllResidenciesInRange(0, count);
        List<Residency> copies = new ArrayList<Residency>(residencies.size());

        for (Residency r : residencies) {
        	Residency copy = ShallowCopier.makeShallowCopy(r);
            copies.add(copy);
        }

        Residencies allResidencies = new Residency.Residencies();
        allResidencies.setResidencies(copies);
        return new ResponseEntity<Residencies>(allResidencies, HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "/cached", method = RequestMethod.GET)
    public void getCachedResidencies(HttpServletResponse response) {
        try {
            cacheResponseWriter.writeResponse(MediaType.APPLICATION_XML_VALUE, fileResolver.resolveResidencyXmlFile(), response);
        } catch (IOException e) {
            logger.error("Problem writing residency xml file: " + e.getMessage());
        }
    }
    
	
}
