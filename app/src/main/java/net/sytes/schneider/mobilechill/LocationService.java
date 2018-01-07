package net.sytes.schneider.mobilechill;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * The service determines the current position via provider and checks if a home location is nearby
 * If this is the case, the position is again checked via GPS and adjusted (power-consuming)
 *
 */

public class LocationService extends JobService {
    protected JobParameters mJobParameters;
    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    private static final String TAG = "LocationService";
    static final String NEW_LOCATION_ACTION_TAG = "LocationService";
    private static final int LOCATION_INTERVAL = 60*1000;  //5 * 60 * 1000;     //jede Minute aktuellen Standort abfragen  TODO
    private static final int LOCATION_FASTEST_INTERVAL = 30*1000;
    static final int TIME_DIFFERENCE_THRESHOLD = 1  * 1000;
    static final int TIME_DIFFERENCE_IGNORE_ACCURACY = 6 * 60  * 1000;
    static final String ACTION_GET_NEW_LOCATION = "LocationServiceGetInfo";

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation = new Location("dummyprovider");;

    public LocationService() {
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "Start Job Called");
        startLocationUpdates();
        initializeBroadCastReceivers();
        getLastLocation();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Toast.makeText(this,
                "MyJobService.onStopJob()",
                Toast.LENGTH_SHORT).show();

        unregisterReceiver(mNewFineInfoScanReceiver);
        unregisterReceiver(mNewInfoScanReceiver);
        return false;
    }

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    // do work here
                    onLocationChanged(locationResult.getLastLocation());
                }
            },
            Looper.myLooper());
    }


    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        getFineLocation();

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
                    Log.d(TAG, "Error trying to get last passive location");
                    e.printStackTrace();
                }
            });
    }


    public void onLocationChanged(Location location) {
        Log.i(TAG, "New passive location...");
        if(isBetterLocation(mLastLocation, location)){
            Log.i(TAG, "New passive set as new lastPosition!");
            mLastLocation.set(location);
            checkForHomeAndsendLocationBroadcast(mLastLocation);
        }
    }




    final BroadcastReceiver mNewInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            getLastLocation();
        }
    };

    final BroadcastReceiver mNewFineInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            //Log.i(TAG, "New Fine Location received...");
            Location mLastGpsLocation = new Location("dummyprovider");

            mLastGpsLocation.setTime(Calendar.getInstance().getTimeInMillis());
            mLastGpsLocation.setAltitude(intent.getDoubleExtra("locationAl",0));
            mLastGpsLocation.setLatitude(intent.getDoubleExtra("locationLa",0));
            mLastGpsLocation.setLongitude(intent.getDoubleExtra("locationLo",0));
            mLastGpsLocation.setAccuracy(intent.getFloatExtra("locationAc",0));

            if(isBetterLocation(mLastLocation, mLastGpsLocation)){
                //Log.i(TAG, "New set as new lastPosition!");
                mLastLocation.set(mLastGpsLocation);
                checkForHomeAndsendLocationBroadcast(mLastLocation);
            }
        }
    };

    void checkForHomeAndsendLocationBroadcast(Location location){
        checkForHomeConnection(location);

        Intent newLocationIntent = new Intent(LocationService.NEW_LOCATION_ACTION_TAG);
        newLocationIntent.putExtra("locationAl", location.getAltitude());
        newLocationIntent.putExtra("locationAc", location.getAccuracy());
        newLocationIntent.putExtra("locationLo", location.getLongitude());
        newLocationIntent.putExtra("locationLa", location.getLatitude());
        sendBroadcast(newLocationIntent);
    }

    private void initializeBroadCastReceivers() {
        Log.i(TAG, "initializeLocationManager");

        registerReceiver(mNewInfoScanReceiver,
                new IntentFilter(LocationService.ACTION_GET_NEW_LOCATION));
        registerReceiver(mNewFineInfoScanReceiver,
                new IntentFilter(LocationFineService.NEW_FINE_LOCATION_ACTION_TAG));
    }

    void getFineLocation(){
        Intent newLocationIntent = new Intent(LocationFineService.ACTION_GET_NEW_FINE_LOCATION);
        sendBroadcast(newLocationIntent);
    }

    boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            return true;
        }

        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() <= oldLocation.getAccuracy();

        long timeDifference = newLocation.getTime() - oldLocation.getTime();

        if(isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else if(isMoreAccurate) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.

            // If time difference is not greater then allowed threshold we accept it.
            if(timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        } else if(isNewer && timeDifference > TIME_DIFFERENCE_IGNORE_ACCURACY) {
            //If five minutes went by, also accept inaccurate position
            return true;
        } else
            Log.w(TAG, "new pos is not new enough (" + timeDifference + " !> " + TIME_DIFFERENCE_IGNORE_ACCURACY + ") and not accurate: new ac vs. old ->  "+ newLocation.getAccuracy() +" !< "+ oldLocation.getAccuracy());

        return false;
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
