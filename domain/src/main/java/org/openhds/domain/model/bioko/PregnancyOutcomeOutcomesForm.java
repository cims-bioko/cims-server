package org.openhds.domain.model.bioko;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by motech on 4/9/15.
 */
@XmlRootElement(name = "pregnancyOutcomeOutcomesForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class PregnancyOutcomeOutcomesForm implements Serializable{

    @XmlElement(name = "pregnancy_outcome_uuid")
    private String pregnancyOutcomeUuid;

    @XmlElement(name = "collection_date_time")
    @XmlJavaTypeAdapter(org.openhds.domain.util.CalendarAdapter.class)
    private Calendar collectionDateTime;

    @XmlElement(name = "processed_by_mirth")
    private boolean processedByMirth;

    @XmlElement(name = "socialgroup_uuid")
    private String socialGroupUuid;

    @XmlElement(name = "outcome_type")
    private String outcomeType;

    @XmlElement(name = "child_uuid")
    private String childUuid;

    @XmlElement(name = "child_first_name")
    private String childFirstName;

    @XmlElement(name = "child_middle_name")
    private String childMiddleName;

    @XmlElement(name = "child_last_name")
    private String childLastName;

    @XmlElement(name = "child_gender")
    private String childGender;

    @XmlElement(name = "child_relationship_to_group_head")
    private String childRelationshipToGroupHead;

    @XmlElement(name = "child_nationality")
    private String childNationality;

    public String getSocialGroupUuid() {
        return socialGroupUuid;
    }

    public void setSocialGroupUuid(String socialGroupUuid) {
        this.socialGroupUuid = socialGroupUuid;
    }

    public String getPregnancyOutcomeUuid() {
        return pregnancyOutcomeUuid;
    }

    public void setPregnancyOutcomeUuid(String pregnancyOutcomeUuid) {
        this.pregnancyOutcomeUuid = pregnancyOutcomeUuid;
    }

    public Calendar getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(Calendar collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    public boolean isProcessedByMirth() {
        return processedByMirth;
    }

    public void setProcessedByMirth(boolean processedByMirth) {
        this.processedByMirth = processedByMirth;
    }

    public String getOutcomeType() {
        return outcomeType;
    }

    public void setOutcomeType(String outcomeType) {
        this.outcomeType = outcomeType;
    }

    public String getChildUuid() {
        return childUuid;
    }

    public void setChildUuid(String childUuid) {
        this.childUuid = childUuid;
    }

    public String getChildFirstName() {
        return childFirstName;
    }

    public void setChildFirstName(String childFirstName) {
        this.childFirstName = childFirstName;
    }

    public String getChildMiddleName() {
        return childMiddleName;
    }

    public void setChildMiddleName(String childMiddleName) {
        this.childMiddleName = childMiddleName;
    }

    public String getChildLastName() {
        return childLastName;
    }

    public void setChildLastName(String childLastName) {
        this.childLastName = childLastName;
    }

    public String getChildGender() {
        return childGender;
    }

    public void setChildGender(String childGender) {
        this.childGender = childGender;
    }

    public String getChildRelationshipToGroupHead() {
        return childRelationshipToGroupHead;
    }

    public void setChildRelationshipToGroupHead(String childRelationshipToGroupHead) {
        this.childRelationshipToGroupHead = childRelationshipToGroupHead;
    }

    public String getChildNationality() {
        return childNationality;
    }

    public void setChildNationality(String childNationality) {
        this.childNationality = childNationality;
    }
}
