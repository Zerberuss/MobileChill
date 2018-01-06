package net.sytes.schneider.mobilechill;

import android.Manifest;
import android.app.ActivityManager;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.Converter.Converters;
import net.sytes.schneider.mobilechill.database.LocationDao;
import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

    private final String TAG = "MainActivity";
    private TextView mTextMessage;
    private FrameLayout nearbyWifiList;
    private FloatingActionButton addHomeButton;
    private ImageView wifiStatus;
    private TextView wifiDescription;
    private Switch wifiSwitch;
    private Switch locationTrackingSwitch;
    private TextView wifiDetailsTxt;

    private LocationDao locationDao;
    private Converters CONVERTER;
    private AppDatabase appDatabase;



    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;

                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_activity_main);
                    switchToHomeLocations();
                    return true;

                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_HomeLocation);
                    switchToConnections();
                    return true;
            }
            return false;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").allowMainThreadQueries().build();
       //appDatabase = Room.databaseBuilder(getApplicationContext(),AppDatabase.class, "app-database").build();

        /*

                DEVELOPING
                CHANGE WHEN LIVE

         */
        //create DB

       /* mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Task<Location> myLocTask = mFusedLocationClient.getLastLocation();

        Location myLoc = myLocTask.getResult();

        Log.d("LOCATION DATA:",myLoc.toString());*/

        //remove when live
        Date today = new Date();
        today.setTime(0);
        LocationEntity testLoc = new LocationEntity();
        testLoc.setName("Graz");

        appDatabase.locationsDao().insertLocation(testLoc);
        System.out.print("LocationEntity has been added to DB");
        Log.i("INFO", "LocationEntity has been added");
        List<LocationEntity> locationEntityList = appDatabase.locationsDao().getAllLocations();
        Log.i("INFO", locationEntityList.toString());

        /*

            DO NOT USE DB ON MAIN THREAD

         */

        mTextMessage = (TextView) findViewById(R.id.message);
        nearbyWifiList = (FrameLayout) findViewById(R.id.nearbyWifiList);
        addHomeButton = (FloatingActionButton) findViewById(R.id.addHomeButton);
        wifiStatus = (ImageView) findViewById(R.id.wifistatus);
        wifiDescription = (TextView) findViewById(R.id.wifidescribtion);
        wifiDetailsTxt = (TextView) findViewById(R.id.wifidetails);
        wifiDetailsTxt.setMovementMethod(new ScrollingMovementMethod());
        wifiSwitch = (Switch) findViewById(R.id.wifiswitch);
        locationTrackingSwitch = (Switch) findViewById(R.id.locationTrackingSwitch);


        nearbyWifiList.animate().translationY(-2000);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_dashboard);

        addHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(nearbyWifiList.getTranslationY() == -2000 ) {
                    nearbyWifiList.animate()
                            .translationY(300);
                } else {
                    nearbyWifiList.animate()
                            .translationY(-2000);
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.«
        if (mMap == null) {
            try {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            } catch (Exception e) {
                Log.e("MainActivity", "Failed to create map!", e);
                throw e;
            }
        }

        //Check permissions and start Location Service
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_WIFI_STATE) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CHANGE_WIFI_STATE) ==
                        PackageManager.PERMISSION_GRANTED) {

            if(!isMyServiceRunning(LocationService.class))
                startService(new Intent(this, LocationService.class));
            if(!isMyServiceRunning(LocationFineService.class))
                startService(new Intent(this, LocationFineService.class));
            if(!isMyServiceRunning(ConnectionService.class))
                startService(new Intent(this, ConnectionService.class));
            Log.i("MAIN", "Started Services");

        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }


        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent newConnectionIntent = new Intent(ConnectionService.ACTION_SEND_INFO_TAG);
                newConnectionIntent.putExtra("isWifiOn", isChecked);
                sendBroadcast(newConnectionIntent);
            }
        });

        locationTrackingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent newLocationIntent = new Intent(LocationService.ACTION_GET_NEW_LOCATION);
                sendBroadcast(newLocationIntent);
            }
        });


        registerReceiver(mWifiScanReceiver,
                new IntentFilter(ConnectionService.ACTION_BROADCAST_TAG));
        registerReceiver(mLocationReceiver,
                new IntentFilter(LocationService.NEW_LOCATION_ACTION_TAG));

        getNewLocation();
        getNewWifiData();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("MainActivity", "Missing permissions to access map!");
                return;
            }
            mMap.setMyLocationEnabled(true);

        } catch (Exception e) {
            Log.e("MainActivity", "Failed to access map!", e);
            throw e;
        }
    }

    final void getNewLocation(){
        Intent newLocationIntent = new Intent(LocationService.ACTION_GET_NEW_LOCATION);
        sendBroadcast(newLocationIntent);
    }

    final void getNewWifiData(){
        Intent newConnectionIntent = new Intent(ConnectionService.ACTION_SEND_INFO_TAG);
        sendBroadcast(newConnectionIntent);
    }


    final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            wifiDetailsTxt.setText(intent.getStringExtra("wifiSSIDList"));
            wifiDescription.setText(intent.getStringExtra("wifiConnection"));
        }
    };

    final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            Log.i(TAG, "Received Location ->  Height: " + intent.getDoubleExtra("locationA",0));//
            double lo = intent.getDoubleExtra("locationLo",0);
            double la = intent.getDoubleExtra("locationLa",0);
            if (locationTrackingSwitch.isChecked()){
                Log.i(TAG, "location:   " + lo+" "+la);
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 19f));
                    getNewLocation();
                }
                else
                    Log.e(TAG, "Map not found");
            }
            else
                Log.i(TAG, "Location Tracking disabled");
        }
    };


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void switchToHomeLocations() {
        Intent i = new Intent(this, LocationActivity.class);

        unregisterReceiver(mWifiScanReceiver);
        unregisterReceiver(mLocationReceiver);


        finish();  //Kill the activity from which you will go to next activity
        startActivity(i);
    }

    public void switchToConnections() {
        Intent i = new Intent(this, ConnectionsActivity.class);

        unregisterReceiver(mWifiScanReceiver);
        unregisterReceiver(mLocationReceiver);


        finish();  //Kill the activity from which you will go to next activity
        startActivity(i);
    }

}
