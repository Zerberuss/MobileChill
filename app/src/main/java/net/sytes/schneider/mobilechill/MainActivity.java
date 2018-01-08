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
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.Converter.Converters;
import net.sytes.schneider.mobilechill.database.Converter.LocationConverter;
import net.sytes.schneider.mobilechill.database.LocationDao;
import net.sytes.schneider.mobilechill.database.LocationEntity;
import net.sytes.schneider.mobilechill.database.Tasks.GetLocationsTask;
import net.sytes.schneider.mobilechill.database.Tasks.HolderClass;
import net.sytes.schneider.mobilechill.database.Tasks.InsertLocationTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

    private String connectedSsid;
    private final String TAG = "MainActivity";
    private int JOBID = 0;
    private TextView mTextMessage;
    private FrameLayout addWifiLocation;
    private FloatingActionButton addHomeButton;
    private ImageView wifiStatus;
    private TextView wifiDescription;
    private Switch wifiSwitch;
    private Switch locationTrackingSwitch;
    private TextView wifiDetailsTxt;
    private Button okButton;

    private LocationDao locationDao;
    private Converters CONVERTER;
    private AppDatabase appDatabase;

    JobScheduler jobScheduler;

    private boolean mapZoomed = false;
    private List<LocationEntity> locationEntityList;
    private WifiManager wifiManager;
    private LocationConverter locationConverter = new LocationConverter();

    private ArrayList<Marker> mMarkers = new ArrayList<>();

    private Location lastLocation;



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
                AppDatabase.class, "app-database").build();
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;


        wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mTextMessage = (TextView) findViewById(R.id.message);
        addWifiLocation = (FrameLayout) findViewById(R.id.addWifiLocation);
        addHomeButton = (FloatingActionButton) findViewById(R.id.addHomeButton);
        wifiStatus = (ImageView) findViewById(R.id.wifistatus);
        wifiDescription = (TextView) findViewById(R.id.wifidescribtion);
        wifiDetailsTxt = (TextView) findViewById(R.id.wifidetails);
        wifiDetailsTxt.setMovementMethod(new ScrollingMovementMethod());
        wifiSwitch = (Switch) findViewById(R.id.wifiswitch);
        locationTrackingSwitch = (Switch) findViewById(R.id.locationTrackingSwitch);
        okButton = (Button) findViewById(R.id.okButton);
        mapZoomed = false;

        addWifiLocation.animate().translationY(-2000);
        wifiDetailsTxt.setText("\nYour current Position and Wifi Connection has been added as Home Location!\n\nCheck Home Locations to disable or remove the location.");
        lastLocation = new Location("dummyprovider");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_dashboard);

        okButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                addLocationAnimation();
            }
        });

        addHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addLocationAnimation();
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
                sendBroadcast(newConnectionIntent);
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
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;
        try {
            getLocationEntities(holderClass);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        mMap = googleMap;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("MainActivity", "Missing permissions to access map!");
                return;
            }
            mMap.setMyLocationEnabled(true);
            LatLng graz = new LatLng(47.074458, 15.438041);                 //  Latitude, Longitude in degrees.
            mMap.addMarker(new MarkerOptions().position(graz).title("Marker in Graz"));
            mMap.clear();
            if(locationEntityList.size()>0){
            for (LocationEntity e : locationEntityList) {
                LatLng ll = new LatLng(e.getLatidude(), e.getLongitude());


                BitmapDescriptor bitmapMarker;

                bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);


                mMarkers.add(mMap.addMarker(new MarkerOptions().position(ll).title(e.getName())
                        .snippet("saved Location")));

                Log.i(TAG,"Setting up marker for position:"+ll+"  " +mMarkers.get(mMarkers.size()-1).getId());
            }
            }else {
                Log.i("INFO","NO LOCATIONS FOUND");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to access map!", e);
            throw e;
        }

    }

    void addLocationAnimation(){
        if(addWifiLocation.getTranslationY() == -2000 ) {
            addWifiLocation.animate()
                    .translationY(300);

            addNewLocation();

        } else {
            addWifiLocation.animate()
                    .translationY(-2000);
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
            String localConnection = intent.getStringExtra("wifiConnection");
            connectedSsid = intent.getStringExtra("wifiSSID");

            wifiDescription.setText(localConnection);
        }
    };

    final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.i(TAG, "Received Location ->  Accurency: " + intent.getFloatExtra("locationAc",0));
            double lo = intent.getDoubleExtra("locationLo", 0);
            double la = intent.getDoubleExtra("locationLa", 0);
            if (locationTrackingSwitch.isChecked()){
                if (mMap != null) {
                    if (!mapZoomed) {                                                     //zomm the map once with first Location Update (workaround -> onResume: map: null)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 18f));
                        mapZoomed = true;
                    } else
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), mMap.getCameraPosition().zoom));
                }
            }
            lastLocation.setTime(Calendar.getInstance().getTimeInMillis());
            lastLocation.setAltitude(intent.getDoubleExtra("locationAl",0));
            lastLocation.setAccuracy(intent.getFloatExtra("locationAc",0));
            lastLocation.setLatitude(la);
            lastLocation.setLongitude(lo);
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


    public void getLocationEntities(HolderClass holderClass) throws ExecutionException, InterruptedException {
        locationEntityList = new GetLocationsTask().execute(holderClass).get();

    }

    void addNewLocation(){
        //vars: wifiSSID, lastLocation
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        LocationEntity locationEntity = new LocationEntity();
        locationEntity = locationConverter.convert2LocationEntity(lastLocation,geocoder);

        //check if in DB
        if(!checkIfinDatabase(locationEntity)){
            locationEntity.setWlanSSID(getConnectedSSID());


            holderClass.locationEntity = locationEntity;

            insertLocationEntity(holderClass);
            Snackbar.make(findViewById(R.id.container), "New Location saved.",
                    Snackbar.LENGTH_SHORT)
                    .show();

        } else {
            Toast.makeText(getApplicationContext(), "Location already saved.", Toast.LENGTH_SHORT).show();

        }
    }

    public void insertLocationEntity(HolderClass holderClass) {
        new InsertLocationTask().execute(holderClass);

    }

    private boolean checkIfinDatabase(LocationEntity locationEntity) {
        boolean inDatabase = false; //Default
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;
        try {
            getLocationEntities(holderClass);
        } catch (ExecutionException | InterruptedException e) {
            Log.i("ERROR",e.toString());
        }

        if (locationEntityList != null || locationEntityList.size() > 0) {
            for (LocationEntity e : locationEntityList)
                if ((Objects.equals(e.getLatidude(), locationEntity.getLatidude()) && Objects.equals(e.getLongitude(), locationEntity.getLongitude()))) {
                    inDatabase = true;
                }
        }
        //not in list
        Log.i("INFO", "not in list");
        return inDatabase;
    }

    public String getConnectedSSID() {

        if (wifiManager.getConnectionInfo() != null) {
            return wifiManager.getConnectionInfo().getSSID();

        }


        return "";
    }

}
