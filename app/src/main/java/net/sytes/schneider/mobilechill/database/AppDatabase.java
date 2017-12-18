package net.sytes.schneider.mobilechill.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import net.sytes.schneider.mobilechill.database.Converter.Converters;

/**
 * Created by Timo Hasenbichler on 16.12.2017.
 */
@Database(entities = {LocationEntity.class,WirelessNetwork.class}, version = 3)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase{
    public abstract LocationDao locationsDao();
    public abstract WirelessNetworkDao wirelessNetworkDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app-database")
                            .allowMainThreadQueries()      //remove on live
                            .build();
        }
        return INSTANCE;
    }
    public static void destroyInstance() {
        INSTANCE = null;
    }
}