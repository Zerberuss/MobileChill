package net.sytes.schneider.mobilechill;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.Converter.LocationConverter;
import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Timo Hasenbichler on 18.12.2017.
 */

public class LocationActivity extends ListActivity {

    private AppDatabase appDatabase;
    private LocationService locationService;
    private FusedLocationProviderClient mFusedLocationClient;

    private List<LocationEntity> locationEntities;
    private LocationConverter locationConverter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").allowMainThreadQueries().build();


        final Button button1 = (Button) findViewById(R.id.button_id1);
        final Button button = (Button) findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Execute on main thread

                locationEntities = appDatabase.locationsDao().getAllLocations();
                List<String> arrOfLoc = new ArrayList<>();
                locationEntities.forEach(e -> {
                    String tempStr = e.getName() + " " + e.getLatidude() + " " + e.getLongitude();
                    arrOfLoc.add(tempStr);
                });
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(), android.R.layout.simple_expandable_list_item_1, arrOfLoc);
                getListView().setAdapter(adapter);
                Log.i("Info", locationEntities.toString());


            }
        });
        button1.setOnClickListener(c -> {

            LocationEntity locationEntity = new LocationEntity();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        LocationEntity locationEntity = locationConverter.convert2LocationEntity(location,geocoder);


                        //locationEntities = appDatabase.locationsDao().getAllLocations();


                        if(locationRangeCheck(location)){

                            //Turn off Wlan


                        } else {
                            //Turn on Wlan
                        }

                        //calc from lat and long





                        //if not in db
                        if (!checkIfinDatabase(locationEntity)) {
                            appDatabase.locationsDao().insertLocation(locationEntity);

                        } else {

                            Log.i("DUPLICATE", "ALREADY IN DB");
                        }


                        //set force disable
                        Log.i("MY CURRENT LOCATION", "LAT=" + location.getLatitude() + "  LONG=" + location.getLongitude());
                    }
                }
            });







        });

    }


    public boolean locationRangeCheck(Location newLocation){
        locationEntities = appDatabase.locationsDao().getAllLocations();

        if(locationEntities != null && locationEntities.size() > 0){

            for(LocationEntity e : locationEntities){
                Location locationInDB = locationConverter.convert2Location(e);
                float distanceInMeters = locationInDB.distanceTo(newLocation);
                boolean result= distanceInMeters<300;
                if(result){
                    return true;
                }
            }
        }



        return false;


    }





    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {

                case R.id.navigation_dashboard:
                    Intent y = new Intent(getApplicationContext() , MainActivity.class);
                    startActivity(y);
                    return true;

                case R.id.navigation_home:
                    Intent x = new Intent(getApplicationContext() , LocationActivity.class);
                    startActivity(x);

                    return true;

                case R.id.navigation_notifications:
                    Intent i = new Intent(getApplicationContext() , ConnectionsActivity.class);
                    startActivity(i);
                    return true;
            }
            return false;
        }

    };

    public List<LocationEntity> getLocations(){
        new DatabaseAsync().execute();
        return locationEntities;
    }


    private boolean checkIfinDatabase(LocationEntity locationEntity){

        List<LocationEntity> list = appDatabase.locationsDao().checkIfinDB(locationEntity.getLatidude(),locationEntity.getLongitude());
        if(list==null || list.size()==0){

            //if it is not in list return false
            return false;
        }
        return true;
    }


    @SuppressLint("StaticFieldLeak")
    final private class DatabaseAsync extends AsyncTask<Void, Void, List<LocationEntity>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Perform pre-adding operation here.
        }

        @Override
        protected List<LocationEntity> doInBackground(Void... voids) {
            //Let's add some dummy data to the database.

            return appDatabase.locationsDao().getAllLocations();

        }



    }



}
