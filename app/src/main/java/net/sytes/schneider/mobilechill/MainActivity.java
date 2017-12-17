package net.sytes.schneider.mobilechill;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
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

import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;


    private TextView mTextMessage;
    private FrameLayout dashboard;
    private FrameLayout notifications;
    private ImageView wifiStatus;
    private TextView wifiDescribtion;
    private Switch wifiSwitch;
    private TextView wifiDetailsTxt;

    //WifiManager mWifiManager;
    List<ScanResult> mScanResults;

    private WifiManager mWifiManager;


    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_activity_main);
                    if(notifications.getTranslationY() == 0 ){
                        notifications.animate()
                                .translationY(-notifications.getHeight());
                    }
                    if(dashboard.getTranslationY() == 0 ){
                        dashboard.animate()
                                .translationY(-dashboard.getHeight());
                    }
                    return true;


                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    if(dashboard.getTranslationY() == -dashboard.getHeight() ) {
                        dashboard.animate()
                                .translationY(0);
                    }
                    if(notifications.getTranslationY() == 0 ){
                        notifications.animate()
                                .translationY(-notifications.getHeight());
                    }

                    startDashboard();




                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    if(notifications.getTranslationY() == -notifications.getHeight() ){
                        notifications.animate()
                                .translationY(0);
                    }
                    if(dashboard.getTranslationY() == 0 ){
                        dashboard.animate()
                                .translationY(-dashboard.getHeight());
                    }
                    return true;
            }
            return false;
        }

    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mTextMessage = (TextView) findViewById(R.id.message);
        dashboard = (FrameLayout) findViewById(R.id.dashboard);
        notifications = (FrameLayout) findViewById(R.id.notifications);
        wifiStatus = (ImageView) findViewById(R.id.wifistatus);
        wifiDescribtion = (TextView) findViewById(R.id.wifidescribtion);
        wifiDetailsTxt = (TextView) findViewById(R.id.wifidetails);
        wifiDetailsTxt.setMovementMethod(new ScrollingMovementMethod());
        wifiSwitch = (Switch) findViewById(R.id.wifiswitch);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.«
        if( mMap == null) {
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
                        PackageManager.PERMISSION_GRANTED) {
            
            startService(new Intent(this, LocationService.class));

        } else {
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        MY_PERMISSIONS_REQUEST_LOCATION);
        }



        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled() == false)
        {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "enabled wifi...",
                    Toast.LENGTH_LONG).show();

            mWifiManager.setWifiEnabled(true);
        }

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mWifiManager.setWifiEnabled(true);
                    wifiDescribtion.setText("connecting");
                }
                else {
                    mWifiManager.setWifiEnabled(false);
                    wifiDescribtion.setText("Chilling\nturned off");
                }
            }
        });


       registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mWifiManager.startScan();
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
            // Add a marker in Sydney and move the camera
            LatLng graz = new LatLng(47.074458, 15.438041);                 //	Latitude, Longitude in degrees.
            mMap.addMarker(new MarkerOptions().position(graz).title("Marker in Graz"));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(graz));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(graz, 12.0f));

        } catch (Exception e) {
            Log.e("MainActivity", "Failed to access map!", e);
            throw e;
        }
        dashboard.animate().translationY(-dashboard.getHeight());
        notifications.animate().translationY(-notifications.getHeight());
    }


    public String wifiDetails(List<ScanResult> wifiList){
        String wifiTxt = "Found " + wifiList.size() + " WIFIs:";

        for (ScanResult wifi : wifiList) {
            wifiTxt += "\n";

            wifiTxt += "\nSSID:  " + wifi.SSID;
            wifiTxt += "\nBSSID  " + wifi.BSSID;
            wifiTxt += "\nfrequency  " + wifi.frequency;
            wifiTxt += "\nlevel:  " + wifi.level;
            wifiTxt += "\ncapabilities  " + wifi.capabilities;
            wifiTxt += "\nchannelWidth  " + wifi.channelWidth;
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
                    wifiDescribtion.setText(mWifiManager.getConnectionInfo().getSSID());
                else if (netInfo != null && netInfo.isConnectedOrConnecting())
                    wifiDescribtion.setText("connecting");
                else
                    wifiDescribtion.setText("no connection");


                Log.i("MainActivity", "New Wifi Scan!\n");

                wifiDetails(mScanResults);
                Toast.makeText(getApplicationContext(), "Scan results are available", Toast.LENGTH_LONG).show();
            }
        }

    };

    public void startDashboard() {
        Intent i = new Intent(this, DashboardActivity.class);

        unregisterReceiver(mWifiScanReceiver);
        finish();  //Kill the activity from which you will go to next activity
        startActivity(i);
    }

}
