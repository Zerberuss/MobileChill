package net.sytes.schneider.mobilechill.database.Tasks;

import android.os.AsyncTask;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.LocationEntity;

/**
 * Created by Timo Hasenbichler on 05.01.2018.
 */

public class RemoveLocationTask extends AsyncTask<HolderClass,Void,Void> {
    @Override
    protected Void doInBackground(HolderClass... holderClasses) {
        AppDatabase appDatabase = holderClasses[0].appDatabase;
        LocationEntity locationEntity = holderClasses[0].locationEntity;
        appDatabase.locationsDao().deleteLocation(locationEntity);
        return null;
    }
}
