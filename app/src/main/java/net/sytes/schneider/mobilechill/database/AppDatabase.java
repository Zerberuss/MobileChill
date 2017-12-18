package net.sytes.schneider.mobilechill.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import net.sytes.schneider.mobilechill.database.Converter.Converters;

/**
 * Created by Timo Hasenbichler on 16.12.2017.
 */
@Database(entities = {LocationEntity.class,WirelessNetwork.class}, version = 3)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase{
    public abstract LocationDao locationsDao();
    public abstract WirelessNetworkDao wirelessNetworkDao();



}