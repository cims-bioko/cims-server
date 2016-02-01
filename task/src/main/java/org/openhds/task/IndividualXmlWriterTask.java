package org.openhds.task;

import org.hibernate.SessionFactory;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Residency;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("individualXmlWriter")
public class IndividualXmlWriterTask extends XmlWriterTemplate<Individual> {

    @Autowired
    public IndividualXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.INDIVIDUAL_TASK_NAME);
    }

    @Override
    protected Individual makeCopyOf(Individual original) {
        // TODO: external systems should not expect Residencies nested within Individuals
        Individual individualCopy = ShallowCopier.makeShallowCopy(original);

        if (!original.getAllResidencies().isEmpty()) {
            Residency residencyCopy = ShallowCopier.makeShallowCopy(original.getCurrentResidency());
            Set<Residency> nestedResidencies = new HashSet<>();
            nestedResidencies.add(residencyCopy);
            individualCopy.setAllResidencies(nestedResidencies);
        }
        return individualCopy;
    }

    @Override
    protected String getExportQuery() {
        return "from Individual i" +
                " join fetch i.allResidencies" +
                " where i.extId != 'UNK'" +
                " and i.deleted = false";
    }

    protected boolean skipEntity(Individual individual) {
        return individual.getCurrentResidency() == null;
    }

    @Override
    protected Class<?> getBoundClass() {
        return Individual.class;
    }

    @Override
    protected String getStartElementName() {
        return "individuals";
    }

}
