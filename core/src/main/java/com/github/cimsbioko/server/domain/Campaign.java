package com.github.cimsbioko.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "campaign")
public class Campaign {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.hibernate.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    private String name;

    private String description;

    @CreationTimestamp
    private Timestamp created;

    private Timestamp disabled;

    private Timestamp deleted;

    private Timestamp start;

    private Timestamp end;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "campaign_users",
            joinColumns = @JoinColumn(name = "campaign"),
            inverseJoinColumns = @JoinColumn(name = "`user`"))
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "campaign_forms",
            joinColumns = @JoinColumn(name = "campaign"),
            inverseJoinColumns = {@JoinColumn(name = "form_id"), @JoinColumn(name = "form_version")})
    @JsonIgnore
    private Set<Form> forms = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "campaign")
    @JsonIgnore
    private Set<Device> devices = new HashSet<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getDisabled() {
        return disabled;
    }

    public void setDisabled(Timestamp disabled) {
        this.disabled = disabled;
    }

    public Timestamp getDeleted() {
        return deleted;
    }

    public void setDeleted(Timestamp deleted) {
        this.deleted = deleted;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Form> getForms() {
        return forms;
    }

    public void setForms(Set<Form> forms) {
        this.forms = forms;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }
}
