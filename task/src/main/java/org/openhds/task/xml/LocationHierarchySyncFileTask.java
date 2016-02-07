package org.openhds.task.xml;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionContract;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamWriter;

import static org.hibernate.transform.Transformers.aliasToBean;

@Component("locationHierarchyXmlWriter")
public class LocationHierarchySyncFileTask extends SyncFileTemplate<LocationHierarchySyncFileTask.LocationHierarchyXml> {

    @Autowired
    public LocationHierarchySyncFileTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.LOCATIONHIERARCHY_TASK_NAME);
    }

    @Override
    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createSQLQuery(query)
                .addScalar("uuid")
                .addScalar("extId")
                .addScalar("name")
                .addScalar("level")
                .addScalar("parent")
                .setResultTransformer(aliasToBean(LocationHierarchyXml.class));
    }

    @Override
    protected String getExportQuery() {
        return "select * from v_locationhierarchy_sync";
    }

    @Override
    protected void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, LocationHierarchyXml original)
            throws JAXBException {
        marshaller.marshal(original, xmlStreamWriter);
    }

    @Override
    protected Class<?> getBoundClass() {
        return LocationHierarchyXml.class;
    }

    @Override
    protected String getStartElementName() {
        return "locationHierarchies";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "hierarchy")
    public static class LocationHierarchyXml {
        public String uuid;
        public String extId;
        public String name;
        public String level;
        public String parent;
    }

}