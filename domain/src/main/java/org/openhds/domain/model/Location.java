
package org.openhds.domain.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.openhds.domain.annotations.Description;
import org.openhds.domain.constraint.CheckFieldNotBlank;
import org.openhds.domain.constraint.ExtensionStringConstraint;
import org.openhds.domain.constraint.Searchable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * Generated by JCodeModel
 *
 */
@Description(description = "All distinct Locations within the area of study are represented here. A Location is identified by a uniquely generated identifier that the system uses internally. Each Location has a name associated with it and resides at a particular level within the Location Hierarchy.")
@Entity
@Table(name = "location")
@XmlRootElement
@JsonInclude(Include.NON_NULL)
public class Location
        extends AuditableCollectedEntity
        implements Serializable
{

    public final static long serialVersionUID = 169551578162260199L;

    @NotNull
    @CheckFieldNotBlank
    @Searchable
    @Description(description = "External Id of the location. This id is used internally.")
    private String extId;

    @CheckFieldNotBlank
    @Searchable
    @Description(description = "Name of the location.")
    private String locationName;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    private LocationHierarchy locationLevel = new LocationHierarchy();

    @ExtensionStringConstraint(constraint = "locationTypeConstraint", message = "Invalid Value for location type", allowNull = true)
    @Description(description = "The type of Location.")
    private String locationType;

    @Description(description = "The longitude for the Location")
    private String longitude;

    @Description(description = "The latitude for the Location")
    private String latitude;

    @Description(description = "How accurate are the longitude/latitude readings for the Location")
    private String accuracy;

    @Description(description = "The altitude for the Location")
    private String altitude;

    @OneToMany(targetEntity = org.openhds.domain.model.Residency.class)
    @JoinColumn(name = "location_uuid")
    private List<Residency> residencies;

    // Extensions for bioko island project
    @Description(description = "The name of the map sector that contains this location")
    private String sectorName;

    @Description(description = "The name of the locality (aka AREA) that contains this location")
    private String localityName;

    @Description(description = "The name of the community that contains this location")
    private String communityName;
    
    @Description(description = "The name of the map area - disregarding Locality - that contains this location")
    private String mapAreaName;

    public String getExtId() {
        return extId;
    }

    public void setExtId(String id) {
        extId = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String name) {
        locationName = name;
    }

    public LocationHierarchy getLocationLevel() {
        return locationLevel;
    }

    public void setLocationLevel(LocationHierarchy level) {
        locationLevel = level;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String type) {
        locationType = type;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longi) {
        longitude = longi;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String lat) {
        latitude = lat;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String acc) {
        accuracy = acc;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String alt) {
        altitude = alt;
    }

    public List<Residency> getResidencies() {
        return residencies;
    }

    public void setResidencies(List<Residency> list) {
        residencies = list;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }
    
    public String getMapAreaName() {
    	return mapAreaName;
    }
    
    public void setMapAreaName(String mapAreaName) {
    	this.mapAreaName = mapAreaName;
    }

    public static Location makeStub(String extId) {

        Location stub = new Location();
        stub.setExtId(extId);
        return stub;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Location)) {
            return false;
        }

        final Location otherLocation = (Location) other;

        if (!extId.equals(otherLocation.getExtId())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return extId.hashCode();
    }

}
