package net.sytes.schneider.mobilechill.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

/**
 * Created by Timo Hasenbichler on 16.12.2017.
 */
@Entity
public class LocationEntity {


    public LocationEntity() {
    }


    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name="displayName")
    private String displayName;

    //geolocation from google api
    @ColumnInfo(name = "latidude")
    private Double latidude;
    @ColumnInfo(name = "longitude")
    private Double longitude;

    //celltowerinformations

    //created
    @ColumnInfo(name = "created")
    private Date created;

    //modified
    @ColumnInfo(name = "modified")
    private Date modified;


    //signalstrength
    @ColumnInfo(name = "signalStrength")
    private int signalStrengthEnum;


    //forceDisable
    @ColumnInfo(name = "forceDisable")
    private boolean forceDisable = false;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public int getSignalStrengthEnum() {
        return signalStrengthEnum;
    }

    public void setSignalStrengthEnum(int signalStrengthEnum) {
        this.signalStrengthEnum = signalStrengthEnum;
    }

    public boolean isForceDisable() {
        return forceDisable;
    }

    public void setForceDisable(boolean forceDisable) {
        this.forceDisable = forceDisable;
    }

    public Date getCreated() {
        return created;
    }

    public Date getModified() {
        return modified;
    }

    public Double getLatidude() {
        return latidude;
    }

    public void setLatidude(Double latidude) {
        this.latidude = latidude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}













