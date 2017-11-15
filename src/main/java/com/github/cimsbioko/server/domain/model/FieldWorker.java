package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.annotations.Description;
import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;
import com.github.cimsbioko.server.domain.constraint.Searchable;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Description(description = "A Field Worker represents one who collects the data within " +
        "the study area. They can be identified by a uniquely generated " +
        "identifier which the system uses internally. Only the first and last names " +
        "are recorded.")
@Entity
@Table(name = "fieldworker")
public class FieldWorker extends AuditableEntity implements Serializable {


    private static final long serialVersionUID = -7550088299362704483L;

    @NotNull
    @CheckFieldNotBlank
    @Searchable
    @Description(description = "External Id of the field worker. This id is used internally.")
    String extId;

    @CheckFieldNotBlank
    @Searchable
    @Description(description = "First name of the field worker.")
    String firstName;

    @CheckFieldNotBlank
    @Searchable
    @Description(description = "Last name of the field worker.")
    String lastName;

    @Description(description = "Password entered for a new field worker.")
    @Transient
    String password;

    @Description(description = "Password re-entered for a new field worker.")
    @Transient
    String confirmPassword;

    @NotNull
    @CheckFieldNotBlank
    @Description(description = "Hashed version of a field worker's password.")
    String passwordHash;

    @Description(description = "The ID prefix used in individual extId generation.")
    int idPrefix;


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
