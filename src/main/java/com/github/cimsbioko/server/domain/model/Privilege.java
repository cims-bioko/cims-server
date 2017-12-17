package com.github.cimsbioko.server.domain.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "privilege")
public class Privilege implements Serializable {

    private static final long serialVersionUID = -5969044695942713833L;

    public static final String CREATE_ENTITY = "CREATE_ENTITY";
    public static final String EDIT_ENTITY = "EDIT_ENTITY";
    public static final String DELETE_ENTITY = "DELETE_ENTITY";
    public static final String VIEW_ENTITY = "VIEW_ENTITY";


    public Privilege() {
    }

    public Privilege(String privilege) {
        this.privilege = privilege;
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.github.cimsbioko.server.domain.util.UUIDGenerator")
    @Column(length = 32)
    private String uuid;

    private String privilege;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Privilege)) {
            return false;
        }

        return privilege.equals(((Privilege) obj).privilege);
    }

    @Override
    public int hashCode() {
        if (privilege == null) {
            return super.hashCode();
        }

        return privilege.hashCode();
    }

    @Override
    public String toString() {
        return privilege;
    }
}
