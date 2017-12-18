package net.sytes.schneider.mobilechill;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.sytes.schneider.mobilechill.database.WirelessNetwork;

/**
 * Created by andi on 08/11/2017.
 */

public class WirelessNetworkActivity extends Activity {
    TextView mTextMessage;
    View notifications;
    View dashboard;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wirelessnetwork);


        mTextMessage = (TextView) findViewById(R.id.message);
        dashboard = (FrameLayout) findViewById(R.id.dashboard);

        Switch wifiSwitch = (Switch) findViewById(R.id.wifiswitch);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
    }


    //Navigation
    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                Intent i;
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        i = new Intent(getApplicationContext(), WirelessNetworkActivity.class);
                        startActivity(i);
                        return true;

                    case R.id.navigation_home:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        return true;

                    case R.id.navigation_notifications:
                        i = new Intent(getApplicationContext(), LocationActivity.class);
                        startActivity(i);
                        return true;
                }
                return false;
            };







}