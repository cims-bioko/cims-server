package org.openhds.domain.model.bioko;

import org.openhds.domain.annotations.Description;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Written by Ben Heasly to model incoming forms for the bioko island project.
 * 
 */
@Description(description = "Model data from the Individual xform for the Bioko island project.  Contains Individual, Relationship, and Membership data.")
@XmlRootElement(name = "individualForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class IndividualForm implements Serializable {

    private static final long serialVersionUID = 1143017330340385847L;

    @XmlElement(name = "uuid")
    private String uuid;

    @XmlElement(name = "processed_by_mirth")
    private boolean processedByMirth;

    @XmlElement(name = "field_worker_ext_id")
    private String fieldWorkerExtId;

    @XmlElement(name = "collection_date_time")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar collectionDateTime;

    @XmlElement(name = "household_ext_id")
    private String householdExtId;

    @XmlElement(name = "individual_ext_id")
    private String individualExtId;

    @XmlElement(name = "individual_first_name")
    private String individualFirstName;

    @XmlElement(name = "individual_last_name")
    private String individualLastName;

    @XmlElement(name = "individual_other_names")
    private String individualOtherNames;

    @XmlElement(name = "individual_age")
    private int individualAge;

    @XmlElement(name = "individual_age_units")
    private String individualAgeUnits;

    @XmlElement(name = "individual_date_of_birth")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar individualDateOfBirth;

    @XmlElement(name = "individual_gender")
    private String individualGender;

    @XmlElement(name = "individual_relationship_to_head_of_household")
    private String individualRelationshipToHeadOfHousehold;

    @XmlElement(name = "individual_phone_number")
    private String individualPhoneNumber;

    @XmlElement(name = "individual_other_phone_number")
    private String individualOtherPhoneNumber;

    @XmlElement(name = "individual_language_preference")
    private String individualLanguagePreference;

    @XmlElement(name = "individual_point_of_contact_name")
    private String individualPointOfContactName;

    @XmlElement(name = "individual_point_of_contact_phone_number")
    private String individualPointOfContactPhoneNumber;

    @XmlElement(name = "individual_dip")
    private int individualDip;

    @XmlElement(name = "individual_member_status")
    private String individualMemberStatus;

    @XmlElement(name = "individual_nationality")
    private String individualNationality;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isProcessedByMirth() {
        return processedByMirth;
    }

    public void setProcessedByMirth(boolean processedByMirth) {
        this.processedByMirth = processedByMirth;
    }

    public String getFieldWorkerExtId() {
        return fieldWorkerExtId;
    }

    public void setFieldWorkerExtId(String fieldWorkerExtId) {
        this.fieldWorkerExtId = fieldWorkerExtId;
    }

    public Calendar getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(Calendar collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    public String getHouseholdExtId() {
        return householdExtId;
    }

    public void setHouseholdExtId(String householdExtId) {
        this.householdExtId = householdExtId;
    }

    public String getIndividualExtId() {
        return individualExtId;
    }

    public void setIndividualExtId(String individualExtId) {
        this.individualExtId = individualExtId;
    }

    public String getIndividualFirstName() {
        return individualFirstName;
    }

    public void setIndividualFirstName(String individualFirstName) {
        this.individualFirstName = individualFirstName;
    }

    public String getIndividualLastName() {
        return individualLastName;
    }

    public void setIndividualLastName(String individualLastName) {
        this.individualLastName = individualLastName;
    }

    public String getIndividualOtherNames() {
        return individualOtherNames;
    }

    public void setIndividualOtherNames(String individualOtherNames) {
        this.individualOtherNames = individualOtherNames;
    }

    public int getIndividualAge() {
        return individualAge;
    }

    public void setIndividualAge(int individualAge) {
        this.individualAge = individualAge;
    }

    public String getIndividualAgeUnits() {
        return individualAgeUnits;
    }

    public void setIndividualAgeUnits(String individualAgeUnits) {
        this.individualAgeUnits = individualAgeUnits;
    }

    public Calendar getIndividualDateOfBirth() {
        return individualDateOfBirth;
    }

    public void setIndividualDateOfBirth(Calendar individualDateOfBirth) {
        this.individualDateOfBirth = individualDateOfBirth;
    }

    public String getIndividualGender() {
        return individualGender;
    }

    public void setIndividualGender(String individualGender) {
        this.individualGender = individualGender;
    }

    public String getIndividualRelationshipToHeadOfHousehold() {
        return individualRelationshipToHeadOfHousehold;
    }

    public void setIndividualRelationshipToHeadOfHousehold(
            String individualRelationshipToHeadOfHousehold) {
        this.individualRelationshipToHeadOfHousehold = individualRelationshipToHeadOfHousehold;
    }

    public String getIndividualPhoneNumber() {
        return individualPhoneNumber;
    }

    public void setIndividualPhoneNumber(String individualPhoneNumber) {
        this.individualPhoneNumber = individualPhoneNumber;
    }

    public String getIndividualOtherPhoneNumber() {
        return individualOtherPhoneNumber;
    }

    public void setIndividualOtherPhoneNumber(String individualOtherPhoneNumber) {
        this.individualOtherPhoneNumber = individualOtherPhoneNumber;
    }

    public String getIndividualLanguagePreference() {
        return individualLanguagePreference;
    }

    public void setIndividualLanguagePreference(String individualLanguagePreference) {
        this.individualLanguagePreference = individualLanguagePreference;
    }

    public String getIndividualPointOfContactName() {
        return individualPointOfContactName;
    }

    public void setIndividualPointOfContactName(String individualPointOfContactName) {
        this.individualPointOfContactName = individualPointOfContactName;
    }

    public String getIndividualPointOfContactPhoneNumber() {
        return individualPointOfContactPhoneNumber;
    }

    public void setIndividualPointOfContactPhoneNumber(String individualPointOfContactPhoneNumber) {
        this.individualPointOfContactPhoneNumber = individualPointOfContactPhoneNumber;
    }

    public int getIndividualDip() {
        return individualDip;
    }

    public void setIndividualDip(int individualDip) {
        this.individualDip = individualDip;
    }

    public String getIndividualMemberStatus() {
        return individualMemberStatus;
    }

    public void setIndividualMemberStatus(String individualMemberStatus) {
        this.individualMemberStatus = individualMemberStatus;
    }

    public String getIndividualNationality() {
        return individualNationality;
    }

    public void setIndividualNationality(String individualNationality) {
        this.individualNationality = individualNationality;
    }
}
