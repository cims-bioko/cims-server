package com.github.cimsbioko.server.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

@Entity
@Table(name = "individual")
@Indexed
public class Individual implements Serializable {

    public final static long serialVersionUID = 9058114832143454609L;

    @Id
    @Column(length = 32)
    private String uuid;

    private Calendar deleted;

    @CreationTimestamp
    private Calendar created;

    @NotNull
    @Size(min = 1)
    @Column(name = "extid")
    @Field
    private String extId;

    @NotNull
    @Column(name = "first_name")
    @Field
    private String firstName;

    @Column(name = "middle_name")
    @Field
    private String middleName;

    @Column(name = "last_name")
    @Field
    private String lastName;

    private String gender;

    @Past(message = "Date of birth must a date in the past")
    @Temporal(TemporalType.DATE)
    private Calendar dob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home")
    private Location home;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector")
    protected FieldWorker collector;

    @Type(type = "json")
    private JSONObject attrs;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setDeleted(Calendar deleted) {
        this.deleted = deleted;
    }

    public Calendar getDeleted() {
        return deleted;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

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

    public Calendar getDob() {
        return dob;
    }

    public void setDob(Calendar date) {
        dob = date;
    }

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        this.home = home;
    }

    public FieldWorker getCollector() {
        return collector;
    }

    public void setCollector(FieldWorker collector) {
        this.collector = collector;
    }

    public JSONObject getAttrs() {
        return attrs;
    }

    public void setAttrs(JSONObject attrs) {
        this.attrs = attrs;
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
        return null != uuid && uuid.equals(otherUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
