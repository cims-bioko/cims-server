package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.FieldWorkerService;
import org.openhds.controller.service.IndividualService;
import org.openhds.controller.service.OutMigrationService;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.OutMigration;
import org.openhds.domain.model.Visit;
import org.openhds.domain.model.bioko.OutMigrationForm;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.webservice.WebServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;

@Controller
@RequestMapping("/outMigrationForm")
public class OutMigrationFormResource extends AbstractFormResource {

    @Autowired
    OutMigrationService outMigrationService;

    @Autowired
    IndividualService individualService;

    @Autowired
    FieldWorkerService fieldWorkerService;

    @Autowired
    VisitService visitService;

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody OutMigrationForm outMigrationForm) {

        OutMigration outMigration = new OutMigration();
        outMigration.setRecordedDate(outMigrationForm.getDateOfMigration());
        outMigration.setDestination(outMigrationForm.getNameOfDestination());
        outMigration.setReason(outMigrationForm.getReasonForOutMigration());

        Individual individual = individualService.findIndivById(outMigrationForm.getIndividualExtId());
        if (null == individual) {
            return requestError("The Individual referenced by the OutMigration does not exist");
        }
        outMigration.setIndividual(individual);

        FieldWorker fieldWorker;
        try {
            fieldWorker = fieldWorkerService.findFieldWorkerById(outMigrationForm.getFieldWorkerExtId());
        } catch (ConstraintViolations e) {
            return requestError(e);
        }
        outMigration.setCollectedBy(fieldWorker);

        Visit visit;
        try {
            visit = visitService.findVisitById(outMigrationForm.getVisitExtId(), "Null visit will throw this exception");
        } catch (Exception e) {
            return requestError("The Visit referenced by the OutMigration does not exist");
        }
        outMigration.setVisit(visit);

        try {
            outMigrationService.createOutMigration(outMigration);
        } catch (ConstraintViolations e) {
            return requestError(e);
        }

        return new ResponseEntity<OutMigrationForm>(outMigrationForm, HttpStatus.CREATED);

    }

}