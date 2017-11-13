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
        notifications = (FrameLayout) findViewById(R.id.notifications);
        Switch wifiSwitch = (Switch) findViewById(R.id.wifiswitch);
    }


    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_activity_main);
                    if (notifications.getTranslationY() == 0) {
                        notifications.animate()
                                .translationY(-notifications.getHeight());
                    }
                    if (dashboard.getTranslationY() == 0) {
                        dashboard.animate()
                                .translationY(-dashboard.getHeight());
                    }
                    startMainActivity();
                    return true;


                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    if (dashboard.getTranslationY() == -dashboard.getHeight()) {
                        dashboard.animate()
                                .translationY(0);
                    }
                    if (notifications.getTranslationY() == 0) {
                        notifications.animate()
                                .translationY(-notifications.getHeight());
                    }

                    //startDashboard();
                    return true;

                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    if (notifications.getTranslationY() == -notifications.getHeight()) {
                        notifications.animate()
                                .translationY(0);
                    }
                    if (dashboard.getTranslationY() == 0) {
                        dashboard.animate()
                                .translationY(-dashboard.getHeight());
                    }
                    return true;
            }
            return false;
        }

        
    };




    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }


}