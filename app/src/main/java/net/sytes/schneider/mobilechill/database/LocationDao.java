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
public interface LocationDao {


    @Query("Select * from LocationEntity")
    List<LocationEntity> getAllLocations();

    @Insert
    void insertLocation(LocationEntity locationEntity);

    @Update
    void updateLocation(LocationEntity locationEntity);


    @Delete
    void deleteLocation(LocationEntity locationEntity);

    @Query("Select * from locationentity where latidude LIKE :lat AND longitude LIKE :lon")
    List<LocationEntity> checkIfinDB(double lat, double lon);


}
