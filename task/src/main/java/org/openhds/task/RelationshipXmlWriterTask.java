package org.openhds.task;


import org.hibernate.SessionFactory;
import org.openhds.domain.model.Relationship;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("relationshipXmlWriter")
public class RelationshipXmlWriterTask extends XmlWriterTemplate<Relationship> {

    @Autowired
    public RelationshipXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.RELATIONSHIP_TASK_NAME);
    }

    @Override
    protected Relationship makeCopyOf(Relationship original) {
        return ShallowCopier.makeShallowCopy(original);
    }

    @Override
    protected String getExportQuery() {
        return "from Relationship" +
                " where deleted = false";
    }

    @Override
    protected Class<?> getBoundClass() {
        return Relationship.class;
    }

    @Override
    protected String getStartElementName() {
        return "relationships";
    }

}
