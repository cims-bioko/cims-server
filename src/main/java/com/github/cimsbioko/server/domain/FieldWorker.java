package com.github.cimsbioko.server.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import static org.mindrot.jbcrypt.BCrypt.gensalt;
import static org.mindrot.jbcrypt.BCrypt.hashpw;

@Entity
@Table(name = "fieldworker")
@Indexed
public class FieldWorker implements Serializable {

    private static final long serialVersionUID = -7550088299362704483L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.hibernate.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    private Calendar deleted;

    @CreationTimestamp
    private Calendar created;

    @NotNull
    @Pattern(regexp = "FW[A-Z][A-Z][1-9][0-9]*")
    @Column(name = "extid")
    @Field
    private String extId;

    @Column(name = "first_name")
    @Field
    @Size(min = 1, max = 255)
    private String firstName;

    @Column(name = "last_name")
    @Field
    @Size(min = 1, max = 255)
    private String lastName;

    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash")
    private String passwordHash;

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

    @Transient
    public void setPassword(String raw) {
        setPasswordHash(hashpw(raw, gensalt(12)));
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
        return null != uuid && uuid.equals(otherUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
