package net.sytes.schneider.mobilechill.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Timo Hasenbichler on 17.12.2017.
 */
@Dao
public interface WirelessNetworkDao {


    @Query("Select * from WirelessNetwork")
    List<WirelessNetwork> getAllWirelessNetworks();

    @Insert
    void insertLocation(WirelessNetwork wirelessNetwork);

    @Update
    void updateLocation(WirelessNetwork wirelessNetwork);


    @Delete
    void deleteLocation(WirelessNetwork wirelessNetwork);



}
