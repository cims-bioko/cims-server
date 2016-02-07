package org.openhds.task;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionContract;
import org.hibernate.type.StringType;
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

@Component("locationXmlWriter")
public class LocationSyncFileTask extends SyncFileTemplate<LocationSyncFileTask.LocationXml> {

    @Autowired
    public LocationSyncFileTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.LOCATION_TASK_NAME);
    }

    @Override
    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createSQLQuery(query)
                .addScalar("uuid")
                .addScalar("extId")
                .addScalar("hierUuid")
                .addScalar("hierExtId")
                .addScalar("name")
                .addScalar("description")
                .addScalar("community")
                .addScalar("communityCode")
                .addScalar("locality")
                .addScalar("map")
                .addScalar("sector")
                .addScalar("building", StringType.INSTANCE)
                .addScalar("floor", StringType.INSTANCE)
                .addScalar("latitude")
                .addScalar("longitude")
                .setResultTransformer(aliasToBean(LocationXml.class));
    }

    @Override
    protected String getExportQuery() {
        return "select * from v_location_sync";
    }

    @Override
    protected void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, LocationXml original)
            throws JAXBException {
        marshaller.marshal(original, xmlStreamWriter);
    }

    @Override
    protected Class<?> getBoundClass() {
        return LocationXml.class;
    }

    @Override
    protected String getStartElementName() {
        return "locations";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "location")
    public static class LocationXml {
        public String uuid;
        public String extId;
        public String hierUuid;
        public String hierExtId;
        public String name;
        public String description;
        public String community;
        public String communityCode;
        public String locality;
        public String map;
        public String sector;
        public String building;
        public String floor;
        public String latitude;
        public String longitude;
    }

}
