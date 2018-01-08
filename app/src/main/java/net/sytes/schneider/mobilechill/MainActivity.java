package net.sytes.schneider.mobilechill;

import android.Manifest;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
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

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.Converter.Converters;
import net.sytes.schneider.mobilechill.database.Converter.LocationConverter;
import net.sytes.schneider.mobilechill.database.LocationDao;
import net.sytes.schneider.mobilechill.database.LocationEntity;
import net.sytes.schneider.mobilechill.database.Tasks.HolderClass;
import net.sytes.schneider.mobilechill.database.Tasks.GetLocationsTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

    private final String TAG = "MainActivity";
    private int JOBID = 0;
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

    JobScheduler jobScheduler;

    private boolean mapZoomed = false;
    private List<LocationEntity> locationEntityList;
    private WifiManager wifiManager;
    private LocationConverter locationConverter = new LocationConverter();





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

        wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);

        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").build();
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;



        mTextMessage = (TextView) findViewById(R.id.message);
        nearbyWifiList = (FrameLayout) findViewById(R.id.nearbyWifiList);
        addHomeButton = (FloatingActionButton) findViewById(R.id.addHomeButton);
        wifiStatus = (ImageView) findViewById(R.id.wifistatus);
        wifiDescription = (TextView) findViewById(R.id.wifidescribtion);
        wifiDetailsTxt = (TextView) findViewById(R.id.wifidetails);
        wifiDetailsTxt.setMovementMethod(new ScrollingMovementMethod());
        wifiSwitch = (Switch) findViewById(R.id.wifiswitch);
        locationTrackingSwitch = (Switch) findViewById(R.id.locationTrackingSwitch);
        mapZoomed = false;

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
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CHANGE_WIFI_STATE) ==
                        PackageManager.PERMISSION_GRANTED) {


            jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder( JOBID++, new ComponentName(getPackageName(), LocationService.class.getName()));
            builder.setPeriodic(15 * 60 * 1000);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

            jobScheduler.schedule(builder.build());

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
                newConnectionIntent.putExtra("ssid", "superwg");        //TODO REMOVE -> for testing only
                sendBroadcast(newConnectionIntent);
            }
        });

        locationTrackingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });


        registerReceiver(mWifiScanReceiver,
                new IntentFilter(ConnectionService.ACTION_BROADCAST_TAG));
        registerReceiver(mLocationReceiver,
                new IntentFilter(LocationService.NEW_LOCATION_ACTION_TAG));
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
            LatLng graz = new LatLng(47.074458, 15.438041);                 //  Latitude, Longitude in degrees.
            mMap.addMarker(new MarkerOptions().position(graz).title("Marker in Graz"));
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to access map!", e);
            throw e;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        setGettingContinousUpdates(false);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapZoomed = false;

        setGettingContinousUpdates(true);
        getNewLocation();
        getNewWifiData();
    }


    final void getNewLocation(){
        Intent newLocationIntent = new Intent(LocationService.ACTION_GET_NEW_LOCATION);
        sendBroadcast(newLocationIntent);
    }

    final void setGettingContinousUpdates(boolean setting){
        Intent newKeepGetingLocUpdatesIntent = new Intent(LocationFineService.ACTION_SET_KEEP_SENDING_UPDATES);
        newKeepGetingLocUpdatesIntent.putExtra("keepSending", setting);
        sendBroadcast(newKeepGetingLocUpdatesIntent);
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
        public void onReceive(Context context, Intent intent) {
            //Log.i(TAG, "Received Location ->  Accurency: " + intent.getFloatExtra("locationAc",0));
            double lo = intent.getDoubleExtra("locationLo", 0);
            double la = intent.getDoubleExtra("locationLa", 0);
            if (locationTrackingSwitch.isChecked())
                Log.i("location", lo + " " + la);
            if (mMap != null) {
                if (!mapZoomed) {                                                     //zomm the map once with first Location Update (workaround -> onResume: map: null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 18f));
                    mapZoomed = true;
                } else
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), mMap.getCameraPosition().zoom));

            }
            HolderClass holderClass = new HolderClass();
            holderClass.appDatabase = appDatabase;
            Location loc = new Location("dummyProvider");
            loc.setLatitude(la);
            loc.setLongitude(lo);
            Optional<LocationEntity> locationEntity = locationRangeCheck(loc);


            if (locationEntity.isPresent()) {
                //TURN ON RELATED WLAN/S
                wifiManager.setWifiEnabled(true);
                List<ScanResult> results = wifiManager.getScanResults();
                List<String> strResults = new ArrayList<>();
                results.forEach(scanResult -> {
                    strResults.add(scanResult.SSID);
                });
                Log.i("loc ssid",locationEntity.get().getWlanSSID());
                if (locationEntity.get().isWirelessPreferences() && wlanInRange(strResults, locationEntity.get())) {
                    Intent newConnectionIntent = new Intent(ConnectionService.ACTION_SEND_INFO_TAG);
                    newConnectionIntent.putExtra("ssid", locationEntity.get().getWlanSSID());        //TODO REMOVE -> for testing only
                    sendBroadcast(newConnectionIntent);
                    Log.i("Connnect to Wlan with SSID:", locationEntity.get().getWlanSSID());
                    Toast.makeText(getApplicationContext(), "Location updated and connected", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();

            }

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
        unregisterReceiver(mLocationReceiver);
        unregisterReceiver(mWifiScanReceiver);

        finish();  //Kill the activity from which you will go to next activity
        startActivity(i);
    }

    public Optional<LocationEntity> locationRangeCheck(Location newLocation) {
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;
        try {
            getLocationEntities(holderClass);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (locationEntityList != null && locationEntityList.size() > 0) {

            for (LocationEntity e : locationEntityList) {
                Location locationInDB = locationConverter.convert2Location(e);
                float distanceInMeters = locationInDB.distanceTo(newLocation);
                boolean result = distanceInMeters < 300;
                if (result) {
                    Log.i("INFO", "IN RANGE");
                    return Optional.ofNullable(e);
                }
            }
        }

        return Optional.empty();

    }

    public boolean wlanInRange(List<String> stringList, LocationEntity locationEntity) {
        if (stringList.size() > 0 && stringList != null) {

            for (String s : stringList) {
                Log.i("INFO", s +"  "+locationEntity.getWlanSSID());
                s = "\""+s+"\"";
                if (s.equals(locationEntity.getWlanSSID())) {
                    Log.i("INFO",s +"  "+locationEntity.getWlanSSID());
                    return true;
                }
            }

        }
        return false;
    }

    public void getLocationEntities(HolderClass holderClass) throws ExecutionException, InterruptedException {
        locationEntityList = new GetLocationsTask().execute(holderClass).get();

    }
}
