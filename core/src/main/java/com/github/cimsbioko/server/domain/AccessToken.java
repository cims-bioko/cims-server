package com.github.cimsbioko.server.domain;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "access_token")
public class AccessToken {

    @Id
    private String value;

    @CreationTimestamp
    private Timestamp created;

    private Timestamp expires;

    public AccessToken() {
    }

    public AccessToken(Timestamp expires) {
        this.expires = expires;
    }

    public String getValue() {
        return value;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getExpires() {
        return expires;
    }
}
