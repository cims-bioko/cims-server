package org.openhds.webservice.resources;

import org.openhds.controller.service.ResidencyService;
import org.openhds.domain.model.Residency;
import org.openhds.domain.model.Residency.Residencies;
import org.openhds.domain.util.ShallowCopier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/residencies")
public class ResidencyResource {

    private ResidencyService residencyService;

    @Autowired
    public ResidencyResource(ResidencyService residencyService) {
        this.residencyService = residencyService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<? extends Serializable> getAllResidencies() {
        int count = (int) residencyService.getTotalResidencyCount();
        List<Residency> residencies = residencyService.getAllResidenciesInRange(null, count);
        List<Residency> copies = new ArrayList<Residency>(residencies.size());

        for (Residency r : residencies) {
            Residency copy = ShallowCopier.makeShallowCopy(r);
            copies.add(copy);
        }

        Residencies allResidencies = new Residency.Residencies();
        allResidencies.setResidencies(copies);
        return new ResponseEntity<Residencies>(allResidencies, HttpStatus.OK);
    }
}
