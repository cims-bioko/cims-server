package org.openhds.domain.model;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.openhds.domain.annotations.Description;
import org.openhds.domain.constraint.CheckFieldNotBlank;

@Description(description = "An ExtBuilding represents extension data associated with a "
        + "building in the location hierarchy.  Each ExtBuilding is associated with a single "
        + "LocationHierarchy entry and holds extra data for it, like building GPS coordinates "
        + "and the date when the building information was collected for OpenHDS.")
@Entity
@Table(name = "extbuilding")
@XmlRootElement
public class ExtBuilding extends AuditableCollectedEntity implements Serializable {

    private static final long serialVersionUID = 5776461914132139824L;

    @Description(description = "LocationHierarchy entry described by this ExtBuilding.")
    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    private LocationHierarchy locationHierarchy = new LocationHierarchy();

    @Description(description = "External Id of the locationHierarchy described by this ExtBuilding.")
    @CheckFieldNotBlank
    private String locationHierarchyExtId;

    @Description(description = "The longitude for the building")
    @CheckFieldNotBlank
    private String longitude;

    @Description(description = "The latitude for the building")
    @CheckFieldNotBlank
    private String latitude;

    @Description(description = "How accurate are the longitude/latitude readings for the building")
    private String accuracy;

    @Description(description = "The altitude for the building")
    private String altitude;

    public LocationHierarchy getLocationHierarchy() {
        return locationHierarchy;
    }

    public void setLocationHierarchy(LocationHierarchy locationHierarchy) {
        this.locationHierarchy = locationHierarchy;
    }
    
    public String getLocationHierarchyExtId() {
        return locationHierarchy.getExtId();
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }
}
