package org.openhds.webservice.resources;

import org.openhds.controller.service.IndividualService;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Individual.Individuals;
import org.openhds.domain.util.ShallowCopier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/individuals")
public class IndividualResource {

    private IndividualService individualService;

    @Autowired
    public IndividualResource(IndividualService individualService) {
        this.individualService = individualService;
    }

    @RequestMapping(value = "/{extId}", method = RequestMethod.GET)
    public ResponseEntity<? extends Serializable> getIndividualById(@PathVariable String extId) {
        Individual individual = individualService.findIndivById(extId);
        if (individual == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(ShallowCopier.makeShallowCopy(individual), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Individuals getAllIndividuals() {
        List<Individual> allIndividual = individualService.getAllIndividuals();
        List<Individual> copies = new ArrayList<>(allIndividual.size());
        for (Individual individual : allIndividual) {
            Individual copy = ShallowCopier.makeShallowCopy(individual);
            copies.add(copy);
        }

        Individuals individuals = new Individual.Individuals();
        individuals.setIndividuals(copies);

        return individuals;
    }
}
