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

    private String version;

    @CreationTimestamp
    private Timestamp created;

    private Timestamp deleted;

    private Timestamp start;

    private Timestamp end;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "campaign_members",
            joinColumns = @JoinColumn(name = "campaign"),
            inverseJoinColumns = @JoinColumn(name = "`user`"))
    @JsonIgnore
    private Set<User> members = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "campaign_forms",
            joinColumns = @JoinColumn(name = "campaign"),
            inverseJoinColumns = {@JoinColumn(name = "form_id"), @JoinColumn(name = "form_version")})
    @JsonIgnore
    private Set<Form> forms = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "campaign_devices",
            joinColumns = @JoinColumn(name = "campaign"),
            inverseJoinColumns = @JoinColumn(name = "device"))
    @JsonIgnore
    private Set<Device> devices = new HashSet<>();

}
