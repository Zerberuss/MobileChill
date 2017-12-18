package net.sytes.schneider.mobilechill;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_notifications);
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
                    String tempStr = e.getName()+" "+e.getLatidude()+" "+e.getLongitude();
                    arrOfLoc.add(tempStr);
                });
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(),android.R.layout.simple_expandable_list_item_1,arrOfLoc);
                getListView().setAdapter(adapter);
                Log.i("Info",locationEntities.toString());


            }
        });
        button1.setOnClickListener(c -> {

            LocationEntity locationEntity = new LocationEntity();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Date today = new Date();

                        //calc from lat and long
                        locationEntity.setName("Home1");
                        locationEntity.setCreated(today);
                        locationEntity.setModified(today);
                        locationEntity.setLatidude(location.getLatitude());
                        locationEntity.setLongitude(location.getLongitude());

                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                            List<Address> list = geocoder.getFromLocation(locationEntity.getLatidude(),locationEntity.getLongitude(),1);
                            if(null!=list & list.size()>0){
                                String name = list.get(0).getCountryName();
                                Log.i("my location",name);
                                locationEntity.setName(name);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //if not in db
                        if(!checkIfinDatabase(locationEntity)){
                            appDatabase.locationsDao().insertLocation(locationEntity);

                        } else {

                            Log.i("DUPLICATE","ALREADY IN DB");
                        }




                        //set force disable
                        Log.i("MY CURRENT LOCATION","LAT="+location.getLatitude()+"  LONG="+location.getLongitude());
                    }
                }
            });







        });


    }

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //Navigation
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {

                case R.id.navigation_dashboard:
                    Intent y = new Intent(getApplicationContext() , WirelessNetworkActivity.class);
                    startActivity(y);
                    return true;

                case R.id.navigation_home:
                    Intent x = new Intent(getApplicationContext() , MainActivity.class);
                    startActivity(x);

                    return true;

                case R.id.navigation_notifications:
                    Intent i = new Intent(getApplicationContext() , LocationActivity.class);
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
