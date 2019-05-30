package com.github.cimsbioko.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.hibernate.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @CreationTimestamp
    private Timestamp created;

    @NotNull
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
    private User creator;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getCreated() {
        return created;
    }

    public AccessToken getToken() {
        return token;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
}
