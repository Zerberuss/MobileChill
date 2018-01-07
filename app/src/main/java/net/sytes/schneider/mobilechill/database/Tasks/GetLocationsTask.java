package net.sytes.schneider.mobilechill.database.Tasks;

import android.os.AsyncTask;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.util.List;

/**
 * Created by Timo Hasenbichler on 05.01.2018.
 */

public class GetLocationsTask extends AsyncTask<HolderClass,Void,List<LocationEntity>> {

    @Override
    protected List<LocationEntity> doInBackground(HolderClass... holderClasses) {
        AppDatabase appDatabase = holderClasses[0].appDatabase;
        return appDatabase.locationsDao().getAllLocations();
    }
}
