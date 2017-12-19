package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.constraint.Searchable;
import org.hibernate.annotations.GenericGenerator;
import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements Serializable, UuidIdentifiable {

    static final long serialVersionUID = 23L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.idgen.UUIDGenerator")
    @Column(length = 32)
    String uuid;

    @CheckFieldNotBlank
    @Searchable
    @Column(name = "first_name")
    String firstName;

    @CheckFieldNotBlank
    @Searchable
    @Column(name = "last_name")
    String lastName;

    @Column(name = "full_name")
    String fullName;

    String description;

    @CheckFieldNotBlank
    String username;

    @CheckFieldNotBlank
    String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = {
            @JoinColumn(name = "user")}, inverseJoinColumns = @JoinColumn(name = "role"))
    Set<Role> roles = new HashSet<>();

    boolean deleted = false;

    // this is used for seamless integration with special study
    @Column(name = "last_login")
    long lastLogin;

    public User() {
    }

    public User(String username, String password) {
        setUsername(username);
        setPassword(password);
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
