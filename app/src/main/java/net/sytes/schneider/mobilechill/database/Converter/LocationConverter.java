package net.sytes.schneider.mobilechill.database.Converter;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by Timo Hasenbichler on 01.01.2018.
 */

public class LocationConverter {



    public Location convert2Location(LocationEntity locationEntity){
       Location location = new Location("dummyprovider");
        location.setLatitude(locationEntity.getLatidude());
        location.setLongitude(locationEntity.getLongitude());
        return location;
    }

    public LocationEntity convert2LocationEntity(Location location, Geocoder geocoder){
        Date today = new Date();

        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setCreated(today);
        locationEntity.setModified(today);
        locationEntity.setLatidude(location.getLatitude());
        locationEntity.setLongitude(location.getLongitude());

        try {
            List<Address> list = geocoder.getFromLocation(locationEntity.getLatidude(), locationEntity.getLongitude(), 1);
            if (null != list & list.size() > 0) {
                String name = list.get(0).getCountryName();
                Log.i("my location", name);
                locationEntity.setName(name);

            }

        } catch (IOException e) {
            locationEntity.setName("LOCALISATION FAILED");
            e.printStackTrace();
        }

        return locationEntity;
    }


}
