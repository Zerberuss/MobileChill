package net.sytes.schneider.mobilechill;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.util.List;

/**
 * Created by Timo Hasenbichler on 02.01.2018.
 */

public class LocationListAdapter extends ArrayAdapter<LocationEntity> {

    private List<LocationEntity> locationEntityList;
    private int layoutResourceId;
    private Context context;


    public LocationListAdapter(@NonNull Context context, int resource, @NonNull List<LocationEntity> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.locationEntityList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LocationEntityHolder holder = new LocationEntityHolder();

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);


        holder.locationEntity = locationEntityList.get(position);

        holder.removeListEntryButton = (ImageButton)row.findViewById(R.id.remove_btn_id);
        //holder.removeListEntryButton.setTag(holder.locationEntity);

        holder.displayName = (TextView)row.findViewById(R.id.locationNameTextView);
        //holder.locationEntity = (TextView)row.findViewById(R.id.atomPay_value);

        row.setTag(holder);

        setupItem(holder);



        return super.getView(position, convertView, parent);
    }

    private void setupItem(LocationEntityHolder holder) {
        holder.displayName.setText(holder.locationEntity.getName());
        //SET OTHER BUTTOn
    }

    public static class LocationEntityHolder{
        LocationEntity locationEntity;
        TextView displayName;
        ImageButton prefernces;
        ImageButton removeListEntryButton;


    }

}
