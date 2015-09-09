package org.openhds.task;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openhds.controller.service.LocationHierarchyService;
import org.openhds.domain.model.Location;
import org.openhds.domain.util.CalendarUtil;
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
                " left join fetch l.locationHierarchy h" +
                " left join fetch l.residencies r" +
                " left join fetch h.parent p" +
                " left join fetch h.level" +
                " left join fetch p.level" +
                " left join fetch l.collectedBy" +
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
