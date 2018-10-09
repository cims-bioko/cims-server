
package com.github.cimsbioko.server.domain;

import com.vividsolutions.jts.geom.Point;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "location")
@Indexed
public class Location implements Serializable {

    public final static long serialVersionUID = 169551578162260199L;

    @Id
    @Column(length = 32)
    private String uuid;

    private Calendar deleted;

    @CreationTimestamp
    private Calendar created;

    @NotNull
    @Size(min = 1)
    @Column(name = "extid")
    @Field
    private String extId;

    @Field
    private String name;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "hierarchy")
    private LocationHierarchy hierarchy = new LocationHierarchy();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = FieldWorker.class)
    @JoinColumn(name = "collector")
    private FieldWorker collector;

    @Column(name = "global_pos")
    private Point globalPos;

    @Field
    private String description;

    @OneToMany(mappedBy = "home")
    private List<Individual> residents = new ArrayList<>();

    @Type(type = "json")
    private JSONObject attrs;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setDeleted(Calendar deleted) {
        this.deleted = deleted;
    }

    public Calendar getDeleted() {
        return deleted;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String id) {
        extId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationHierarchy getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(LocationHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public FieldWorker getCollector() {
        return collector;
    }

    public void setCollector(FieldWorker collector) {
        this.collector = collector;
    }

    public Point getGlobalPos() {
        return globalPos;
    }

    public void setGlobalPos(Point globalPos) {
        this.globalPos = globalPos;
    }

    public List<Individual> getResidents() {
        return residents;
    }

    public void addResident(Individual resident) {
        residents.add(resident);
        resident.setHome(this);
    }

    public JSONObject getAttrs() {
        return attrs;
    }

    public void setAttrs(JSONObject attrs) {
        this.attrs = attrs;
    }

    public JSONObject getAttrsForUpdate() {
        if (attrs == null) {
            attrs = new JSONObject();
        }
        return attrs;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Location)) {
            return false;
        }

        final String otherUuid = ((Location) other).getUuid();
        return null != uuid && uuid.equals(otherUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
