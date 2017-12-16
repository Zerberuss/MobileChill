package net.sytes.schneider.mobilechill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Example of Broadcast receiver
 */
public class wifiBroadcastReceiver extends BroadcastReceiver {
    private LocationManager locManager;


    @Override
    public void onReceive(Context c, Intent intent) {
        // This condition is not necessary if you listen to only one action
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {


            Log.i("MainActivity", "New Wifi Scan!\n");

        }
    }
}
