package net.sytes.schneider.mobilechill.database.Tasks;

import android.os.AsyncTask;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.LocationEntity;

/**
 * Created by Timo Hasenbichler on 05.01.2018.
 */

public class InsertLocationTask extends AsyncTask<HolderClass,Void,Void> {


    @Override
    protected Void doInBackground(HolderClass... HolderClasses) {
        AppDatabase appDatabase = HolderClasses[0].appDatabase;
        LocationEntity locationEntity = HolderClasses[0].locationEntity;
        appDatabase.locationsDao().insertLocation(locationEntity);
        return null;
    }
}
