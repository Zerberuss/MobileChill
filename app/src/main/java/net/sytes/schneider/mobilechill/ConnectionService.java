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
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;


public class ConnectionService extends Service {
    private static final String TAG = "ConnectionService";
    static final String ACTION_BROADCAST_TAG = "ConnectionServiceBroadcast";
    static final String ACTION_SEND_INFO_TAG = "ConnectionServiceSendInfo";

    private WifiManager mWifiManager = null;
    private String wifiConnection = "";
    private String homeWifiSsid = "";
    private String wifiDetailsStr = "";
    private ArrayList<String> wifiSSIDList;
    private boolean wifiStatus;


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


                if (netInfo == null){
                    wifiConnection = ("no connection");

                }
                else if (netInfo.isConnected()){
                    wifiConnection = (mWifiManager.getConnectionInfo().getSSID());
                } else if (netInfo.isConnectedOrConnecting())
                    wifiConnection = ("connecting");

                Log.i(TAG, "New Wifi Scan!\n");

                saveWifiDetails(mScanResults);
                //Toast.makeText(getApplicationContext(), "Wifi info updated", Toast.LENGTH_LONG).show();

                if (netInfo != null && homeWifiSsid != null && !wifiConnection.equals(homeWifiSsid)){
                    Log.i(TAG, "connectToWif! " );
                    connectToWifiSsid(homeWifiSsid);
                }
                broadcastConnectionInfos();
            }
        }
    };

    final BroadcastReceiver mNewInfoScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean wifiOn = intent.getBooleanExtra("isWifiOn", true);
            Log.w(TAG, "New Connection Info will be sent out.. " + wifiOn);

            if(wifiOn != wifiStatus) {
                mWifiManager.setWifiEnabled(wifiOn);
                wifiStatus= wifiOn;
                homeWifiSsid = mWifiManager.getConnectionInfo().getSSID();
                wifiConnection = mWifiManager.getConnectionInfo().getSSID();

                mWifiManager.startScan();
                if(intent.getExtras()!= null && intent.getExtras().containsKey("ssid"))
                    homeWifiSsid = intent.getStringExtra("ssid");
            }

            broadcastConnectionInfos();
        }
    };

    private void connectToWifiSsid(@NonNull String ssid){
        try {
            List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();

            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                if (wifiConfiguration.SSID.equals("\"" + ssid + "\"")) {
                    mWifiManager.enableNetwork(wifiConfiguration.networkId, true);
                    Log.i(TAG, "connectToWifi: will enable " + wifiConfiguration.SSID);
                    mWifiManager.reconnect();
                    wifiConnection = ssid;
                    return; // return! (sometimes logcat showed me network-entries twice, which may will end in bugs
                }
            }
        } catch (NullPointerException | IllegalStateException e) {
            Log.e(TAG, "connectToWifi: Missing network configuration." + e);
        }

    }

    public void broadcastConnectionInfos(){
        Intent newConnetionIntent = new Intent(ACTION_BROADCAST_TAG);
        newConnetionIntent.putExtra("wifiConnection", wifiConnection);
        newConnetionIntent.putExtra("wifiDetailsStr", wifiDetailsStr);
        if(wifiSSIDList!=null)
            newConnetionIntent.putExtra("wifiSSIDList", wifiSSIDList);
        newConnetionIntent.putExtra("wifiSSID", homeWifiSsid);
        sendBroadcast(newConnetionIntent);
    }


    public void saveWifiDetails(List<ScanResult> wifiList){
        String wifiTxt;
        ArrayList<String> ssids = new ArrayList();

        if(wifiList.size()>1) {
            wifiTxt = "There are " + wifiList.size() + " WIFIs available:";
        }
        else {
            wifiTxt = "There is " + "one" + " WIFI available:";
        }
        for (ScanResult wifi : wifiList) {
            ssids.add(wifi.SSID);

            wifiTxt += "\n";
            wifiTxt += "\n " + wifi.SSID;
            wifiTxt += "\nBSSID  " + wifi.BSSID;
            wifiTxt += "\nfrequency  " + wifi.frequency;
            wifiTxt += "\nlevel:  " + wifi.level;
            wifiTxt += "\ncapabilities  " + wifi.capabilities;
            wifiTxt += "\nchannelWidth  " + wifi.channelWidth;
        }
        wifiDetailsStr = wifiTxt;
        wifiSSIDList = ssids;
    }

    private void initializeConnectionManager() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            if (mWifiManager == null) {
                mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                wifiStatus = true;
                mWifiManager.setWifiEnabled(true);

                registerReceiver(mWifiScanReceiver,
                        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                registerReceiver(mNewInfoScanReceiver,
                        new IntentFilter(ConnectionService.ACTION_SEND_INFO_TAG));

                homeWifiSsid = mWifiManager.getConnectionInfo().getSSID();
                wifiConnection = mWifiManager.getConnectionInfo().getSSID();

                broadcastConnectionInfos();

                mWifiManager.startScan();
            }
        } else {
            Toast.makeText(this, "Please confirm location access!", Toast.LENGTH_LONG).show();
            throw new AccessControlException("No Permission to location for location service");
        }

    }


}
