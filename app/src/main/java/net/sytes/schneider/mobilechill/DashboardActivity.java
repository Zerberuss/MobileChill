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

/**
 * Created by andi on 08/11/2017.
 */

public class DashboardActivity extends Activity {
    TextView mTextMessage;
    View notifications;
    View dashboard;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        mTextMessage = (TextView) findViewById(R.id.message);
        dashboard = (FrameLayout) findViewById(R.id.dashboard);

        Switch wifiSwitch = (Switch) findViewById(R.id.wifiswitch);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
    }


    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    startMainActivity();
                    return true;

                case R.id.navigation_home:
                    return true;

                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }


    };




    public void startMainActivity() {

        Intent i = new Intent(this, MainActivity.class);
        finish();  //Kill the activity from which you will go to next activity
        startActivity(i);
    }


}