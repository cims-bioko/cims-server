package com.github.cimsbioko.server.domain.model;

import com.github.cimsbioko.server.domain.constraint.Searchable;
import org.hibernate.annotations.GenericGenerator;
import com.github.cimsbioko.server.domain.annotations.Description;
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

@Description(description = "A User in the system. Users contain a group of Roles which " +
        "define the actions they can take within OpenHDS. It contains descriptive " +
        "information about the User such as first and last name, description, and " +
        "the chosen username and password.")
@Entity
@Table(name = "users")
public class User implements Serializable, UuidIdentifiable {

    static final long serialVersionUID = 23L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.domain.util.UUIDGenerator")
    @Column(length = 32)
    String uuid;

    @CheckFieldNotBlank
    @Searchable
    @Description(description = "User's first name")
    @Column(name = "first_name")
    String firstName;

    @CheckFieldNotBlank
    @Searchable
    @Description(description = "User's last name")
    @Column(name = "last_name")
    String lastName;

    @Description(description = "User's full name")
    @Column(name = "full_name")
    String fullName;

    @Description(description = "Description of the user.")
    String description;

    @CheckFieldNotBlank
    @Description(description = "The name used for logging into the system.")
    String username;

    @CheckFieldNotBlank
    @Description(description = "Password associated with the username.")
    String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = {
            @JoinColumn(name = "user")}, inverseJoinColumns = @JoinColumn(name = "role"))
    @Description(description = "Set of roles applied to the user.")
    Set<Role> roles = new HashSet<>();

    @Description(description = "Indicator for signaling some data to be deleted.")
    boolean deleted = false;

    // this is used for seamless integration with special study
    String sessionId;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
