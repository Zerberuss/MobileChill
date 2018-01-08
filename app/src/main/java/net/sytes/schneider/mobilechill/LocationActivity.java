package net.sytes.schneider.mobilechill;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.Converter.LocationConverter;
import net.sytes.schneider.mobilechill.database.LocationEntity;
import net.sytes.schneider.mobilechill.database.Tasks.GetLocationsTask;
import net.sytes.schneider.mobilechill.database.Tasks.HolderClass;
import net.sytes.schneider.mobilechill.database.Tasks.InsertLocationTask;
import net.sytes.schneider.mobilechill.database.Tasks.RemoveLocationTask;
import net.sytes.schneider.mobilechill.database.Tasks.UpdateLocationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Created by Timo Hasenbichler on 18.12.2017.
 */

public class LocationActivity extends ListActivity {

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {

                case R.id.navigation_dashboard:
                    Intent y = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(y);
                    return true;

                case R.id.navigation_home:
                    Intent x = new Intent(getApplicationContext(), LocationActivity.class);
                    //startActivity(x);

                    return true;

                case R.id.navigation_notifications:
                    Intent i = new Intent(getApplicationContext(), ConnectionsActivity.class);

                    startActivity(i);
                    return true;
            }
            return false;
        }

    };
    private AppDatabase appDatabase;
    private LocationService locationService;
    private FusedLocationProviderClient mFusedLocationClient;
    private List<LocationEntity> locationEntityList;
    private LocationConverter locationConverter = new LocationConverter();
    private LocationListAdapter adapter;
    private ConnectionService connectionService;
    private String connectedSSID = null;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").build();
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;

        try {
            getLocationEntities(holderClass);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        refreshListView(holderClass);


        final Button saveBtn = (Button) findViewById(R.id.button_save);
        final Button getLocationButton = (Button) findViewById(R.id.button_load);


        getLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //refresh
                refreshListView(holderClass);
            }
        });
        saveBtn.setOnClickListener(c -> {

            LocationEntity locationEntity = new LocationEntity();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        LocationEntity locationEntity = locationConverter.convert2LocationEntity(location, geocoder);


                        //if not in db
                        if (!checkIfinDatabase(locationEntity) && !getConnectedSSID().equals("")) {
                            locationEntity.setWlanSSID(getConnectedSSID());
                            holderClass.locationEntity = locationEntity;
                            holderClass.appDatabase = appDatabase;
                            insertLocationEntity(holderClass);
                            Snackbar.make(findViewById(R.id.container), "New Location saved.",
                                    Snackbar.LENGTH_SHORT)
                                    .show();


                        } else {
                            Toast.makeText(getApplicationContext(), "Location already saved.", Toast.LENGTH_SHORT).show();
                        }


                    }

                    refreshListView(holderClass);

                }
            });
        });

    }

    public String getConnectedSSID() {

        if (wifiManager.getConnectionInfo() != null) {
            return wifiManager.getConnectionInfo().getSSID();

        }


        return "";
    }

    public Optional<LocationEntity> locationRangeCheck(Location newLocation) {
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;
        try {
            getLocationEntities(holderClass);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (locationEntityList != null && locationEntityList.size() > 0) {

            for (LocationEntity e : locationEntityList) {
                Location locationInDB = locationConverter.convert2Location(e);
                float distanceInMeters = locationInDB.distanceTo(newLocation);
                boolean result = distanceInMeters < 300;
                if (result) {
                    Log.i("INFO", "IN RANGE");
                    return Optional.ofNullable(e);
                }
            }
        }


        return Optional.empty();


    }

    public void refreshListView(HolderClass holderClass) {
        Log.i("INFO", locationEntityList.toString());
        adapter = new LocationListAdapter(LocationActivity.this, R.layout.location_list_item, locationEntityList);
        //refresh
        try {
            locationEntityList = new GetLocationsTask().execute(holderClass).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        ListView listView = (ListView) findViewById(android.R.id.list);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(LocationActivity.this, "location deleted", Toast.LENGTH_LONG);
            }
        });
        listView.setAdapter(adapter);
    }

    private boolean checkIfinDatabase(LocationEntity locationEntity) {
        boolean inDatabase = false; //Default
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;
        try {
            getLocationEntities(holderClass);
        } catch (ExecutionException | InterruptedException e) {
            Log.i("ERROR",e.toString());
        }

        if (locationEntityList != null || locationEntityList.size() > 0) {
            for (LocationEntity e : locationEntityList)
                if (!(Objects.equals(e.getLatidude(), locationEntity.getLatidude()) && Objects.equals(e.getLongitude(), e.getLatidude()))) {
                    inDatabase = true;
                }
        }
        //not in list
        Log.i("INFO", "not in list");
        return inDatabase;
    }



    public void removeListEntry(View view) {
        ImageButton bt = (ImageButton) view;
        LocationEntity item2remove = (LocationEntity) bt.getTag();

        Toast.makeText(this, "Button " + bt.getTag().toString(), Toast.LENGTH_LONG).show();
        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;
        holderClass.locationEntity = item2remove;
        removeLocationEntity(holderClass);
        adapter.remove(item2remove);
    }

    public void changeWirelessPreferences(View view) {
        ToggleButton toggleButton = (ToggleButton) view;
        LocationEntity locationEntity = (LocationEntity) toggleButton.getTag();


        if (locationEntity.isWirelessPreferences()) {
            //AN
            toggleButton.setTextOff("OFF");
            toggleButton.toggle();
            locationEntity.setWirelessPreferences(false);
        } else {
            //AUS
            toggleButton.setTextOn("ON");
            toggleButton.toggle();
            locationEntity.setWirelessPreferences(true);
        }


        Log.i("INFO", "TOGGLED SWITCH FOR ENTITY");
        HolderClass holderClass = new HolderClass();
        holderClass.locationEntity = locationEntity;
        holderClass.appDatabase = appDatabase;
        //update does not update therefore delete+insert
        //removeLocationEntity(holderClass);
        //insertLocationEntity(holderClass);
        updateLocationEntity(holderClass);
        try {
            getLocationEntities(holderClass);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void modifyName(View view) {
        ImageButton imageButton = (ImageButton) view;
        LocationEntity locationEntity = (LocationEntity) imageButton.getTag();

        HolderClass holderClass = new HolderClass();
        holderClass.appDatabase = appDatabase;
        holderClass.locationEntity = locationEntity;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LocationActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.prompts, null);
        alertDialog.setView(dialogView);

        TextView saveTxt = (TextView) dialogView.findViewById(R.id.promptText);
        saveTxt.setText("Change name:");
        EditText editText = (EditText) dialogView.findViewById(R.id.editTextDialogUserInput);
        editText.setText(locationEntity.getName());


        alertDialog.setTitle("Change name");
        alertDialog.setMessage("Please enter a new Name:");

        final EditText input = new EditText(LocationActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input); // uncomment this line
        alertDialog.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString();
                        locationEntity.setName(newName);
                        holderClass.locationEntity = locationEntity;
                        updateLocationEntity(holderClass);
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }


    public void getLocationEntities(HolderClass holderClass) throws ExecutionException, InterruptedException {
        locationEntityList = new GetLocationsTask().execute(holderClass).get();

    }

    public void removeLocationEntity(HolderClass holderClass) {
        new RemoveLocationTask().execute(holderClass);
    }

    public void insertLocationEntity(HolderClass holderClass) {
        new InsertLocationTask().execute(holderClass);
        refreshListView(holderClass);
    }

    //TODO BUGGED
    public void updateLocationEntity(HolderClass holderClass) {
        removeLocationEntity(holderClass);
        insertLocationEntity(holderClass);
        //new UpdateLocationTask().execute(holderClass);
    }

    /*
    final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.i(TAG, "Received Location ->  Accurency: " + intent.getFloatExtra("locationAc",0));
            double lo = intent.getDoubleExtra("locationLo", 0);
            double la = intent.getDoubleExtra("locationLa", 0);

            Location loc = new Location("dummyProvider");
            loc.setLongitude(intent.getDoubleExtra("locationLa", 0));
            loc.setLongitude(intent.getDoubleExtra("locationLo", 0));
            Optional<LocationEntity> locationEntity = locationRangeCheck(loc);
            if (locationEntity.isPresent()) {//need to check if relevant location {
                //TURN ON RELATED WLAN/S
                wifiManager.setWifiEnabled(true);
                List<ScanResult> results = wifiManager.getScanResults();
                List<String> strResults = new ArrayList<>();
                results.forEach(scanResult -> {
                    strResults.add(scanResult.SSID);
                });

                if (locationEntity.get().isWirelessPreferences() && wlanInRange(strResults, locationEntity.get())) {
                    Intent newConnectionIntent = new Intent(ConnectionService.ACTION_SEND_INFO_TAG);
                    newConnectionIntent.putExtra("ssid", locationEntity.get().getWlanSSID());        //TODO REMOVE -> for testing only
                    sendBroadcast(newConnectionIntent);
                    Log.i("Connnect to Wlan with SSID:", locationEntity.get().getWlanSSID());
                    Toast.makeText(getApplicationContext(), "Location updated and connected", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();

            }


        }

    }; */




}
