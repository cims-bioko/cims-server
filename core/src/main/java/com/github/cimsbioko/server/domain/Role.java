package com.github.cimsbioko.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")
@Indexed
public class Role implements Serializable {

    static final long serialVersionUID = 21L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.hibernate.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    private Calendar deleted;

    @CreationTimestamp
    private Calendar created;

    @Field
    private String name;

    @Field
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_privileges", joinColumns = {
            @JoinColumn(name = "role")}, inverseJoinColumns = @JoinColumn(name = "privilege"))
    @JsonIgnore
    private Set<Privilege> privileges = new HashSet<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Role other = (Role) obj;
        return !((this.uuid == null) ? (other.uuid != null) : !this.uuid.equals(other.uuid));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }
}
