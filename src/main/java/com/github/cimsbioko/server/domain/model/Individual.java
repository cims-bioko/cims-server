package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.constraint.*;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

@Description(description = "An Individual represents one who is a part of the study. Each Individual is identified by a uniquely generated external identifier which the system uses internally. Information about the Individual such as name, gender, date of birth, and parents are stored here. An Individual may be associated with many Residencies, Relationships, and Memberships.")
@Entity
@Table(name = "individual")
@XmlRootElement(name = "individual")
public class Individual extends AuditableCollectedEntity implements Serializable {

    public final static long serialVersionUID = 9058114832143454609L;

    @NotNull
    @Size(min = 1)
    @Searchable
    @Description(description = "External Id of the individual. This id is used internally.")
    private String extId;

    @NotNull
    @Searchable
    @Description(description = "First name of the individual.")
    private String firstName;

    @Searchable
    @Description(description = "Middle name of the individual.")
    private String middleName;

    @CheckFieldNotBlank
    @Searchable
    @Description(description = "Last name of the individual.")
    private String lastName;

    @ExtensionStringConstraint(constraint = "genderConstraint", message = "Invalid Value for gender", allowNull = true)
    @Description(description = "The gender of the individual.")
    private String gender;

    @Past(message = "Date of birth must a date in the past")
    @Temporal(TemporalType.DATE)
    @Description(description = "Birth date of the individual.")
    private Calendar dob;

    @OneToMany(mappedBy = "individual", cascade = {CascadeType.ALL})
    @Description(description = "The set of all residencies that the individual may have.")
    private Set<Residency> allResidencies = new HashSet<>();

    @OneToMany(mappedBy = "individual", cascade = {CascadeType.ALL})
    @Description(description = "The set of all memberships the individual is participating in.")
    private Set<Membership> allMemberships = new HashSet<>();

    //Project-specific fields
    @Column
    private String phoneNumber;
    @Column
    private String otherPhoneNumber;
    @Column
    private String languagePreference;
    @Column
    private String pointOfContactName;
    @Column
    private String pointOfContactPhoneNumber;
    @Column
    private int dip;
    @Column
    private String nationality;

    public String getExtId() {
        return extId;
    }

    public void setExtId(String id) {
        extId = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        firstName = name;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String name) {
        middleName = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String name) {
        lastName = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String sex) {
        gender = sex;
    }

    @XmlJavaTypeAdapter(CalendarAdapter.class)
    public Calendar getDob() {
        return dob;
    }

    public void setDob(Calendar date) {
        dob = date;
    }

    @XmlElementWrapper(name = "residencies")
    @XmlElement(name = "residency")
    public Set<Residency> getAllResidencies() {
        return allResidencies;
    }

    public void setAllResidencies(Set<Residency> list) {
        allResidencies = list;
    }

    @XmlElementWrapper(name = "memberships")
    @XmlElement(name = "membership")
    public Set<Membership> getAllMemberships() {
        return allMemberships;
    }

    public void setAllMemberships(Set<Membership> list) {
        allMemberships = list;
    }

    public Residency getCurrentResidency() {
        if (allResidencies.size() == 0) {
            return null;
        }

        // sort by "earliest" and pick off the "least early" ie "latest"
        PriorityQueue<Residency> residencyHeap = new PriorityQueue<>(
                allResidencies.size(), Residency.earliestByInsertDate());
        residencyHeap.addAll(allResidencies);

        return residencyHeap.peek();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setOtherPhoneNumber(String otherPhoneNumber) {
        this.otherPhoneNumber = otherPhoneNumber;
    }

    public String getOtherPhoneNumber() {
        return otherPhoneNumber;
    }

    public void setLanguagePreference(String languagePreference) {
        this.languagePreference = languagePreference;
    }

    public String getLanguagePreference() {
        return languagePreference;
    }

    public void setPointOfContactName(String pointOfContactName) {
        this.pointOfContactName = pointOfContactName;
    }

    public String getPointOfContactName() {
        return pointOfContactName;
    }

    public void setPointOfContactPhoneNumber(String pointOfContactPhoneNumber) {
        this.pointOfContactPhoneNumber = pointOfContactPhoneNumber;
    }

    public String getPointOfContactPhoneNumber() {
        return pointOfContactPhoneNumber;
    }

    public void setDip(int dip) {
        this.dip = dip;
    }

    public int getDip() {
        return dip;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Individual)) {
            return false;
        }

        final String otherUuid = ((Individual) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }
}
