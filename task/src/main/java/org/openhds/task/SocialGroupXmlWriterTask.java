package org.openhds.task;

import org.hibernate.SessionFactory;
import org.openhds.domain.model.SocialGroup;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("socialGroupXmlWriter")
public class SocialGroupXmlWriterTask extends XmlWriterTemplate<SocialGroup> {

    @Autowired
    public SocialGroupXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.SOCIALGROUP_TASK_NAME);
    }

    @Override
    protected String getExportQuery() {
        return "from SocialGroup" +
                " where deleted = false";
    }

    @Override
    protected Class<?> getBoundClass() {
        return SocialGroup.class;
    }

    @Override
    protected String getStartElementName() {
        return "socialgroups";
    }

}
