package net.sytes.schneider.mobilechill;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * The service returns the exact GPS position (usually much more accurate than the pos. from the provider, but a battery drainer)
 *  Can be used as template in other projects!
 *
 * 1. Used to update the current position of the map in the map
 * 2. Used to verify that you are really near a home location
 */
public class LocationFineService extends Service {
    String TAG = "Location Fine Service";

    static final String ACTION_GET_NEW_FINE_LOCATION = "LocationFineServiceGetInfo";
    static final String NEW_FINE_LOCATION_ACTION_TAG = "LocationFineServiceNewLocation";
    static final String ACTION_SET_KEEP_SENDING_UPDATES = "LocationFineServiceKeepSendingUpdates";

    private static final int UPDATE_INTERVAL = 1000;
    private static final int FASTEST_INTERVAL = 200;

    boolean KEEP_SENDING_UPDATES = true;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient locationClient;

    @Override
    public void onCreate() {
        KEEP_SENDING_UPDATES = true;
        startLocationUpdates();

        registerReceiver(mNewInfoScanReceiver,
                new IntentFilter(LocationFineService.ACTION_GET_NEW_FINE_LOCATION));
        registerReceiver(mSetKeepSendingReceiver,
                new IntentFilter(LocationFineService.ACTION_SET_KEEP_SENDING_UPDATES));

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        locationClient=getFusedLocationProviderClient(this);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        locationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper());
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private void stopLocationUpdates() {
        locationClient.removeLocationUpdates(mLocationCallback);
        locationClient=null;
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i("Fine Location Changed", location.getLatitude() + " and " + location.getLongitude() + "KEEP GETTING: " + KEEP_SENDING_UPDATES);
            if(!KEEP_SENDING_UPDATES)
                stopLocationUpdates();

            sendLocationBroadcast(location);
        }
    }

    final BroadcastReceiver mNewInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.i(TAG, "Fine Location Info will be sent out..");
            getLastLocation();
        }
    };

    final BroadcastReceiver mSetKeepSendingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean newKeepsending = intent.getBooleanExtra("keepSending",false);

            Log.i(TAG, "Configuring Keep Sending Setting: " + newKeepsending);

            if(newKeepsending && !KEEP_SENDING_UPDATES) {
                KEEP_SENDING_UPDATES = true;
                if(locationClient == null)
                    startLocationUpdates();
            }
            KEEP_SENDING_UPDATES = newKeepsending;
        }
    };

    void sendLocationBroadcast(Location location){
        if(location != null) {
            Intent newLocationIntent = new Intent(LocationFineService.NEW_FINE_LOCATION_ACTION_TAG);
            newLocationIntent.putExtra("locationAl", location.getAltitude());
            newLocationIntent.putExtra("locationAc", location.getAccuracy());
            newLocationIntent.putExtra("locationLo", location.getLongitude());
            newLocationIntent.putExtra("locationLa", location.getLatitude());
            sendBroadcast(newLocationIntent);
        }
    }

}
