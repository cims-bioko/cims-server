package org.openhds.task.xml;

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

@Component("individualXmlWriter")
public class IndividualSyncFileTask extends SyncFileTemplate<IndividualSyncFileTask.IndividualXml> {

    @Autowired
    public IndividualSyncFileTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
        super(asyncTaskService, factory, AsyncTaskService.INDIVIDUAL_TASK_NAME);
    }

    @Override
    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createSQLQuery(query)
                .addScalar("uuid")
                .addScalar("extId")
                .addScalar("firstName")
                .addScalar("middleName")
                .addScalar("lastName")
                .addScalar("dob", CalendarType.INSTANCE)
                .addScalar("gender")
                .addScalar("residenceLocation")
                .addScalar("residenceEndType")
                .addScalar("dip")
                .addScalar("age")
                .addScalar("ageUnits")
                .addScalar("phone")
                .addScalar("otherPhone")
                .addScalar("pOCName")
                .addScalar("pOCPhone")
                .addScalar("memberStatus")
                .addScalar("language")
                .addScalar("nationality")
                .setResultTransformer(aliasToBean(IndividualXml.class));
    }

    @Override
    protected String getExportQuery() {
        return "select * from v_individual_sync";
    }

    @Override
    protected void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, IndividualXml original)
            throws JAXBException {
        marshaller.marshal(original, xmlStreamWriter);
    }

    @Override
    protected Class<?> getBoundClass() {
        return IndividualXml.class;
    }

    @Override
    protected String getStartElementName() {
        return "individuals";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "individual")
    public static class IndividualXml {

        public String uuid;
        public String extId;
        public String firstName;
        public String middleName;
        public String lastName;
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        public Calendar dob;
        public String gender;
        public String residenceLocation;
        public String residenceEndType;
        public Integer dip;
        public Integer age;
        public String ageUnits;
        public String phone;
        public String otherPhone;
        public String pOCName;
        public String pOCPhone;
        public String memberStatus;
        public String language;
        public String nationality;
    }

}
