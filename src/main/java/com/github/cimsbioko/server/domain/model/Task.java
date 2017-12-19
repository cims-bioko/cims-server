package com.github.cimsbioko.server.domain.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Calendar;


@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.idgen.UUIDGenerator")
    @Column(length = 32)
    String uuid;
    private String name;
    private long itemCount;
    private String descriptor;
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar started;
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar finished;


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

    public void setItemCount(long totalCount) {
        this.itemCount = totalCount;
    }

    public long getItemCount() {
        return itemCount;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public Calendar getStarted() {
        return started;
    }

    public void setStarted(Calendar started) {
        this.started = started;
    }

    public Calendar getFinished() {
        return finished;
    }

    public void setFinished(Calendar finished) {
        this.finished = finished;
    }

}
