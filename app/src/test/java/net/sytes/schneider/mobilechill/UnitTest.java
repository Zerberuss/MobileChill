package net.sytes.schneider.mobilechill;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.test.ActivityTestCase;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.LocationEntity;
import net.sytes.schneider.mobilechill.database.Tasks.GetLocationsTask;
import net.sytes.schneider.mobilechill.database.Tasks.HolderClass;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Timo Hasenbichler on 30.01.2018.
 */
public class UnitTest extends AndroidTestCase {

    LocationEntity locationEntity;


    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        locationEntity = new LocationEntity();
        locationEntity.setName("test");
        locationEntity.setWlanSSID("testWlan");
    }


    @Test
    public void checkLocationEntity() {
        assertNotNull(locationEntity.getName());
        assertNotNull(locationEntity.getWlanSSID());
    }


}
