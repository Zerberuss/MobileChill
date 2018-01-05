package net.sytes.schneider.mobilechill;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.util.List;

/**
 * Created by Timo Hasenbichler on 02.01.2018.
 */

public class LocationListAdapter extends ArrayAdapter<LocationEntity> {

    private List<LocationEntity> locationEntityList;
    private int layoutResourceId;
    private Context context;
    private int layoutTextview;

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
        if(row == null){
            LayoutInflater vi = LayoutInflater.from(getContext());
            row = vi.inflate(R.layout.location_list_item,null);

        }

        ViewHolder holder = new ViewHolder();
        //setupItem(holder);
        locationEntityList.get(position).setDisplayName(locationEntityList.get(position).getName());
        if(locationEntityList.get(position).getDisplayName()!=null) {

           holder.displayName = (TextView) row.findViewById(R.id.locationNameTextView);
           holder.removeListEntryButton = (ImageButton) row.findViewById(R.id.remove_btn_id);
            holder.changeNameButton = (ImageButton) row.findViewById(R.id.modifyNameButton);
            holder.changeWirelessPreferencesButton = (ToggleButton) row.findViewById(R.id.wirelessPreferencesButton);

            //(CharSequence) locationEntityList.get(position);
            // Create an ArrayAdapter using the string array and a default spinner layout

        }
        holder.displayName.setText( locationEntityList.get(position).getDisplayName());
        holder.removeListEntryButton.setTag(locationEntityList.get(position));
        if(locationEntityList.get(position).isWirelessPreferences()) {
            holder.changeWirelessPreferencesButton.setChecked(true);
        }




        row.setTag(holder);
        return row;
    }


    public static class ViewHolder {
        public LocationEntity locationEntity;
        public TextView displayName;
        public ImageButton removeListEntryButton;
        public ImageButton changeNameButton;
        public ToggleButton changeWirelessPreferencesButton;
    }

}
