package org.openhds.domain.model.bioko;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.openhds.domain.annotations.Description;

/**
 * Written by Ben Heasly to model incoming forms for the bioko island project.
 * 
 */
@Description(description = "Model data from the Individual xform for the Bioko island project.  Contains Individual, Relationship, and Membership data.")
@XmlRootElement(name = "individualForm")
public class IndividualForm implements Serializable {

	private static final long serialVersionUID = 1143017330340385847L;

	private boolean processedByMirth;
	private String fieldWorkerExtId;
	private Date collectionDateTime;
	private String householdExtId;
	private String individualExtId;
	private String individualFirstName;
	private String individualLastName;
	private String individualOtherNames;
	private int individualAge;
	private String individualAgeUnits;
	private Date individualDateOfBirth;
	private String individualGender;
	private String individualRelationshipToHeadOfHousehold;
	private String individualPhoneNumber;
	private String individualOtherPhoneNumber;
	private String individualLanguagePreference;
	private String individualPointOfContactName;
	private String individualPointOfContactPhoneNumber;
	private int individualDip;
	private String individualMemberStatus;

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

	public Date getCollectionDateTime() {
		return collectionDateTime;
	}

	public void setCollectionDateTime(Date collectionDateTime) {
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

	public Date getIndividualDateOfBirth() {
		return individualDateOfBirth;
	}

	public void setIndividualDateOfBirth(Date individualDateOfBirth) {
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

	public void setIndividualLanguagePreference(
			String individualLanguagePreference) {
		this.individualLanguagePreference = individualLanguagePreference;
	}

	public String getIndividualPointOfContactName() {
		return individualPointOfContactName;
	}

	public void setIndividualPointOfContactName(
			String individualPointOfContactName) {
		this.individualPointOfContactName = individualPointOfContactName;
	}

	public String getIndividualPointOfContactPhoneNumber() {
		return individualPointOfContactPhoneNumber;
	}

	public void setIndividualPointOfContactPhoneNumber(
			String individualPointOfContactPhoneNumber) {
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
}
