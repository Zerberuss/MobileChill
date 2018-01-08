package net.sytes.schneider.mobilechill.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Timo Hasenbichler on 16.12.2017.
 */
@Entity(foreignKeys = @ForeignKey(entity = LocationEntity.class,
        parentColumns = "uid",
        childColumns = "location_id"))
public class WirelessNetwork {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "password")
    private String pwassword;

    @ColumnInfo(name = "establishedConnections")
    private int establishedConnections;

    @ColumnInfo(name = "location_id")
    private int location_id;

    public int getLocation_id() {
        return location_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

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

    public String getPwassword() {
        return pwassword;
    }

    public void setPwassword(String pwassword) {
        this.pwassword = pwassword;
    }

    public int getEstablishedConnections() {
        return establishedConnections;
    }

    public void setEstablishedConnections(int establishedConnections) {
        this.establishedConnections = establishedConnections;
    }
}
