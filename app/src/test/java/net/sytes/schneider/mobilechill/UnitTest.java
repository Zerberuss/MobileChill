package net.sytes.schneider.mobilechill;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.LocationEntity;
import net.sytes.schneider.mobilechill.database.Tasks.GetLocationsTask;
import net.sytes.schneider.mobilechill.database.Tasks.HolderClass;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Timo Hasenbichler on 30.01.2018.
 */

public class UnitTest  extends AndroidTestCase {


    private AppDatabase appDatabase;
    private Context context;
    private List<LocationEntity> locationEntityList;


    @Before
    public void setUpDB() throws ExecutionException, InterruptedException {
        context = new MockContext();
        appDatabase = Room.databaseBuilder(context,
                AppDatabase.class, "app-database").build();

        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;

        locationEntityList = new GetLocationsTask().execute(holderClass).get();

    }


    @Test
    public void checkAppDB(){
        assertNotNull(locationEntityList);
    }


}
