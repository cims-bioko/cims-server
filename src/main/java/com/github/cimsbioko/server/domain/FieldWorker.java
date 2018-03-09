package com.github.cimsbioko.server.domain;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "fieldworker")
@Indexed
public class FieldWorker extends AuditableEntity implements Serializable {

    private static final long serialVersionUID = -7550088299362704483L;

    @NotNull
    @Searchable("fieldWorkerID")
    @Field
    private String extId;

    @Searchable("fieldWorkerFirstName")
    @Column(name = "first_name")
    @Field
    private String firstName;

    @Searchable("fieldWorkerLastName")
    @Column(name = "last_name")
    @Field
    private String lastName;

    @Transient
    private String password;

    @Transient
    private String confirmPassword;

    @NotNull
    @Column(name = "password_hash")
    private String passwordHash;

    private int idPrefix;


    public int getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(int idPrefix) {
        this.idPrefix = idPrefix;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof FieldWorker)) {
            return false;
        }

        final String otherUuid = ((FieldWorker) other).getUuid();
        return null != uuid && null != otherUuid && uuid.equals(otherUuid);
    }
}
