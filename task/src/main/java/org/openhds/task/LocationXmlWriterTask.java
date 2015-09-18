package org.openhds.task;

import org.hibernate.SessionFactory;
import org.openhds.domain.model.Location;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("locationXmlWriter")
public class LocationXmlWriterTask extends XmlWriterTemplate<Location> {

    @Autowired
    public LocationXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.LOCATION_TASK_NAME);
    }

    @Override
    protected Location makeCopyOf(Location original) {
        return ShallowCopier.makeShallowCopy(original);
    }

    @Override
    protected String getExportQuery() {
        return "from Location l" +
                " join fetch l.locationHierarchy h" +
                " join fetch h.level" +
                " where l.deleted = false";
    }

    @Override
    protected Class<?> getBoundClass() {
        return Location.class;
    }

    @Override
    protected String getStartElementName() {
        return "locations";
    }

}
