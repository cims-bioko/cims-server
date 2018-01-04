package com.github.cimsbioko.server.domain;

import com.github.cimsbioko.server.util.CalendarAdapter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "individual")
@XmlRootElement(name = "individual")
public class Individual extends AuditableCollectedEntity implements Serializable {

    public final static long serialVersionUID = 9058114832143454609L;

    @NotNull
    @Size(min = 1)
    private String extId;

    @NotNull
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    private String gender;

    @Past(message = "Date of birth must a date in the past")
    @Temporal(TemporalType.DATE)
    private Calendar dob;

    //Project-specific fields
    private String phone1;

    private String phone2;

    private String language;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_phone")
    private String contactPhone;

    private int dip;

    private String nationality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home")
    private Location home;

    @Column(name = "home_role")
    private String homeRole;

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

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactPhone() {
        return contactPhone;
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

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        this.home = home;
    }

    public String getHomeRole() {
        return homeRole;
    }

    public void setHomeRole(String homeRole) {
        this.homeRole = homeRole;
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
