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

@Component("membershipXmlWriter")
public class MembershipXmlWriterTask extends XmlWriterTemplate<MembershipXmlWriterTask.MembershipXml> {

    @Autowired
    public MembershipXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.MEMBERSHIP_TASK_NAME);
    }

    @Override
    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createSQLQuery(query)
                .addScalar("uuid")
                .addScalar("indiv")
                .addScalar("bIsToA")
                .addScalar("socialGroup")
                .setResultTransformer(aliasToBean(MembershipXml.class));
    }

    @Override
    protected String getExportQuery() {
        return "select * from v_membership_sync";
    }

    @Override
    protected void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, MembershipXml original)
            throws JAXBException {
        marshaller.marshal(original, xmlStreamWriter);
    }

    @Override
    protected Class<?> getBoundClass() {
        return MembershipXml.class;
    }

    @Override
    protected String getStartElementName() {
        return "memberships";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "membership")
    public static class MembershipXml {
        public String uuid;
        public String indiv;
        public String bIsToA;
        public String socialGroup;
    }

}
