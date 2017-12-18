package net.sytes.schneider.mobilechill;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.arch.persistence.room.Room;
import android.content.Intent;
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

import net.sytes.schneider.mobilechill.database.AppDatabase;
import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Timo Hasenbichler on 18.12.2017.
 */

public class LocationActivity extends ListActivity {

    private AppDatabase appDatabase;
    private LocationService locationService;
    private FusedLocationProviderClient mFusedLocationClient;

    private List<LocationEntity> locationEntities;


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
                    String tempStr = e.getName()+e.getLatidude()+e.getLongitude();
                    arrOfLoc.add(tempStr);
                });
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(),android.R.layout.simple_expandable_list_item_1,arrOfLoc);
                getListView().setAdapter(adapter);
                Log.i("Info",locationEntities.toString());


            }
        });
        button1.setOnClickListener(c -> {

            LocationServices
            LocationEntity locationEntity = new LocationEntity();
            Date today = new Date();
            locationEntity.setCreated(today);
            locationEntity.setModified(today);
            locationEntity.setName("Home1");





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
