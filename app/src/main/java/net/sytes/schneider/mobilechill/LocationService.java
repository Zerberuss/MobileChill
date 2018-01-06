package net.sytes.schneider.mobilechill;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.security.AccessControlException;
import java.util.Calendar;


public class LocationService extends Service {
    private static final String TAG = "LocationService";
    static final String NEW_LOCATION_ACTION_TAG = "LocationService";
    private static final int LOCATION_INTERVAL = 100;  //5 * 60 * 1000;     //alle 5 Minuten aktuellen Standort abfragen
    private static final float LOCATION_DISTANCE = 0;
    static final String ACTION_GET_NEW_LOCATION = "LocationServiceGetInfo";

    private LocationManager mLocationManager = null;

    private Location mLastLocation = null;

    private class LocationListener implements android.location.LocationListener
    {
        public LocationListener(String provider)
        {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.i(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.i(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            sendLocationBroadcast(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.NETWORK_PROVIDER)
            //new LocationListener(LocationManager.GPS_PROVIDER)        for longer battery life, only the Network provider is active
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
            mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "network provider does not exist, " + ex.getMessage());
        }
        getFineLoctaion();
    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "Destroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.e(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    final BroadcastReceiver mNewInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.i(TAG, "New Location Info will be sent out..");

            getFineLoctaion();
            if (mLastLocation != null)
                sendLocationBroadcast(mLastLocation);
        }
    };

    final BroadcastReceiver mNewFineInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.i(TAG, "New Fine Location received...");
            double lo = intent.getDoubleExtra("locationLo",0);
            double la = intent.getDoubleExtra("locationLa",0);
            double a = intent.getDoubleExtra("locationA",0);
            if (mLastLocation == null) {
                Log.w(TAG, "Can't update last location! -> creating new location");
                mLastLocation = new Location(mLocationManager.getAllProviders().get(0));
                //return;
            }
            mLastLocation.setTime(Calendar.getInstance().getTimeInMillis());
            mLastLocation.setAltitude(a);
            mLastLocation.setLatitude(la);
            mLastLocation.setLongitude(lo);

            sendLocationBroadcast(mLastLocation);
        }
    };

    void sendLocationBroadcast(Location location){
        checkForHomeConnection(location);

        Intent newLocationIntent = new Intent(LocationService.NEW_LOCATION_ACTION_TAG);
        newLocationIntent.putExtra("locationA", location.getAltitude());
        newLocationIntent.putExtra("locationLo", location.getLongitude());
        newLocationIntent.putExtra("locationLa", location.getLatitude());
        sendBroadcast(newLocationIntent);
    }

    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                registerReceiver(mNewInfoScanReceiver,
                        new IntentFilter(LocationService.ACTION_GET_NEW_LOCATION));
                registerReceiver(mNewFineInfoScanReceiver,
                        new IntentFilter(LocationFineService.NEW_FINE_LOCATION_ACTION_TAG));
            }
        } else {
            Toast.makeText(this, "Please confirm location access!", Toast.LENGTH_LONG).show();
            throw new AccessControlException("No Permission to location for location service");
        }
    }

    void getFineLoctaion(){
        Intent newLocationIntent = new Intent(LocationFineService.ACTION_GET_NEW_FINE_LOCATION);
        sendBroadcast(newLocationIntent);
    }

    void checkForHomeConnection(Location location){
        System.out.print("Implement DB request and check for Homelocation");            //if mlastLocation im Umgreis von ca 3km -> LocationFineService fragen -> mit dessen antwort überprüfen ob HomeLocation innerhalb 300m liegt
        boolean isHome = false;
        boolean activateWlan = true;

        /*
        if(isHome && activateWlan){

        }
        */

    }
}
