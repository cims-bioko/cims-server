package com.github.cimsbioko.server.domain;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Calendar;


@Entity
@Immutable
@Table(name = "v_backup")
public class Backup {

    @Id
    @Column(name = "schema_name")
    @NotNull
    @Pattern(regexp = "[\\p{Alnum}_]+")
    private String name;

    @NotEmpty
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

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

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }
}
