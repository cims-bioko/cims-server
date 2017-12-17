package com.github.cimsbioko.server.domain.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.github.cimsbioko.server.domain.constraint.Searchable;

@Entity
@Table(name = "role")
public class Role extends AuditableEntity implements Serializable {

    static final long serialVersionUID = 21L;

    @Searchable
    String name;

    String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_privileges", joinColumns = {
            @JoinColumn(name = "role")}, inverseJoinColumns = @JoinColumn(name = "privilege"))
    Set<Privilege> privileges = new HashSet<>();

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
