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


public class LocationService extends Service {
    private static final String TAG = "LocationService";
    static final String ACTION_TAG = "LocationService";
    private static final int LOCATION_INTERVAL = 2 * 60 * 1000;
    private static final float LOCATION_DISTANCE = 300;
    static final String ACTION_GET_NEW_LOCATION = "LocationServiceGetInfo";

    private LocationManager mLocationManager = null;

    private Location mLastLocation = null;

    private class LocationListener implements android.location.LocationListener
    {
        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

            Log.e(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);

            mLastLocation.set(location);
            mLocationManager.removeUpdates(mLocationListeners[1]);
            sendLocationBroadcast(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.GPS_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "Destroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    final BroadcastReceiver mNewInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.w(TAG, "New Location Info will be sent out..");
            if (mLastLocation != null)
                sendLocationBroadcast(mLastLocation);
        }
    };

    void sendLocationBroadcast(Location location){
        checkForHomeConnection(location);

        Intent newLocationIntent = new Intent(LocationService.ACTION_TAG);
        newLocationIntent.putExtra("locationA", location.getAltitude());
        newLocationIntent.putExtra("locationLo", location.getLongitude());
        newLocationIntent.putExtra("locationLa", location.getLatitude());
        sendBroadcast(newLocationIntent);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                registerReceiver(mNewInfoScanReceiver,
                        new IntentFilter(LocationService.ACTION_GET_NEW_LOCATION));
            }
        } else {
            Toast.makeText(this, "Please confirm location access!", Toast.LENGTH_LONG).show();
            throw new AccessControlException("No Permission to location for location service");
        }
    }

    void checkForHomeConnection(Location location){
        System.out.print("Implement DB request and check for Homelocation");
        boolean isHome = false;
        boolean activateWlan = true;

        /*
        if(isHome && activateWlan){

        }
        */

    }
}
