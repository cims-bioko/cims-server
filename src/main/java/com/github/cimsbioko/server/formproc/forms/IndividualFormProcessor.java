package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.util.CalendarAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;

@Component
public class IndividualFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(IndividualFormProcessor.class);

    @Transactional
    public void processForm(Form form) throws ConstraintViolations {

    }

    @XmlRootElement(name = "individualForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "IndividualForm";

        private static final long serialVersionUID = 1143017330340385847L;

        private String entityUuid;

        private String fieldWorkerUuid;

        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar collectionDateTime;

        private String householdExtId;

        private String householdUuid;

        private String individualExtId;

        private String individualFirstName;

        private String individualLastName;

        private String individualOtherNames;

        @XmlJavaTypeAdapter(CalendarAdapter.class)
        private Calendar individualDateOfBirth;

        private String individualGender;

        private String individualRelationshipToHeadOfHousehold;

        private String individualPhoneNumber;

        private String individualOtherPhoneNumber;

        private String individualLanguagePreference;

        private String individualPointOfContactName;

        private String individualPointOfContactPhoneNumber;

        private int individualDip;

        private String individualNationality;

        private String individualMemberStatus;
    }
}
