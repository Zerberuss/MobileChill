package net.sytes.schneider.mobilechill.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Timo Hasenbichler on 16.12.2017.
 */

@Dao
public interface LocationsDao {


    @Query("Select * from Location")
    List<Location> getAllLocations();

    @Insert
    void insertLocation(Location location);

    @Update
    void updateLocation(Location location);


    @Delete
    void deleteLocation(Location location);


}
