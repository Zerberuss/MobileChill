package net.sytes.schneider.mobilechill;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Das Service gibt die genaue GPS Position zurürck
 *
 * 1. Wird verwendet um in der Map die aktuelle position zu tracken
 * 2. Wird verwendet um zu verifizieren das man sich wirklich in der Nähe einer Homelocation befindet
 */
public class LocationFineService extends Service implements LocationListener {
    LocationManager mLocationManager;
    String TAG = "Location Fine Service";
    Location location = null;
    static final String ACTION_GET_NEW_FINE_LOCATION = "LocationFineServiceGetInfo";
    static final String NEW_FINE_LOCATION_ACTION_TAG = "LocationFineServiceNewLocation";
    static final String ACTION_SET_KEEP_SENDING_UPDATES = "LocationFineServiceKeepSendingUpdates";

    private static final int LOCATION_INTERVAL = 100;
    boolean KEEP_SENDING_UPDATES = true;

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
//        if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
  //          Log.w(TAG, "last Fine Location too new");
  //      }
        KEEP_SENDING_UPDATES = true;
        configureGpsUpater();

        registerReceiver(mNewInfoScanReceiver,
                new IntentFilter(LocationFineService.ACTION_GET_NEW_FINE_LOCATION));
        registerReceiver(mSetKeepSendingReceiver,
                new IntentFilter(LocationFineService.ACTION_SET_KEEP_SENDING_UPDATES));
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e("Fine Location Changed", location.getLatitude() + " and " + location.getLongitude());
            if(!KEEP_SENDING_UPDATES)
                mLocationManager.removeUpdates(this);

            sendLocationBroadcast(location);
        }
    }

    final BroadcastReceiver mNewInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.i(TAG, "Fine Location Info will be sent out..");
            configureGpsUpater();
            sendLocationBroadcast(location);
        }
    };

    final BroadcastReceiver mSetKeepSendingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.e(TAG, "Configuring Keep Sending Setting");
            KEEP_SENDING_UPDATES = intent.getBooleanExtra("keepSending",false);
        }
    };

    void sendLocationBroadcast(Location location){
        Intent newLocationIntent = new Intent(LocationFineService.NEW_FINE_LOCATION_ACTION_TAG);
        newLocationIntent.putExtra("locationAl", location.getAltitude());
        newLocationIntent.putExtra("locationAc", location.getAccuracy());
        newLocationIntent.putExtra("locationLo", location.getLongitude());
        newLocationIntent.putExtra("locationLa", location.getLatitude());
        sendBroadcast(newLocationIntent);
    }

    void configureGpsUpater(){
        if (mLocationManager != null) {
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, 0, this);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }
        }
    }


    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}


}
