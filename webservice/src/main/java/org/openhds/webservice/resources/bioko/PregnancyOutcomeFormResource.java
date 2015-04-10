package org.openhds.webservice.resources.bioko;

import org.openhds.controller.service.ResidencyService;
import org.openhds.controller.service.refactor.FieldWorkerService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.SocialGroupService;
import org.openhds.domain.model.bioko.PregnancyOutcomeCoreForm;
import org.openhds.domain.model.bioko.PregnancyOutcomeOutcomesForm;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by motech on 4/10/15.
 */

@Controller
@RequestMapping("/pregnancyOutcome")
public class PregnancyOutcomeFormResource {

    @Autowired
    private ResidencyService residencyService;

    @Autowired
    private SocialGroupService socialGroupService;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private Marshaller marshaller = null;


    @RequestMapping(value = "/core", method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processCoreForm(@RequestBody PregnancyOutcomeCoreForm coreForm) throws JAXBException {

        try {
            JAXBContext context = JAXBContext.newInstance(PregnancyOutcomeCoreForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyOutcomeFormResource");
        }

        List<String> logMessage = new ArrayList<String>();

        return null;


    }

    @RequestMapping(value = "/outcomes", method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processOutcomesForm(@RequestBody PregnancyOutcomeOutcomesForm outcomesForm) throws JAXBException {

        try {
            JAXBContext context = JAXBContext.newInstance(PregnancyOutcomeOutcomesForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for PregnancyOutcomeFormResource");
        }

        List<String> logMessage = new ArrayList<String>();

        return null;
    }

}
