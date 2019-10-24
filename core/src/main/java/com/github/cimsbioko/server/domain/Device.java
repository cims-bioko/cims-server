package com.github.cimsbioko.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "device")
@Indexed
public class Device {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.hibernate.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    @NotNull
    @Field
    private String name;

    @NotNull
    @Field
    private String description;

    @CreationTimestamp
    private Timestamp created;

    private Timestamp deleted;

    @NotNull
    @JsonIgnore
    private String secret;

    @JsonIgnore
    private Timestamp secretExpires;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token")
    @JsonIgnore
    private AccessToken token;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "device_roles", joinColumns = {
            @JoinColumn(name = "device")}, inverseJoinColumns = @JoinColumn(name = "`role`"))
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    @JsonIgnore
    private User creator;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    @ManyToOne
    @JoinColumn(name = "campaign")
    private Campaign campaign;

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getDeleted() {
        return deleted;
    }

    public void setDeleted(Timestamp deleted) {
        this.deleted = deleted;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Timestamp getSecretExpires() {
        return secretExpires;
    }

    public void setSecretExpires(Timestamp secretExpires) {
        this.secretExpires = secretExpires;
    }

    public AccessToken getToken() {
        return token;
    }

    public void setToken(AccessToken token) {
        this.token = token;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
}
