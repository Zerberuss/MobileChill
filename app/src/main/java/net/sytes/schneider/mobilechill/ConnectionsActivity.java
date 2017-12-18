package net.sytes.schneider.mobilechill;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.Converter.Converters;
import net.sytes.schneider.mobilechill.database.LocationDao;

import java.util.List;

public class ConnectionsActivity extends Activity {

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


    List<ScanResult> mScanResults;

    private WifiManager mWifiManager;



    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    startMainActivity();
                    return true;

                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_activity_main);
                    switchToHomeLocations();
                    return true;

                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);


        mTextMessage = (TextView) findViewById(R.id.message);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_notifications);

        mTextMessage = (TextView) findViewById(R.id.message);
        nearbyWifiList = (FrameLayout) findViewById(R.id.nearbyWifiList);
        addHomeButton = (FloatingActionButton) findViewById(R.id.addHomeButton);
        wifiStatus = (ImageView) findViewById(R.id.wifistatus);
        wifiDescription = (TextView) findViewById(R.id.wifidescribtion);
        wifiDetailsTxt = (TextView) findViewById(R.id.wifidetails);
        wifiDetailsTxt.setMovementMethod(new ScrollingMovementMethod());
        wifiSwitch = (Switch) findViewById(R.id.wifiswitch);
        locationTrackingSwitch = (Switch) findViewById(R.id.locationTrackingSwitch);


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
    }



    public String wifiDetails(List<ScanResult> wifiList){
        String wifiTxt = null;
        if(wifiList.size()>1)
            wifiTxt = "There are " + wifiList.size() + " WIFIs available:";
        else
            wifiTxt = "There is " + "one" + " WIFI available:";

        for (ScanResult wifi : wifiList) {
            wifiTxt += "\n";

            wifiTxt += "\n " + wifi.SSID;
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


    public void switchToHomeLocations() {
        Intent i = new Intent(this, DashboardActivity.class);

        //unregisterReceiver(mWifiScanReceiver);

        onPause();
        startActivity(i);
    }

    public void startMainActivity() {

        Intent i = new Intent(this, MainActivity.class);
        onPause();
        startActivity(i);
    }

}
