package net.sytes.schneider.mobilechill.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import net.sytes.schneider.mobilechill.database.Converter.Converters;

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

    //geolocation from google api


    //celltowerinformations

    //created
    @ColumnInfo(name = "created")
    private long created;



    //modified
    @ColumnInfo(name = "modified")
    private long modified;


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


    public void setCreated(long created) {
        this.created = created;
    }

    public void setModified(long modified) {
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

    public long getCreated() {
        return created;
    }

    public long getModified() {
        return modified;
    }

}













