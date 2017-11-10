package com.github.cimsbioko.server.domain.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Calendar;


@Entity
@Table(name = "asynctask")
public class AsyncTask {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.domain.util.UUIDGenerator")
    @Column(length = 32)
    String uuid;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar taskStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar taskEndDate;

    private String taskName;
    private long totalCount;
    private String md5Hash;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Calendar getTaskStartDate() {
        return taskStartDate;
    }

    public void setTaskStartDate(Calendar taskStartDate) {
        this.taskStartDate = taskStartDate;
    }

    public Calendar getTaskEndDate() {
        return taskEndDate;
    }

    public void setTaskEndDate(Calendar taskEndDate) {
        this.taskEndDate = taskEndDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTotalItems(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }
}
