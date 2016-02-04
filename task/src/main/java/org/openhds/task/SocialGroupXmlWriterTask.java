package org.openhds.task;

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

@Component("socialGroupXmlWriter")
public class SocialGroupXmlWriterTask extends XmlWriterTemplate<SocialGroupXmlWriterTask.SocialGroupXml> {

    @Autowired
    public SocialGroupXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.SOCIALGROUP_TASK_NAME);
    }

    @Override
    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createSQLQuery(query)
                .addScalar("uuid")
                .addScalar("head")
                .addScalar("location")
                .addScalar("name")
                .setResultTransformer(aliasToBean(SocialGroupXml.class));
    }

    @Override
    protected String getExportQuery() {
        return "select * from v_socialgroup_sync";
    }

    @Override
    protected void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, SocialGroupXml original)
            throws JAXBException {
        marshaller.marshal(original, xmlStreamWriter);
    }

    @Override
    protected Class<?> getBoundClass() {
        return SocialGroupXml.class;
    }

    @Override
    protected String getStartElementName() {
        return "socialgroups";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "socialgroup")
    public static class SocialGroupXml {
        public String uuid;
        public String head;
        public String location;
        public String name;
    }

}
