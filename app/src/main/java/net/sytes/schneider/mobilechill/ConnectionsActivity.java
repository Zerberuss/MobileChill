package net.sytes.schneider.mobilechill;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class ConnectionsActivity extends Activity {

    private TextView mTextMessage;
    private ImageView wifiStatus;
    private TextView wifiDescription;
    private Switch wifiSwitch;
    private TextView wifiDetailsTxt;


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
                    mTextMessage.setText(R.string.title_HomeLocation);
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
        wifiStatus = (ImageView) findViewById(R.id.wifistatus);
        wifiDescription = (TextView) findViewById(R.id.wifidescribtion);
        wifiDetailsTxt = (TextView) findViewById(R.id.wifidetails);
        wifiDetailsTxt.setMovementMethod(new ScrollingMovementMethod());
        wifiSwitch = (Switch) findViewById(R.id.wifiswitch);


        registerReceiver(mWifiScanReceiver,
                new IntentFilter(ConnectionService.ACTION_BROADCAST_TAG));

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Intent newConnectionIntent = new Intent(ConnectionService.ACTION_SEND_INFO_TAG);
            newConnectionIntent.putExtra("isWifiOn", isChecked);
            sendBroadcast(newConnectionIntent);
            }
        });

        Intent newConnectionIntent = new Intent(ConnectionService.ACTION_SEND_INFO_TAG);
        sendBroadcast(newConnectionIntent);
    }

    final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            wifiDetailsTxt.setText(intent.getStringExtra("wifiDetailsStr"));
            wifiDescription.setText(intent.getStringExtra("wifiConnection"));
        }
    };




    public void switchToHomeLocations() {
        Intent i = new Intent(this, LocationActivity.class);

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
