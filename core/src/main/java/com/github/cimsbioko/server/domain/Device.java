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

    private Timestamp deleted;

    @NotNull
    @Column(name = "token_hash")
    private String tokenHash;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "device_roles", joinColumns = {
            @JoinColumn(name = "device")}, inverseJoinColumns = @JoinColumn(name = "`role`"))
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    private User creator;

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

    public Timestamp getDeleted() {
        return deleted;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
