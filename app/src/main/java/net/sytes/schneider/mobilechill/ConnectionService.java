package net.sytes.schneider.mobilechill;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.security.AccessControlException;
import java.util.List;


public class ConnectionService extends Service {
    private static final String TAG = "ConnectionService";
    static final String ACTION_BROADCAST_TAG = "ConnectionServiceBroadcast";
    static final String ACTION_SEND_INFO_TAG = "ConnectionServiceSendInfo";

    private WifiManager mWifiManager = null;
    private String wifiConnection = "";
    private String wifiDetailsStr = "";
    private String wifiSSIDList = "";


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }


    @Override
    public void onCreate()
    {
        Log.w(TAG, "onCreate");
        initializeConnectionManager();
    }


    final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            // This condition is not necessary if you listen to only one action
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();

                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected())
                    wifiConnection = (mWifiManager.getConnectionInfo().getSSID());
                else if (netInfo != null && netInfo.isConnectedOrConnecting())
                    wifiConnection = ("connecting");
                else
                    wifiConnection = ("no connection");

                Log.i("MainActivity", "New Wifi Scan!\n");

                saveWifiDetails(mScanResults);
                Toast.makeText(getApplicationContext(), "Connection info updated", Toast.LENGTH_LONG).show();
                broadcastConnectionInfos();
            }
        }
    };

    final BroadcastReceiver mNewInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.w(TAG, "New Connection Info will be sent out..");
            boolean wifiOn = intent.getBooleanExtra("isWifiOn", true);
            mWifiManager.setWifiEnabled(wifiOn);
            broadcastConnectionInfos();

            if(wifiOn){
                mWifiManager.startScan();
            }
        }
    };

    public void broadcastConnectionInfos(){
        Intent newConnetionIntent = new Intent(ACTION_BROADCAST_TAG);
        newConnetionIntent.putExtra("wifiConnection", wifiConnection);
        newConnetionIntent.putExtra("wifiDetailsStr", wifiDetailsStr);
        newConnetionIntent.putExtra("wifiSSIDList", wifiSSIDList);
        sendBroadcast(newConnetionIntent);
    }


    public void saveWifiDetails(List<ScanResult> wifiList){
        String wifiTxt = null;
        StringBuilder ssids = new StringBuilder();

        if(wifiList.size()>1) {
            wifiTxt = "There are " + wifiList.size() + " WIFIs available:";
            ssids.append(wifiTxt).append("\n");
        }
        else {
            wifiTxt = "There is " + "one" + " WIFI available:";
            ssids.append(wifiTxt).append("\n");
        }
        for (ScanResult wifi : wifiList) {
            ssids.append("\n   ").append(wifi.SSID);

            wifiTxt += "\n";
            wifiTxt += "\n " + wifi.SSID;
            wifiTxt += "\nBSSID  " + wifi.BSSID;
            wifiTxt += "\nfrequency  " + wifi.frequency;
            wifiTxt += "\nlevel:  " + wifi.level;
            wifiTxt += "\ncapabilities  " + wifi.capabilities;
            wifiTxt += "\nchannelWidth  " + wifi.channelWidth;
        }
        wifiDetailsStr = wifiTxt;
        wifiSSIDList = ssids.toString();
    }

    private void initializeConnectionManager() {
        Log.e(TAG, "initializeLocationManager");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            if (mWifiManager == null) {
                registerReceiver(mWifiScanReceiver,
                        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                registerReceiver(mNewInfoScanReceiver,
                        new IntentFilter(ConnectionService.ACTION_SEND_INFO_TAG));

                mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                mWifiManager.startScan();
            }
        } else {
            Toast.makeText(this, "Please confirm location access!", Toast.LENGTH_LONG).show();
            throw new AccessControlException("No Permission to location for location service");
        }

    }


}
