package net.sytes.schneider.mobilechill;

import android.os.AsyncTask;

import net.sytes.schneider.mobilechill.database.LocationDao;

/**
 * Created by Timo Hasenbichler on 17.12.2017.
 */


public class loadDBTask extends AsyncTask<Void, Void, Void> {

    private LocationDao locationDao;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //Perform pre-adding operation here.
    }

    @Override
    protected Void doInBackground(Void... Void) {

        return null;
    }
}



