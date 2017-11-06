package net.sytes.schneider.mobilechill;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        dashboard = (FrameLayout) findViewById(R.id.dashboard);
        notifications = (FrameLayout) findViewById(R.id.notifications);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
