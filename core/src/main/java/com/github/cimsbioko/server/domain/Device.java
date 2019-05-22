package com.github.cimsbioko.server.domain;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "device")
public class Device {

    @Id
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
    private String token;

}
