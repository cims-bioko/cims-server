package org.openhds.task;


import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionContract;
import org.hibernate.type.CalendarType;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.stream.XMLStreamWriter;

import static org.hibernate.transform.Transformers.aliasToBean;

@Component("relationshipXmlWriter")
public class RelationshipXmlWriterTask extends XmlWriterTemplate<RelationshipXmlWriterTask.RelationshipXml> {

    @Autowired
    public RelationshipXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.RELATIONSHIP_TASK_NAME);
    }

    @Override
    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createSQLQuery(query)
                .addScalar("uuid")
                .addScalar("indivA")
                .addScalar("indivB")
                .addScalar("startDate", CalendarType.INSTANCE)
                .addScalar("aIsToB")
                .setResultTransformer(aliasToBean(RelationshipXml.class));
    }

    @Override
    protected String getExportQuery() {
        return "select * from v_relationship_sync";
    }

    @Override
    protected void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, RelationshipXml original)
            throws JAXBException {
        marshaller.marshal(original, xmlStreamWriter);
    }

    @Override
    protected Class<?> getBoundClass() {
        return RelationshipXml.class;
    }

    @Override
    protected String getStartElementName() {
        return "relationships";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "relationship")
    public static class RelationshipXml {
        public String uuid;
        public String indivA;
        public String indivB;
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        public Calendar startDate;
        public String aIsToB;
    }
}
