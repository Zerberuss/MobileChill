package net.sytes.schneider.mobilechill;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
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
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.Converter.Converters;
import net.sytes.schneider.mobilechill.database.LocationEntity;
import net.sytes.schneider.mobilechill.database.LocationDao;

import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;


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


    //WifiManager mWifiManager;
    List<ScanResult> mScanResults;

    private WifiManager mWifiManager;


    private LatLng currentLocation;

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    /*
                    if(nearbyWifiList.getTranslationY() == -nearbyWifiList.getHeight() ) {
                        nearbyWifiList.animate()
                                .translationY(0);
                    }
                    if(notifications.getTranslationY() == 0 ){
                        notifications.animate()
                                .translationY(-notifications.getHeight());
                    }
                    */
                    return true;

                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_activity_main);
                    /*
                    if(notifications.getTranslationY() == 0 ){
                        notifications.animate()
                                .translationY(-notifications.getHeight());
                    }
                    if(nearbyWifiList.getTranslationY() == 0 ){
                        nearbyWifiList.animate()
                                .translationY(-nearbyWifiList.getHeight());
                    }
                    */
                    Homelocations();
                    return true;

                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    /*
                    if(notifications.getTranslationY() == -notifications.getHeight() ){
                        notifications.animate()
                                .translationY(0);
                    }
                    if(nearbyWifiList.getTranslationY() == 0 ){
                        nearbyWifiList.animate()
                                .translationY(-nearbyWifiList.getHeight());
                    }*/
                    return true;
            }
            return false;
        }

    };


    @SuppressLint("MissingPermission")
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



        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_dashboard);


        addHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(nearbyWifiList.getTranslationY() == -nearbyWifiList.getHeight() ) {
                    nearbyWifiList.animate()
                            .translationY(+300);
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


        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled() == false) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "enabled wifi...",
                    Toast.LENGTH_LONG).show();

            mWifiManager.setWifiEnabled(true);
        }

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mWifiManager.setWifiEnabled(true);
                    wifiDescription.setText("connecting");
                }
                else {
                    mWifiManager.setWifiEnabled(false);
                    wifiDescription.setText("Chilling\nturned off");
                }
            }
        });


        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mWifiManager.startScan();

        registerReceiver( mLocationReceiver, new IntentFilter(LocationService.ACTION_TAG));

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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Add a marker in Sydney and move the camera
            LatLng graz = new LatLng(47.074458, 15.438041);                 //	Latitude, Longitude in degrees.
            mMap.addMarker(new MarkerOptions().position(graz).title("Marker in Graz"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(graz));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(graz, 12.0f));

        } catch (Exception e) {
            Log.e("MainActivity", "Failed to access map!", e);
            throw e;
        }
        nearbyWifiList.animate().translationY(-nearbyWifiList.getHeight());
    }


    public String wifiDetails(List<ScanResult> wifiList){
        String wifiTxt = "Add one of these " + wifiList.size() + " WIFI(s):";

        for (ScanResult wifi : wifiList) {
            wifiTxt += "\n";

            wifiTxt += "\n " + wifi.SSID;
            /*wifiTxt += "\nBSSID  " + wifi.BSSID;
            wifiTxt += "\nfrequency  " + wifi.frequency;
            wifiTxt += "\nlevel:  " + wifi.level;
            wifiTxt += "\ncapabilities  " + wifi.capabilities;
            wifiTxt += "\nchannelWidth  " + wifi.channelWidth;
            */
        }

        wifiDetailsTxt.setText(wifiTxt);
        return wifiTxt;
    }

    final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            // This condition is not necessary if you listen to only one action
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                wifiSwitch.setChecked(true);

                List<ScanResult> mScanResults = mWifiManager.getScanResults();

                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected())
                    wifiDescription.setText(mWifiManager.getConnectionInfo().getSSID());
                else if (netInfo != null && netInfo.isConnectedOrConnecting())
                    wifiDescription.setText("connecting");
                else
                    wifiDescription.setText("no connection");


                Log.i("MainActivity", "New Wifi Scan!\n");

                wifiDetails(mScanResults);
                Toast.makeText(getApplicationContext(), "Scan results are available", Toast.LENGTH_LONG).show();
            }
        }

    };

    final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            System.out.println("Location Height: " + intent.getDoubleExtra("locationA",0));//
            double lo = intent.getDoubleExtra("locationLo",0);
            double la = intent.getDoubleExtra("locationLa",0);
            if (locationTrackingSwitch.isChecked())
                currentLocation = new LatLng(lo,la);
                Log.i("Location","new Location"+currentLocation.toString());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lo, la)));
        }
    };

    public void Homelocations() {
        Intent i = new Intent(this, DashboardActivity.class);

        unregisterReceiver(mWifiScanReceiver);
        unregisterReceiver(mLocationReceiver);


        //finish();  //Kill the activity from which you will go to next activity
        startActivity(i);
    }

}
