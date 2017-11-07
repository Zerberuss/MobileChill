package net.sytes.schneider.mobilechill;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextView mTextMessage;
    private FrameLayout dashboard;
    private FrameLayout notifications;
    private ImageView wifiStatus;
    private TextView wifiDescribtion;
    private Switch wifiSwitch;
    private TextView wifiDetails;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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

    private Switch.OnClickListener wifiSwitchListener = new Switch.OnClickListener() {
        @Override
        public void onClick(View view) {

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
        wifiDetails = (TextView) findViewById(R.id.wifidetails);
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

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.e("MainActivity", "Got Connection\n\n");


            wifiDescribtion.setText(netInfo.getDetailedState().toString() + "   " + netInfo.getDetailedState().compareTo(NetworkInfo.DetailedState.CONNECTED));
            String wifiDetailsTxt = "";

            wifiDetailsTxt += netInfo.getDetailedState().toString() + "\n";
            wifiDetailsTxt += netInfo.getExtraInfo().toString() + "\n";
            //wifiDetailsTxt += netInfo.getReason().toString() + "\n";

            wifiDetails.setText(wifiDetailsTxt);



            if(netInfo.getDetailedState().compareTo(NetworkInfo.DetailedState.CONNECTED) == 0){
                wifiStatus.setVisibility(View.VISIBLE);
            } else {
                wifiStatus.setVisibility(View.INVISIBLE);
            }
            wifiStatus.setVisibility(View.VISIBLE);


        }
        else {
            Log.e("MainActivity", "No Connection -> activate Wifi\n\n");

            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
        }








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


}
