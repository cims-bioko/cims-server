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

@Component("fieldWorkerXmlWriter")
public class FieldWorkerSyncFileTask extends SyncFileTemplate<FieldWorkerSyncFileTask.FieldWorkerXml> {

    @Autowired
    public FieldWorkerSyncFileTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.FIELDWORKER_TASK_NAME);
    }

    @Override
    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createSQLQuery(query)
                .addScalar("uuid")
                .addScalar("extId")
                .addScalar("id")
                .addScalar("pass")
                .addScalar("firstName")
                .addScalar("lastName")
                .setResultTransformer(aliasToBean(FieldWorkerXml.class));
    }

    @Override
    protected String getExportQuery() {
        return "select * from v_fieldworker_sync";
    }

    @Override
    protected void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, FieldWorkerXml original)
            throws JAXBException {
        marshaller.marshal(original, xmlStreamWriter);
    }

    @Override
    protected Class<?> getBoundClass() {
        return FieldWorkerXml.class;
    }

    @Override
    protected String getStartElementName() {
        return "fieldworkers";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "fieldworker")
    public static class FieldWorkerXml {
        public String uuid;
        public String extId;
        public Integer id;
        public String pass;
        public String firstName;
        public String lastName;
    }

}
